package org.ecloudmanager.node.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AWSNodeAPI implements NodeBaseAPI {
    static Logger log = LoggerFactory.getLogger(AWSNodeAPI.class);
    private static String DEFAULT_SECURITY_GROUP_NAME = "default";
    private static String TAG_NAME = "Name";

    private static String getSubnetLabel(com.amazonaws.services.ec2.model.Subnet subnet) {
        return subnet.getTags().stream()
                .filter(t -> t.getKey().equals("Name"))
                .map(Tag::getValue)
                .findAny()
                .orElse("");
    }

    private static FirewallRule fromIpPermission(IpPermission permission) {
        return new FirewallRule().protocol(permission.getIpProtocol()).port(permission.getToPort().toString()).from(permission.getIpRanges());
    }

    private static IpPermission fromFirewallRule(FirewallRule rule) {
        return new IpPermission().withFromPort(Integer.parseInt(rule.getPort())).withToPort(Integer.parseInt(rule.getPort())).withIpProtocol(rule.getProtocol()).withIpRanges(rule.getFrom());
    }

    private String getCidrIp() {
        return "0.0.0.0/0";
    }

    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception {
        return Arrays.stream(Parameter.values()).map(Parameter::getNodeParameter).collect(Collectors.toList());
    }

    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = parameters == null ? null : parameters.get(Parameter.region.name());
        AmazonEC2 amazonEC2 = region == null ? null : AWS.ec2(accessKey, secretKey, region);
        Parameter p = Parameter.valueOf(parameter);
        switch (p) {
            case region:
                return Stream.of(Regions.values())
                        .map(r -> new ParameterValue().value(r.getName())).collect(Collectors.toList());
            case instance_type:
                return Stream.of(AWSInstanceType.values())
                        .map(t -> new ParameterValue().value(t.getInstanceTypeName()))
                        .collect(Collectors.toList());
            case subnet:
                return amazonEC2 == null ?
                        Collections.emptyList() :
                        amazonEC2.describeSubnets().getSubnets().stream()
                                .map(s -> new ParameterValue().value(s.getSubnetId())
                                        .description(s.getSubnetId() + " : " + s.getCidrBlock() + " " + getSubnetLabel(s))).collect(Collectors.toList());
            case keypair:
                return amazonEC2 == null ?
                        Collections.emptyList() :
                        amazonEC2.describeKeyPairs().getKeyPairs().stream()
                                .map(k -> new ParameterValue().value(k.getKeyName())).collect(Collectors.toList());
            case image:
                try {
                    List<ParameterValue> result = amazonEC2 == null ?
                            Collections.emptyList() :
                            amazonEC2.describeImages().getImages().stream()
                                    .map(i -> new ParameterValue().value(i.getImageId()).description(i.getDescription())).collect(Collectors.toList());
                    return result;
                } catch (Exception t) {
                    t.printStackTrace();

                }
            case storage:
            case name:
            case cpu:
            case memory:
                return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public CreateNodeResponse createNode(Credentials credentials, Map<String, String> parameters) {
        // TODO tags support
        ExecutionDetails details = new ExecutionDetails();

        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = parameters.get(Parameter.region.name());
        String name = parameters.get(Parameter.name.name());
        String subnet = parameters.get(Parameter.subnet.name());
        int storage = Integer.parseInt(parameters.get(Parameter.storage.name()));
        String image = parameters.get(Parameter.image.name());
        String instanceType = parameters.get(Parameter.instance_type.name());
        String keyPair = parameters.get(Parameter.keypair.name());
        log.info("Creating AWS security group " + name);
        AmazonEC2 ec2;
        try {
            ec2 = AWS.ec2(accessKey, secretKey, region);
        } catch (Exception e) {
            NodeUtil.logError(details, "Can't connect to AWS region " + region, e);
            return new CreateNodeResponse().details(details);
        }
        String securityGroupId;
        try {
            securityGroupId = createSecurityGroup(ec2, subnet, name);
            NodeUtil.logInfo(details, "Security group created with name: " + name + " id: " + securityGroupId);
        } catch (Exception t) {
            NodeUtil.logError(details, "Can't create security group with name " + name, t);
            return new CreateNodeResponse().details(details);
        }
        try {
            addSshAcess(ec2, securityGroupId);
            NodeUtil.logInfo(details, "Ssh access firewall rule added");
        } catch (Exception t) {
            NodeUtil.logError(details, "Can't add ssh access firewall rule", t);
            deleteSecurityGroup(ec2, securityGroupId, details);
            return new CreateNodeResponse().details(details);
        }
        String nodeId;
        try {
            nodeId = createNode(ec2, storage, securityGroupId, subnet, image, instanceType, keyPair);
            NodeUtil.logInfo(details, "Node created with id: " + nodeId);
        } catch (Exception t) {
            NodeUtil.logError(details, "Can't create node", t);
            deleteSecurityGroup(ec2, securityGroupId, details);
            return new CreateNodeResponse().details(details);
        }
        try {
            createTags(accessKey, secretKey, nodeId, name, ec2);
        } catch (Exception e) {
            NodeUtil.logInfo(details, "Tags creation failed: " + e.getMessage());

        }
        return new CreateNodeResponse().nodeId(region + ":" + nodeId).details(details.status(ExecutionDetails.StatusEnum.OK));
    }

    private void deleteSecurityGroup(AmazonEC2 ec2, String id, ExecutionDetails details) {
        try {
            ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(id));
            NodeUtil.logInfo(details, "Security group deleted with id: " + id);
        } catch (Exception t1) {
            NodeUtil.logError(details, "Failed to delete security group with id: " + id, t1);
        }
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        Reservation reservation = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(id)).getReservations().get(0);
        Instance instance = reservation.getInstances().get(0);
        Optional<Tag> nameTag = instance.getTags().stream().filter(t -> t.getKey().equals(TAG_NAME)).findFirst();
        String vpcId = instance.getVpcId();
        String awsStatus = instance.getState().getName();
        NodeInfo.StatusEnum status;
        switch (awsStatus) {
            case "running":
                status = NodeInfo.StatusEnum.RUNNING;
                break;
            case "terminated":
                status = NodeInfo.StatusEnum.TERMINATED;
                break;
            default:
                status = NodeInfo.StatusEnum.PENDING;
        }
        NodeInfo info = new NodeInfo().status(status).ip(instance.getPrivateIpAddress()).id(nodeId);
        return info;
    }

    @Override
    public ExecutionDetails updateNode(Credentials credentials, String nodeId, Node node) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        NodeUtil.logInfo(details, "TODO");
        // TODO
        return details.status(ExecutionDetails.StatusEnum.OK);
    }

    @Override
    public ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        Reservation reservation = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(id)).getReservations().get(0);
        Instance instance = reservation.getInstances().get(0);
        String vpcId = instance.getVpcId();
        String groupIdToDelete = getSecurityGroup(ec2, id);
        if (groupIdToDelete != null) {
            log.info("Deassociating security group");
            List<com.amazonaws.services.ec2.model.SecurityGroup> sgs = ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest().withFilters(new Filter("vpc-id").withValues(vpcId))).getSecurityGroups();
            // TODO ? need better impl
            Optional<com.amazonaws.services.ec2.model.SecurityGroup> defaultSG = sgs.stream().filter(sg -> !groupIdToDelete.equals(sg.getGroupId())).findFirst();
            if (defaultSG.isPresent()) {
                NodeUtil.logInfo(details, "Node created with id: " + nodeId);
                ec2.modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(id).withGroups(defaultSG.get().getGroupId()));
                log.info("Deleting security group");
                ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(groupIdToDelete));
                NodeUtil.logInfo(details, "Security group deleted: " + groupIdToDelete);
            }
        }
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(id);
        TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
        NodeUtil.logInfo(details, "Node terminated: " + id);
        return details.status(ExecutionDetails.StatusEnum.OK);
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        String groupId = getSecurityGroup(ec2, id);
        com.amazonaws.services.ec2.model.SecurityGroup sg = ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupIds(groupId)).getSecurityGroups().get(0);
        List<IpPermission> permissions = sg.getIpPermissions();
        List<FirewallRule> rules = permissions.stream().map(AWSNodeAPI::fromIpPermission).collect(Collectors.toList());
        return new FirewallInfo().rules(rules);
    }

    @Override
    public ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        String securityGroupId = getSecurityGroup(ec2, id);
        List<IpPermission> permissions = firewallUpdate.getCreate().stream().map(AWSNodeAPI::fromFirewallRule).collect(Collectors.toList());
        if (!permissions.isEmpty()) {
            AuthorizeSecurityGroupIngressRequest req = new AuthorizeSecurityGroupIngressRequest()
                    .withGroupId(securityGroupId).withIpPermissions(permissions);
            ec2.authorizeSecurityGroupIngress(req);
            NodeUtil.logInfo(details, "Firewall rules created:");
            NodeUtil.logInfo(details, firewallUpdate.getCreate().toString());
        }
        List<IpPermission> permissionsToDelete = firewallUpdate.getDelete().stream().map(AWSNodeAPI::fromFirewallRule).collect(Collectors.toList());
        if (!permissionsToDelete.isEmpty()) {
            RevokeSecurityGroupIngressRequest delete = new RevokeSecurityGroupIngressRequest().withGroupId(securityGroupId).withIpPermissions(permissionsToDelete);
            ec2.revokeSecurityGroupIngress(delete);
            NodeUtil.logInfo(details, "Firewall rules deleted:");
            NodeUtil.logInfo(details, firewallUpdate.getDelete().toString());
        }
        return details.status(ExecutionDetails.StatusEnum.OK);
    }

    private void createTags(String accessKey, String secretKey, String instanceId, String name, AmazonEC2 ec2) {
        // TODO generic tags support
        String userId;
        try {
            userId = AWS.identityManagementClient(accessKey, secretKey).getUser().getUser().getUserId();
        } catch (AmazonServiceException e) {
            userId = e.getErrorMessage().replaceAll("^[^/]*/", "").replaceAll(" is not authorized .*", "");
        }
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag(TAG_NAME, name));
        tags.add(new Tag("Creator", userId));
        tags.add(new Tag("Group", "rfng"));
        tags.add(new Tag("Cost Center", "521633"));
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                .withResources(instanceId)
                .withTags(tags);

        log.info("Updating tags of AWS instance " + name);
        ec2.createTags(createTagsRequest);
    }

    private String getSecurityGroup(AmazonEC2 ec2, String nodeInternalId) {
        Reservation reservation = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(nodeInternalId)).getReservations().get(0);
        Instance instance = reservation.getInstances().get(0);
        Optional<Tag> nameTag = instance.getTags().stream().filter(t -> t.getKey().equals(TAG_NAME)).findFirst();
        String vpcId = instance.getVpcId();
        if (nameTag.isPresent()) {
            String name = nameTag.get().getValue();
            Optional<GroupIdentifier> group = instance.getSecurityGroups().stream().filter(g -> g.getGroupName().startsWith(name)).findAny();
            if (group.isPresent()) {
                return group.get().getGroupId();
            }
        }
        return null;
    }

    private String createSecurityGroup(AmazonEC2 ec2, String subnet, String name) {
        DescribeSubnetsResult describeSubnetsResult = ec2.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(subnet));
        String vpcId = describeSubnetsResult.getSubnets().get(0).getVpcId();
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(name, "Created by ecloud manager").withVpcId(vpcId);
        CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(createSecurityGroupRequest);
        String securityGroupId = createSecurityGroupResult.getGroupId();
        return securityGroupId;
    }

    private void addSshAcess(AmazonEC2 ec2, String securityGroupId) {
        AuthorizeSecurityGroupIngressRequest sshRule = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(securityGroupId)
                .withFromPort(22)
                .withToPort(22)
                .withIpProtocol("TCP")
                .withCidrIp(getCidrIp());
        ec2.authorizeSecurityGroupIngress(sshRule);
    }

    private String createNode(AmazonEC2 ec2, int storage, String securityGroupId, String subnet, String image, String instanceType, String keyPair) {
        BlockDeviceMapping mapping = new BlockDeviceMapping().withDeviceName("/dev/sda1").withEbs(new EbsBlockDevice
                ().withDeleteOnTermination(true).withVolumeSize(storage));
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withMaxCount(1)
                .withMinCount(1)
                .withSubnetId(subnet)
                .withImageId(image)
                .withInstanceType(instanceType)
                .withKeyName(keyPair)
                .withBlockDeviceMappings(mapping)
                .withSecurityGroupIds(securityGroupId);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        Instance inst = runInstancesResult.getReservation().getInstances().get(0);
        return inst.getInstanceId();
    }

    private enum Parameter {
        name("Node name", true, null, false, false),
        region("AWS region", true, null, true, true),
        subnet("AWS subnet", true, null, true, true, region),
        storage("Storage size", true, "20", false, false),
        cpu("CPU count", false, null, false, false),
        memory("memory", false, null, false, false),
        instance_type("Instance type", true, null, true, true, cpu, memory),
        keypair("Keypair", true, null, true, true, region),
        image("AWS image", true, null, false, true, region);

        private NodeParameter nodeParameter;

        Parameter(String description, boolean required, String defaultValue, boolean canSuggest, boolean strictSuggest, Parameter... args) {
            List<String> argsList = Arrays.stream(args).map(Enum::name).collect(Collectors.toList());
            nodeParameter = new NodeParameter().name(name()).description(description)
                    .required(required).defaultValue(defaultValue)
                    .canSuggest(canSuggest).strictSuggest(strictSuggest).args(argsList);
        }

        public NodeParameter getNodeParameter() {
            return nodeParameter;
        }

    }


}

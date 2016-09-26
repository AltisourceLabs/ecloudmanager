package org.ecloudmanager.node.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AWSNodeAPI implements NodeBaseAPI {
    private static final int RETRY_TIMEOUT = 10;
    static Logger log = LoggerFactory.getLogger(AWSNodeAPI.class);
    private static String DEFAULT_SECURITY_GROUP_NAME = "default";
    private static String TAG_NAME = "Name";
    private static String TAG_CREATOR = "Creator";
    private static String TAG_HOSTED_ZONE = "Hosted Zone";
    private static APIInfo API_INFO = new APIInfo().id("AWS").description("Amazon EC2");
    static {
        try {
            GitInfo gitInfo = GitInfo.get();
            API_INFO = API_INFO.version(gitInfo.getBuildVersion()).revision(gitInfo.getCommitIdAbbrev());
        } catch (IOException e) {
            log.error("Can't read Git Info", e);
        }
    }

    private static String getSubnetLabel(com.amazonaws.services.ec2.model.Subnet subnet) {
        return subnet.getTags().stream()
                .filter(t -> t.getKey().equals("Name"))
                .map(Tag::getValue)
                .findAny()
                .orElse("");
    }

    private static List<FirewallRule> fromIpPermission(IpPermission permission) {
        return permission.getIpRanges().stream().map(range ->
                new FirewallRule().type(FirewallRule.TypeEnum.IP).protocol(permission.getIpProtocol()).port(permission.getToPort()).from(range)).collect(Collectors.toList());
    }

    private static Predicate<Throwable> errorCode(String errorCode) {
        return e -> AmazonServiceException.class.isInstance(e) && errorCode.equals(AmazonServiceException.class.cast(e).getErrorCode());
    }

    @Override
    public APIInfo getAPIInfo() {
        return API_INFO;
    }

    private String getUserId(String accessKey, String secretKey) {
        try {
            return AWS.identityManagementClient(accessKey, secretKey).getUser().getUser().getUserId();
        } catch (AmazonServiceException e) {
            return e.getErrorMessage().replaceAll("^[^/]*/", "").replaceAll(" is not authorized .*", "");
        }
    }

    private void createTags(String userId, String instanceId, Map<String, String> parameters, AmazonEC2 ec2) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag(TAG_CREATOR, userId));
        getTagParameters().forEach(p -> {
                    if (parameters.containsKey(p.getName())) {
                        tags.add(new Tag(p.getName(), parameters.get(p.getName())));
                    }
        });
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                .withResources(instanceId)
                .withTags(tags);
        log.info("Updating tags of AWS instance " + instanceId);
        ec2.createTags(createTagsRequest);
        log.info("Tags created:");
        createTagsRequest.getTags().forEach(t -> log.info(t.toString()));
    }

    private List<NodeParameter> getTagParameters() {
        // FIXME should be configurable
        NodeParameter costCenter = new NodeParameter().name("Cost Center").description("Cost Center").create(true).configure(true).canSuggest(true).strictSuggest(false).required(true).defaultValue("521633");
        NodeParameter group = new NodeParameter().name("Group").description("Group").create(true).configure(true).canSuggest(true).strictSuggest(false).required(true).defaultValue("rfng");
        return Lists.newArrayList(costCenter, group);
    }

    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) {
        return Stream.concat(Arrays.stream(Parameter.values()).map(Parameter::getNodeParameter), getTagParameters().stream()).collect(Collectors.toList());
    }

    private List<String> getTagValues(AmazonEC2 ec2, String tag) {
        DescribeTagsResult describeTagsResult = ec2.describeTags(new DescribeTagsRequest().withFilters(new Filter("key", Lists.newArrayList(tag))));
        return describeTagsResult.getTags().stream()
                .map(TagDescription::getValue)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = parameters == null ? null : parameters.get(Parameter.region.name());
        AmazonEC2 amazonEC2 = region == null ? null : AWS.ec2(accessKey, secretKey, region);
        AmazonRoute53 route53Client = AWS.route53(accessKey, secretKey);
        Parameter p;
        try {
            p = Parameter.valueOf(parameter);
        } catch (IllegalArgumentException e) {
            return getTagValues(amazonEC2, parameter).stream().map(t -> new ParameterValue().value(t)).collect(Collectors.toList());
        }
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
            case hosted_zone:
                return route53Client.listHostedZones().getHostedZones().stream().map(z -> new ParameterValue().value(z.getName())).collect(Collectors.toList());
            case image:
//                if (amazonEC2 == null) {
//                    return Collections.emptyList();
//                }
//                try {
//                    List<Image> images = amazonEC2.describeImages().getImages();
//                    return  images.stream()
//                            .map(i -> new ParameterValue().value(i.getImageId()).description(i.getDescription())).collect(Collectors.toList());
//                } catch (Exception t) {
//                    t.printStackTrace();
//
//                }
            case storage:
            case name:
            case cpu:
            case memory:
                return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public String createNode(Credentials credentials, Map<String, String> parameters) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = parameters.get(Parameter.region.name());
        String subnet = parameters.get(Parameter.subnet.name());
        String name = parameters.get(Parameter.name.name());
        int storage = Integer.parseInt(parameters.get(Parameter.storage.name()));
        String image = parameters.get(Parameter.image.name());
        String instanceType = parameters.get(Parameter.instance_type.name());
        String keyPair = parameters.get(Parameter.keypair.name());
        log.info("Creating AWS security group " + name);
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        String securityGroupId = createSecurityGroup(ec2, subnet, name);
        log.info("Security group created with name: " + name + " id: " + securityGroupId);
        try {
            addSshAcess(ec2, securityGroupId);
            log.info("Ssh access firewall rule added");
        } catch (Exception t) {
            log.error("Can't add ssh access firewall rule", t);
            deleteSecurityGroupOnFailure(ec2, securityGroupId);
            throw t;
        }
        String vmId;
        RetryPolicy createNodeRetryPolicy = new RetryPolicy()
                .retryOn(e -> AmazonServiceException.class.isInstance(e) && "InvalidGroup.NotFound".equals(AmazonServiceException.class.cast(e).getErrorCode()))
                .withDelay(1, TimeUnit.SECONDS)
                .withMaxDuration(10, TimeUnit.MINUTES);
        try {
            vmId = Failsafe.with(createNodeRetryPolicy).get(() -> createNode(ec2, storage, securityGroupId, subnet, image, instanceType, keyPair));
            log.info("VM created with id: " + vmId);
        } catch (Exception t) {
            log.error("Can't create node", t);
            deleteSecurityGroupOnFailure(ec2, securityGroupId);
            throw t;
        }
        return region + ":" + vmId;
    }

    private void deleteSecurityGroupOnFailure(AmazonEC2 ec2, String id) {
        try {
            log.info("Security group deleted with id: " + id);
            ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(id));
            log.info("Security group deleted with id: " + id);
        } catch (Exception t1) {
            log.error("WARNING! Failed to delete security group with id: " + id, t1);
        }
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        Reservation reservation = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(id)).getReservations().get(0);
        Instance instance = reservation.getInstances().get(0);
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

    private IpPermission fromFirewallRule(Credentials credentials, FirewallRule rule) throws IllegalArgumentException {
        String ipRange = "";
        switch (rule.getType()) {
            case IP:
                ipRange = rule.getFrom() + "/32";
                break;
            case NODE_ID:
                try {
                    ipRange = getNode(credentials, rule.getFrom()).getIp() + "/32";
                } catch (AWS.RegionNotExistException | ExecutionException e) {
                    throw new IllegalArgumentException("Can't access node " + rule.getFrom(), e);
                }
                break;
            case ANY:
                ipRange = "0.0.0.0/0";
                break;
        }
        return new IpPermission().withFromPort(rule.getPort()).withToPort(rule.getPort()).withIpProtocol(rule.getProtocol()).withIpRanges(ipRange);
    }

    private String getCidrIp() {
        return "0.0.0.0/0";
    }

    private int getStorageSize(AmazonEC2 ec2, String vmId) {
        DescribeInstancesResult result =
                ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
        Instance instance = result.getReservations().get(0).getInstances().get(0);
        InstanceBlockDeviceMapping blockDeviceMapping = instance.getBlockDeviceMappings().get(0);
        String volumeId = blockDeviceMapping.getEbs().getVolumeId();
        DescribeVolumesResult dvr = ec2.describeVolumes(new DescribeVolumesRequest().withVolumeIds(volumeId));
        return dvr.getVolumes().get(0).getSize();
    }

    private Instance getInstance(AmazonEC2 ec2, String vmId) {
        DescribeInstancesResult result =
                ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
        return result.getReservations().size() > 0 ? result.getReservations().get(0).getInstances().get(0) : null;
    }

    private String getInstanceName(Instance instance) {
        Optional<Tag> tag = instance.getTags().stream().filter(t -> t.getKey().equals(TAG_NAME)).findAny();
        return tag.isPresent() ? tag.get().getValue() : null;
    }

    private void setInstanceName(AmazonEC2 ec2, String vmId, String name) {
        ec2.createTags(new CreateTagsRequest()
                .withResources(vmId)
                .withTags(new Tag(TAG_NAME, name)));
    }

    @Override
    public NodeInfo configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String vmId = nodeId.split(":")[1];
        String userId = getUserId(accessKey, secretKey);
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        AmazonRoute53 route53 = AWS.route53(accessKey, secretKey);

        Instance instance = getInstance(ec2, vmId);

        String oldName = getInstanceName(instance);
        String newName = parameters.get(Parameter.name.name());
        if (newName != null && !newName.equals(oldName)) {
            setInstanceName(ec2, vmId, newName);
            log.info("Value of tag '" + TAG_NAME + "' set to " + newName);

        }
        createTags(userId, vmId, parameters, ec2);

        String oldHostedZone = getInstanceHostedZone(instance);
        String newHostedZone = parameters.get(Parameter.hosted_zone.name());
        if (!StringUtils.equals(oldHostedZone, newHostedZone)) {
            setInstanceHostedZone(ec2, vmId, newHostedZone);
            log.info("Value of tag '" + TAG_HOSTED_ZONE + "' set to " + newHostedZone);
        }
        if (!StringUtils.equals(oldName, newName) ||
                !StringUtils.equals(oldHostedZone, newHostedZone)) {
            if (oldHostedZone != null) {
                deleteDnsRecord(route53, oldName, oldHostedZone);
            }
            createDnsRecord(route53, instance.getPrivateIpAddress(), newName, newHostedZone);
            log.info("Created DNS record for " + newName + "." + newHostedZone + " with IP: " + instance.getPrivateIpAddress());
        }

        String newInstanceType = parameters.get(Parameter.instance_type.name());

        String newStorage = parameters.get(Parameter.storage.name());
        boolean needUpdateInstanceType = newInstanceType != null && !StringUtils.equals(instance.getInstanceType(), newInstanceType);

        boolean needResizeStorage = newStorage != null && getStorageSize(ec2, vmId) != Integer.parseInt(newStorage);
        if (needUpdateInstanceType || needResizeStorage) {
            log.info("Stopping AWS instance " + vmId + "(" + newName + ")");
            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(vmId);
            ec2.stopInstances(stopInstancesRequest);
            RetryPolicy untilInstanceStopped = new RetryPolicy()
                    .<DescribeInstancesResult>retryIf((r) -> !"stopped".equals(r.getReservations().get(0).getInstances().get(0).getState().getName()))
                    .withDelay(1, TimeUnit.SECONDS)
                    .withMaxDuration(10, TimeUnit.MINUTES);
            Failsafe.with(untilInstanceStopped).get(() -> ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId)));
        }

        if (needUpdateInstanceType) {
            log.info("Changing instance type of AWS instance " + vmId + "(" + newName + ") to " + newInstanceType);
            ModifyInstanceAttributeRequest modifyInstanceAttributeRequest =
                    new ModifyInstanceAttributeRequest().withInstanceId(vmId).withInstanceType(newInstanceType);
            ec2.modifyInstanceAttribute(modifyInstanceAttributeRequest);
        }

        if (needResizeStorage) {
            log.info("Resizing root volume of AWS instance " + vmId + "(" + newName + ")");
            InstanceBlockDeviceMapping blockDeviceMapping = instance.getBlockDeviceMappings().get(0);
            String volumeId = blockDeviceMapping.getEbs().getVolumeId();
            log.info("Creating snapshot of AWS volume " + volumeId + " attached to instance " + vmId + "(" + newName + ")");
            CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest(volumeId, "Resize instance " + vmId);
            CreateSnapshotResult createSnapshotResult = ec2.createSnapshot(createSnapshotRequest);
            String snapshotId = createSnapshotResult.getSnapshot().getSnapshotId();

            RetryPolicy untilSnapshotCompleted = new RetryPolicy()
                    .<Snapshot>retryIf(s -> !"completed".equals(s.getState()))
                    .withDelay(1, TimeUnit.SECONDS)
                    .withMaxDuration(10, TimeUnit.MINUTES);
            Failsafe.with(untilSnapshotCompleted).get(() -> ec2.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snapshotId)).getSnapshots().get(0));

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest().withVolumeIds(volumeId);
            DescribeVolumesResult describeVolumesResult = ec2.describeVolumes(describeVolumesRequest);
            String availabilityZone = describeVolumesResult.getVolumes().get(0).getAvailabilityZone();

            log.info("Creating new volume " + volumeId + " in availability zone " + availabilityZone + " from snapshot " + snapshotId);
            CreateVolumeRequest createVolumeRequest =
                    new CreateVolumeRequest(snapshotId, availabilityZone).withSize(Integer.parseInt(newStorage));
            CreateVolumeResult createVolumeResult = ec2.createVolume(createVolumeRequest);
            String newVolumeId = createVolumeResult.getVolume().getVolumeId();

            log.info("New volume " + newVolumeId + " was created from snapshot " + snapshotId);

            RetryPolicy untilVolumeAvailable = new RetryPolicy()
                    .<Volume>retryIf(v -> !"available".equals(v.getState()))
                    .withDelay(1, TimeUnit.SECONDS)
                    .withMaxDuration(10, TimeUnit.MINUTES);

            Failsafe.with(untilVolumeAvailable).get(() -> ec2.describeVolumes(new DescribeVolumesRequest().withVolumeIds(newVolumeId)).getVolumes().get(0));



            log.info("Detaching old volume " + volumeId + " from " + vmId + "(" + newName + ")");
            ec2.detachVolume(new DetachVolumeRequest(volumeId));

            log.info("Attaching new volume " + newVolumeId + " to instance " + vmId + "(" + newName + ")");
            AttachVolumeRequest attachVolumeRequest = new AttachVolumeRequest(newVolumeId, vmId, blockDeviceMapping.getDeviceName());
            ec2.attachVolume(attachVolumeRequest);

            log.info("Deleting old volume " + volumeId);
            ec2.deleteVolume(new DeleteVolumeRequest(volumeId));

            log.info("Deleting snapshot " + snapshotId);
            ec2.deleteSnapshot(new DeleteSnapshotRequest(snapshotId));
        }

        if (needUpdateInstanceType || needResizeStorage) {
            log.info("Starting AWS instance " + vmId + "(" + newName + ")");
            ec2.startInstances(new StartInstancesRequest().withInstanceIds(vmId));

            RetryPolicy untilInstanceReady = new RetryPolicy()
                    .<Instance>retryIf(i -> !"running".equals(i.getState().getName()) || !Strings.isNullOrEmpty(i.getPrivateIpAddress()))
                    .withDelay(1, TimeUnit.SECONDS)
                    .withMaxDuration(10, TimeUnit.MINUTES);
            Failsafe.with(untilInstanceReady).get(() -> ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId)).getReservations().get(0).getInstances().get(0));
        }
        return getNode(credentials, nodeId);
    }

    private String getInstanceHostedZone(Instance instance) {
        Optional<Tag> tag = instance.getTags().stream().filter(t -> t.getKey().equals(TAG_HOSTED_ZONE)).findAny();
        return tag.isPresent() ? tag.get().getValue() : null;
    }

    private void setInstanceHostedZone(AmazonEC2 ec2, String vmId, String hostedZone) {
        ec2.createTags(new CreateTagsRequest()
                .withResources(vmId)
                .withTags(new Tag(TAG_HOSTED_ZONE, hostedZone)));
    }

    private ChangeResourceRecordSetsResult createDnsRecord(AmazonRoute53 route53, String ip, String name, String hostedZoneName) {
        log.info("Creating route53 record for " + name);

        HostedZone hostedZone = getHostedZone(hostedZoneName, route53);

        ResourceRecord resourceRecord = new ResourceRecord(ip);
        ResourceRecordSet resourceRecordSet = new ResourceRecordSet(name + "." + hostedZoneName, RRType.A).withTTL
                (60L).withResourceRecords(resourceRecord);
        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(new Change(ChangeAction.CREATE,
                resourceRecordSet)));
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZone.getId()).withChangeBatch(changeBatch);
        return failsafeChangeResourceRecordSets(route53, changeResourceRecordSetsRequest);
    }

    private ChangeResourceRecordSetsResult failsafeChangeResourceRecordSets(AmazonRoute53 route53, ChangeResourceRecordSetsRequest request) {
        RetryPolicy route53RetryPolicy = new RetryPolicy()
                .retryOn(PriorRequestNotCompleteException.class)
                .withDelay(1, TimeUnit.SECONDS)
                .withMaxDuration(10, TimeUnit.MINUTES);
        return Failsafe.with(route53RetryPolicy).get(() -> route53.changeResourceRecordSets(request));
    }

    private HostedZone getHostedZone(String awsHostedZone, AmazonRoute53 route53) {
        return route53.listHostedZones().getHostedZones().stream()
                .filter(z -> z.getName().equals(awsHostedZone))
                .findAny()
                .orElse(null);
    }

    private void deleteDnsRecord(AmazonRoute53 route53, String name, String hostedZoneName) {
        log.info("Deleting route53 record for " + name);
        if (StringUtils.isEmpty(name)) {
            log.error("Cannot delete route53 record - name is empty.");
            return;
        }
        HostedZone hostedZone = getHostedZone(hostedZoneName, route53);

        String recordSetName = name.toLowerCase() + "." + hostedZoneName;
        ListResourceRecordSetsRequest listResourceRecordSetsRequest =
                new ListResourceRecordSetsRequest(hostedZone.getId()).withStartRecordName(recordSetName);
        List<ResourceRecordSet> resourceRecordSets = route53.listResourceRecordSets
                (listResourceRecordSetsRequest).getResourceRecordSets();
        List<ResourceRecordSet> filtered = resourceRecordSets.stream().filter(f -> recordSetName.equals(f.getName())).collect(Collectors.toList());
        if (filtered.size() < 1) {
            log.warn("Cannot delete route53 record - record not found. Skipping this step.");
            return;
        }
        log.info("Records found:  " + filtered);
        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(new Change(ChangeAction.DELETE,
                filtered.get(0))));
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZone.getId()).withChangeBatch(changeBatch);
        ChangeResourceRecordSetsResult result = failsafeChangeResourceRecordSets(route53, changeResourceRecordSetsRequest);
        log.info("Submitted delete recordset request " + result.getChangeInfo().toString());
    }


    @Override
    public void deleteNode(Credentials credentials, String nodeId) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        AmazonRoute53 route53 = AWS.route53(accessKey, secretKey);
        Instance instance = getInstance(ec2, id);
        if (instance == null) {
            log.warn("Instance " + id + " not found - skip deleting instance, route53 record and security group.");
            return;
        }
        String vpcId = instance.getVpcId();
        String name = getInstanceName(instance);
        String hostedZone = getInstanceHostedZone(instance);
        if (!Strings.isNullOrEmpty(hostedZone)) {
            try {
                deleteDnsRecord(route53, name, hostedZone);
            } catch (Exception e) {
                log.warn("Can't delete dns record: " + name + "." + hostedZone, e);
            }
        }

        String groupIdToDelete = getSecurityGroup(ec2, id);
        if (groupIdToDelete != null) {
            log.info("Deassociating security group");
            List<com.amazonaws.services.ec2.model.SecurityGroup> sgs = ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest().withFilters(new Filter("vpc-id").withValues(vpcId))).getSecurityGroups();
            // TODO ? need better impl
            Optional<com.amazonaws.services.ec2.model.SecurityGroup> defaultSGOptional = sgs.stream().filter(sg -> !groupIdToDelete.equals(sg.getGroupId())).findFirst();
            if (defaultSGOptional.isPresent()) {
                com.amazonaws.services.ec2.model.SecurityGroup defaultSG = defaultSGOptional.get();
                log.info("Assigning " + nodeId + " to security group id: " + defaultSG.getGroupId() + " name: " + defaultSG.getGroupName());
                ec2.modifyInstanceAttribute(new ModifyInstanceAttributeRequest().withInstanceId(id).withGroups(defaultSG.getGroupId()));
                log.info("Node " + nodeId + " assigned to security group id: " + defaultSG.getGroupId() + " name: " + defaultSG.getGroupName());
                log.info("Deleting security group " + groupIdToDelete);
                ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(groupIdToDelete));
                log.info("Security group deleted: " + groupIdToDelete);
            }
        }
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(id);
        TerminateInstancesResult result = ec2.terminateInstances(terminateInstancesRequest);
        log.info("Node terminated: " + id);
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        String groupId = getSecurityGroup(ec2, id);
        com.amazonaws.services.ec2.model.SecurityGroup sg = ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupIds(groupId)).getSecurityGroups().get(0);
        List<IpPermission> permissions = sg.getIpPermissions();
        List<FirewallRule> rules = permissions.stream().flatMap(p -> fromIpPermission(p).stream()).collect(Collectors.toList());
        return new FirewallInfo().rules(rules);
    }

    @Override
    public FirewallInfo updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws AWS.RegionNotExistException, ExecutionException {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        String securityGroupId = getSecurityGroup(ec2, id);
        List<IpPermission> permissions = firewallUpdate.getCreate().stream().map(p -> fromFirewallRule(credentials, p)).filter(Objects::nonNull).collect(Collectors.toList());
        List<IpPermission> permissionsToDelete = firewallUpdate.getDelete().stream().map(p -> fromFirewallRule(credentials, p)).filter(Objects::nonNull).collect(Collectors.toList());
        if (!permissions.isEmpty()) {
            AuthorizeSecurityGroupIngressRequest req = new AuthorizeSecurityGroupIngressRequest()
                    .withGroupId(securityGroupId).withIpPermissions(permissions);
            ec2.authorizeSecurityGroupIngress(req);
            log.info("Firewall rules created for node " + nodeId);
            log.info(firewallUpdate.getCreate().toString());
        }

        if (!permissionsToDelete.isEmpty()) {
            RevokeSecurityGroupIngressRequest delete = new RevokeSecurityGroupIngressRequest().withGroupId(securityGroupId).withIpPermissions(permissionsToDelete);
            ec2.revokeSecurityGroupIngress(delete);
            log.info("Firewall rules deleted for node " + nodeId);
            log.info(firewallUpdate.getDelete().toString());
        }
        return getNodeFirewallRules(credentials, nodeId);
    }

    private String getSecurityGroup(AmazonEC2 ec2, String vmId) {
        Reservation reservation = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId)).getReservations().get(0);
        Instance instance = reservation.getInstances().get(0);
        Optional<Tag> nameTag = instance.getTags().stream().filter(t -> t.getKey().equals(TAG_NAME)).findFirst();
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
        name("Node name", false, true, true, null, false, false),
        region("AWS region", true, false, true, null, true, true),
        subnet("AWS subnet", true, false, true, null, true, true, region),
        storage("Storage size", true, true, true, "20", false, false),
        cpu("CPU count", true, true, false, null, false, false),
        memory("memory", true, true, false, null, false, false),
        instance_type("Instance type", true, true, true, null, true, true, cpu, memory),
        keypair("Keypair", true, true, true, null, true, true, region),
        image("AWS image", true, false, true, null, false, true, region),
        hosted_zone("AWS hosted zone", false, true, true, null, true, true),;

        private NodeParameter nodeParameter;

        Parameter(String description, boolean create, boolean configure, boolean required, String defaultValue, boolean canSuggest, boolean strictSuggest, Parameter... args) {
            List<String> argsList = Arrays.stream(args).map(Enum::name).collect(Collectors.toList());
            nodeParameter = new NodeParameter().name(name()).description(description).create(create).configure(configure)
                    .required(required).defaultValue(defaultValue)
                    .canSuggest(canSuggest).strictSuggest(strictSuggest).args(argsList);
        }

        public NodeParameter getNodeParameter() {
            return nodeParameter;
        }

    }


}

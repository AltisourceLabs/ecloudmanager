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
import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.node.util.SynchronousPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
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

    private static List<FirewallRule> fromIpPermission(IpPermission permission) {
        return permission.getIpRanges().stream().map(range ->
                new FirewallRule().type(FirewallRule.TypeEnum.IP).protocol(permission.getIpProtocol()).port(permission.getToPort().toString()).from(range)).collect(Collectors.toList());
    }

    private IpPermission fromFirewallRule(Credentials credentials, FirewallRule rule) throws Exception {
        String ipRange = "";
        switch (rule.getType()) {
            case IP:
                ipRange = rule.getFrom() + "/32";
                break;
            case NODE_ID:
                ipRange = getNode(credentials, rule.getFrom()).getIp() + "/32";
                break;
            case ANY:
                ipRange = "0.0.0.0/0";
                break;
        }
        return new IpPermission().withFromPort(Integer.parseInt(rule.getPort())).withToPort(Integer.parseInt(rule.getPort())).withIpProtocol(rule.getProtocol()).withIpRanges(ipRange);
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
        AmazonRoute53 route53Client = AWS.route53(accessKey, secretKey);
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
            case hosted_zone:
                return route53Client.listHostedZones().getHostedZones().stream().map(z -> new ParameterValue().value(z.getName())).collect(Collectors.toList());
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

    private int getStorageSize(AmazonEC2 ec2, String vmId) {
        DescribeInstancesResult result =
                ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
        Instance instance = result.getReservations().get(0).getInstances().get(0);
        InstanceBlockDeviceMapping blockDeviceMapping = instance.getBlockDeviceMappings().get(0);
        String volumeId = blockDeviceMapping.getEbs().getVolumeId();
        //blockDeviceMapping.getEbs().getVolumeId().
        DescribeVolumesResult dvr = ec2.describeVolumes(new DescribeVolumesRequest().withVolumeIds(volumeId));
        return dvr.getVolumes().get(0).getSize();
    }

    private Instance getInstance(AmazonEC2 ec2, String vmId) {
        DescribeInstancesResult result =
                ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
        return result.getReservations().get(0).getInstances().get(0);
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
    public ExecutionDetails configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {

        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String vmId = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        AmazonRoute53 route53 = AWS.route53(accessKey, secretKey);

        SynchronousPoller poller = new SynchronousPoller();
        Instance instance = getInstance(ec2, vmId);

        String oldName = getInstanceName(instance);
        String newName = parameters.get(Parameter.name.name());
        if (newName != null && !newName.equals(oldName)) {
            setInstanceName(ec2, vmId, newName);
        }
        String oldHostedZone = getAWSHostedZone(route53, instance.getPrivateIpAddress(), oldName);
        String newHostedZone = parameters.get(Parameter.hosted_zone.name());
        if (!StringUtils.equals(oldName, newName) ||
                !StringUtils.equals(oldHostedZone, newHostedZone)) {
            if (oldHostedZone != null) {
                deleteDnsRecord(route53, oldName, oldHostedZone);
                NodeUtil.logInfo(details, "Deleted DNS record for " + oldName + oldHostedZone);
            }
            createDnsRecord(route53, instance.getPrivateIpAddress(), newName, newHostedZone);
            NodeUtil.logInfo(details, "Created DNS record for " + newName + newHostedZone + " with IP: " + instance.getPrivateIpAddress());
        }

        //createTags(instanceId, after, amazonEC2);

        String newInstanceType = parameters.get(Parameter.instance_type.name());

        String newStorage = parameters.get(Parameter.storage.name());
        boolean needUpdateInstanceType = newInstanceType != null && !StringUtils.equals(instance.getInstanceType(), newInstanceType);

        boolean needResizeStorage = newStorage != null && getStorageSize(ec2, vmId) != Integer.parseInt(newStorage);
        if (needUpdateInstanceType || needResizeStorage) {
            log.info("Stopping AWS instance " + vmId + "(" + newName + ")");
            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(vmId);
            ec2.stopInstances(stopInstancesRequest);

            Callable<DescribeInstancesResult> poll =
                    () -> ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
            Predicate<DescribeInstancesResult> check =
                    (stopResult) -> "stopped".equals(stopResult.getReservations().get(0).getInstances().get(0).getState().getName());
            poller.poll(poll, check, 1, 600, "wait for instance " + vmId + " to stop.");
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

            Callable<DescribeSnapshotsResult> poll =
                    () -> ec2.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snapshotId));
            Predicate<DescribeSnapshotsResult> check =
                    (snapshotResult) -> "completed".equals(snapshotResult.getSnapshots().get(0).getState());
            poller.poll(poll, check, 1, 600, "wait for snapshot " + snapshotId + " to be completed.");

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest().withVolumeIds(volumeId);
            DescribeVolumesResult describeVolumesResult = ec2.describeVolumes(describeVolumesRequest);
            String availabilityZone = describeVolumesResult.getVolumes().get(0).getAvailabilityZone();

            log.info("Creating new volume " + volumeId + " in availability zone " + availabilityZone + " from snapshot " + snapshotId);
            CreateVolumeRequest createVolumeRequest =
                    new CreateVolumeRequest(snapshotId, availabilityZone).withSize(Integer.parseInt(newStorage));
            CreateVolumeResult createVolumeResult = ec2.createVolume(createVolumeRequest);
            String newVolumeId = createVolumeResult.getVolume().getVolumeId();

            log.info("New volume " + newVolumeId + " was created from snapshot " + snapshotId);

            Callable<DescribeVolumesResult> pollVol =
                    () -> ec2.describeVolumes(new DescribeVolumesRequest().withVolumeIds(newVolumeId));
            Predicate<DescribeVolumesResult> checkVol =
                    (volResult) -> "available".equals(volResult.getVolumes().get(0).getState());
            poller.poll(pollVol, checkVol, 1, 600, "wait for volume " + newVolumeId + " to become available.");

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

            Callable<DescribeInstancesResult> poll =
                    () -> ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(vmId));
            Predicate<DescribeInstancesResult> check =
                    (describeResult) -> "running".equals(describeResult.getReservations().get(0).getInstances().get(0).getState().getName());
            poller.poll(
                    poll, check,
                    1, 600, 20,
                    "wait for instance " + vmId + " to become ready."
            );
        }

        return details.status(ExecutionDetails.StatusEnum.OK);
    }

    private void createDnsRecord(AmazonRoute53 route53, String ip, String name, String hostedZoneName) {
        log.info("Creating route53 record for " + name);

        HostedZone hostedZone = getHostedZone(hostedZoneName, route53);

        ResourceRecord resourceRecord = new ResourceRecord(ip);
        ResourceRecordSet resourceRecordSet = new ResourceRecordSet(name + "." + hostedZoneName, RRType.A).withTTL
                (60L).withResourceRecords(resourceRecord);
        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(new Change(ChangeAction.CREATE,
                resourceRecordSet)));
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZone.getId()).withChangeBatch(changeBatch);
        guardedChangeResourceRecordSets(route53, changeResourceRecordSetsRequest);
    }

    private String getAWSHostedZone(AmazonRoute53 route53, String ip, String name) {
        List<HostedZone> zones = route53.listHostedZones().getHostedZones().stream().filter(
                z -> {
                    String recordSetName = name + "." + z.getName();
                    List<ResourceRecordSet> records = route53.listResourceRecordSets(new ListResourceRecordSetsRequest(z.getId()).withStartRecordName(recordSetName)).getResourceRecordSets();
                    return records.stream().anyMatch(r -> r.getResourceRecords().stream().anyMatch(rr -> rr.getValue().equals(ip)));
                }
        ).collect(Collectors.toList());
        if (zones == null || zones.size() == 0) {
            return null;
        }
        return zones.get(0).getName();
    }

    private HostedZone getHostedZone(String awsHostedZone, AmazonRoute53 route53) {
        ListHostedZonesResult listHostedZonesResult = route53.listHostedZones();
        HostedZone hostedZone = listHostedZonesResult.getHostedZones().stream()
                .filter(z -> z.getName().equals(awsHostedZone))
                .findAny()
                .orElse(null);
        return hostedZone;
    }

    private void deleteDnsRecord(AmazonRoute53 route53, String name, String hostedZoneName) {
        log.info("Deleting route53 record for " + name);
        if (StringUtils.isEmpty(name)) {
            log.error("Cannot delete route53 record - name is empty.");
            return;
        }
        HostedZone hostedZone = getHostedZone(hostedZoneName, route53);

        String recordSetName = name + "." + hostedZoneName;
        ListResourceRecordSetsRequest listResourceRecordSetsRequest =
                new ListResourceRecordSetsRequest(hostedZone.getId()).withStartRecordName(recordSetName);
        ListResourceRecordSetsResult listResourceRecordSetsResult = route53.listResourceRecordSets
                (listResourceRecordSetsRequest);
        if (listResourceRecordSetsResult.getResourceRecordSets().size() < 1) {
            log.error("Cannot delete route53 record - record not found. Skipping this step.");
            return;
        }

        ResourceRecordSet resourceRecordSet = listResourceRecordSetsResult.getResourceRecordSets().get(0);
        if (!recordSetName.equals(resourceRecordSet.getName())) {
            log.error("Cannot delete route53 record - record not found. Skipping this step.");
            return;
        }

        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(new Change(ChangeAction.DELETE,
                resourceRecordSet)));
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZone.getId()).withChangeBatch(changeBatch);
        ChangeResourceRecordSetsResult changeResourceRecordSetsResult =
                guardedChangeResourceRecordSets(route53, changeResourceRecordSetsRequest);
        log.info("Submitted delete recordset request " + changeResourceRecordSetsResult.getChangeInfo().toString());
    }

    private ChangeResourceRecordSetsResult guardedChangeResourceRecordSets(
            AmazonRoute53 route53,
            ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest
    ) {
        Callable<ChangeResourceRecordSetsResult> poll =
                () -> {
                    try {
                        return route53.changeResourceRecordSets(changeResourceRecordSetsRequest);
                    } catch (PriorRequestNotCompleteException e) {
                        log.warn("Route 53 request not completed, retrying...", e);
                        return null;
                    }
                };
        Predicate<ChangeResourceRecordSetsResult> check = Objects::nonNull;
        return new SynchronousPoller().poll(poll, check, 1, 600, "wait for route53 request to be submitted");
    }

    @Override
    public ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        AmazonEC2 ec2 = AWS.ec2(accessKey, secretKey, region);
        AmazonRoute53 route53 = AWS.route53(accessKey, secretKey);
        Instance instance = getInstance(ec2, id);
        String vpcId = instance.getVpcId();
        String name = getInstanceName(instance);
        String hostedZone = getAWSHostedZone(route53, instance.getPrivateIpAddress(), name);
        if (!Strings.isNullOrEmpty(hostedZone)) {
            deleteDnsRecord(route53, name, getAWSHostedZone(route53, instance.getPrivateIpAddress(), name));
            NodeUtil.logInfo(details, "Deleted dns record for : " + name + hostedZone);
        }

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
        List<FirewallRule> rules = permissions.stream().flatMap(p -> fromIpPermission(p).stream()).collect(Collectors.toList());
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
        List<IpPermission> permissions = firewallUpdate.getCreate().stream().map(p -> {
            try {
                return fromFirewallRule(credentials, p);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
        if (!permissions.isEmpty()) {
            AuthorizeSecurityGroupIngressRequest req = new AuthorizeSecurityGroupIngressRequest()
                    .withGroupId(securityGroupId).withIpPermissions(permissions);
            ec2.authorizeSecurityGroupIngress(req);
            NodeUtil.logInfo(details, "Firewall rules created:");
            NodeUtil.logInfo(details, firewallUpdate.getCreate().toString());
        }
        List<IpPermission> permissionsToDelete = firewallUpdate.getDelete().stream().map(p -> {
            try {
                return fromFirewallRule(credentials, p);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
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
        name("Node name", true, true, true, null, false, false),
        region("AWS region", true, false, true, null, true, true),
        subnet("AWS subnet", true, true, true, null, true, true, region),
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

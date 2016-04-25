/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.service.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.AWSInfrastructureDeployer;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.execution.SynchronousPoller;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static org.ecloudmanager.deployment.vm.infrastructure.AWSInfrastructureDeployer.*;

public class AWSVmService {
    @Inject
    SynchronousPoller synchronousPoller;
    @Inject
    private Logger log;
    @Inject
    private AWSClientService awsClientService;

    public String createSecurityGroup(VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
        log.info("Creating AWS security group " + name);

        String regionStr = getAwsRegion(vmDeployment);

        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(regionStr));
        DescribeSubnetsResult describeSubnetsResult = amazonEC2.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(getAwsSubnet(vmDeployment)));
        String vpcId = describeSubnetsResult.getSubnets().get(0).getVpcId();
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(name, "Created by ecloud manager").withVpcId(vpcId);
        CreateSecurityGroupResult createSecurityGroupResult = amazonEC2.createSecurityGroup(createSecurityGroupRequest);
        String securityGroupId = createSecurityGroupResult.getGroupId();
        log.info("Security group created: " + securityGroupId + " name: " + name);

        AuthorizeSecurityGroupIngressRequest sshRule = new AuthorizeSecurityGroupIngressRequest().withGroupId(securityGroupId).withFromPort(22).withToPort(22).withIpProtocol("TCP").withCidrIp("0.0.0.0/0");
        amazonEC2.authorizeSecurityGroupIngress(sshRule);
        return securityGroupId;
    }

    public void createFirewallRules(VMDeployment deployment) {
        ApplicationDeployment ad = (ApplicationDeployment) deployment.getTop();
        String securityGroupId = AWSInfrastructureDeployer.getAwsSecurityGroupId(deployment);
        if (deployment.getTop() == deployment.getParent()) {
            // TODO move public IP firewall rules creation to ApplicationDeployment?
            deployment.children(Endpoint.class).forEach(e -> {
                if (ad.getPublicEndpoints().contains(deployment.getName() + ":" + e.getName())) {
                    int port = Integer.parseInt(e.getConfigValue("port"));
                    AuthorizeSecurityGroupIngressRequest rule = new AuthorizeSecurityGroupIngressRequest()
                            .withGroupId(securityGroupId)
                            .withFromPort(port).withToPort(port)
                            .withIpProtocol("TCP")
                            .withCidrIp("0.0.0.0/0");
                    getAmazonEC2(deployment).authorizeSecurityGroupIngress(rule);
                }
            });
        } else {
            // TODO move haproxy IP firewall rules creation to ComponentGroupDeployment?
            String haproxyIp = deployment.getParent().getParent().getConfigValue(HAProxyDeployer.HAPROXY_IP);
            deployment.getVirtualMachineTemplate().getEndpoints().forEach(e -> {
                if (e.getPort() != null) {
                    int port = e.getPort();
                    AuthorizeSecurityGroupIngressRequest rule = new AuthorizeSecurityGroupIngressRequest()
                            .withGroupId(securityGroupId)
                            .withFromPort(port).withToPort(port)
                            .withIpProtocol("TCP")
                            .withCidrIp(haproxyIp + "/32");
                    getAmazonEC2(deployment).authorizeSecurityGroupIngress(rule);
                }
            });
        }

        List<Endpoint> required = deployment.getRequiredEndpoints();
        required.forEach(e -> {
            int port = Integer.parseInt(e.getConfigValue("port"));
            DeploymentObject d = e.getParent();
            if (d instanceof VMDeployment) {
                VMDeployment supplier = (VMDeployment) d;
                String supplierSecurityGroupId = AWSInfrastructureDeployer.getAwsSecurityGroupId(supplier);

                AuthorizeSecurityGroupIngressRequest inRule = new AuthorizeSecurityGroupIngressRequest()
                        .withGroupId(supplierSecurityGroupId)
                        .withFromPort(port).withToPort(port)
                        .withIpProtocol("TCP")
                        .withCidrIp(InfrastructureDeployer.getIP(deployment) + "/32");
                getAmazonEC2(supplier).authorizeSecurityGroupIngress(inRule);
//                    AuthorizeSecurityGroupEgressRequest outRule = new AuthorizeSecurityGroupEgressRequest()
//                            .withGroupId(securityGroupId)
//                            .withFromPort(port).withToPort(port)
//                            .withIpProtocol("TCP")
//                            .withCidrIp(InfrastructureDeployer.getIP(supplier) + "/32");
//                    getAmazonEC2(deployment).authorizeSecurityGroupEgress(outRule);
            } else {
                log.warn("Unsupported endpoint:" + d);
            }
        });
    }

    public Instance createVm(VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
        log.info("Creating AWS instance " + name);

        String regionStr = getAwsRegion(vmDeployment);

        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(regionStr));

        int storage = vmDeployment.getVirtualMachineTemplate().getStorage();
        // Set 'delete on termination' and size
        BlockDeviceMapping mapping = new BlockDeviceMapping().withDeviceName("/dev/sda1").withEbs(new EbsBlockDevice
            ().withDeleteOnTermination(true).withVolumeSize(storage));

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
            .withMaxCount(1)
            .withMinCount(1)
            .withSubnetId(getAwsSubnet(vmDeployment))
            .withImageId(getAwsAmi(vmDeployment))
            .withInstanceType(getAwsInstanceType(vmDeployment))
            .withKeyName(getAwsKeypair(vmDeployment))
            .withBlockDeviceMappings(mapping);

        String securityGroup = getAwsSecurityGroupId(vmDeployment);
        if (securityGroup != null) {
            runInstancesRequest = runInstancesRequest.withSecurityGroupIds(securityGroup);
        }
        runInstancesRequest = runInstancesRequest.withSecurityGroupIds(securityGroup);

        RunInstancesResult runInstancesResult = amazonEC2.runInstances(runInstancesRequest);
        Instance inst = runInstancesResult.getReservation().getInstances().get(0);

        Callable<DescribeInstancesResult> poll = () -> amazonEC2.describeInstances(new DescribeInstancesRequest()
            .withInstanceIds(inst.getInstanceId()));
        Predicate<DescribeInstancesResult> check =
            (result) -> "running".equals(result.getReservations().get(0).getInstances().get(0).getState().getName());
        DescribeInstancesResult result = synchronousPoller.poll(
                poll, check,
                1, 600, 20,
                "wait for instance " + inst.getInstanceId() + " to become ready."
            );

        // Instance with IP address assigned
        Instance instance = result.getReservations().get(0).getInstances().get(0);
        log.info("AWS instance " + name + " created with IP " + instance.getPrivateIpAddress());

        String instanceId = instance.getInstanceId();

        createTags(instanceId, vmDeployment, amazonEC2);

        createDnsRecord(instance, vmDeployment);

        return instance;
    }

    private void createTags(String instanceId, VMDeployment vmDeployment, AmazonEC2 amazonEC2) {
        String userId;
        try {
            userId = awsClientService.getIamClient().getUser().getUser().getUserId();
        } catch (AmazonServiceException e) {
            userId = e.getErrorMessage().replaceAll("^[^/]*/", "").replaceAll(" is not authorized .*", "");
        }

        String vmName = vmDeployment.getConfigValue(VMDeployer.VM_NAME);

        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Name", vmName));
        tags.add(new Tag("Creator", userId));
        tags.add(new Tag("Group", getGroupAwsTag(vmDeployment)));
        tags.add(new Tag("Cost Center", getCostCenterAwsTag(vmDeployment)));
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
            .withResources(instanceId)
            .withTags(tags);

        log.info("Updating tags of AWS instance " + vmName);
        amazonEC2.createTags(createTagsRequest);
    }

    private void createDnsRecord(Instance instance, VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
        log.info("Creating route53 record for " + name);
        AmazonRoute53 route53 = awsClientService.getRoute53Client();

        String awsHostedZone = getAwsHostedZone(vmDeployment);
        HostedZone hostedZone = getHostedZone(awsHostedZone, route53);

        ResourceRecord resourceRecord = new ResourceRecord(instance.getPrivateIpAddress());
        ResourceRecordSet resourceRecordSet = new ResourceRecordSet(name + "." + awsHostedZone, RRType.A).withTTL
            (60L).withResourceRecords(resourceRecord);
        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(new Change(ChangeAction.CREATE,
            resourceRecordSet)));
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
            .withHostedZoneId(hostedZone.getId()).withChangeBatch(changeBatch);
        guardedChangeResourceRecordSets(route53, changeResourceRecordSetsRequest);
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
        return synchronousPoller.poll(poll, check, 1, 600, "wait for route53 request to be submitted");
    }

    private void deleteDnsRecord(VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);

        log.info("Deleting route53 record for " + name);
        if (StringUtils.isEmpty(name)) {
            log.error("Cannot delete route53 record - name is empty.");
            return;
        }

        AmazonRoute53 route53 = awsClientService.getRoute53Client();

        String awsHostedZone = getAwsHostedZone(vmDeployment);
        HostedZone hostedZone = getHostedZone(awsHostedZone, route53);

        String recordSetName = name + "." + awsHostedZone;
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

    @NotNull
    private HostedZone getHostedZone(String awsHostedZone, AmazonRoute53 route53) {
        ListHostedZonesResult listHostedZonesResult = route53.listHostedZones();
        HostedZone hostedZone = listHostedZonesResult.getHostedZones().stream()
            .filter(z -> z.getName().equals(awsHostedZone))
            .findAny()
            .orElse(null);
        if (hostedZone == null) {
            throw new ActionException("Create route53 record failed - cannot find hosted zone for name: " +
                awsHostedZone);
        }
        return hostedZone;
    }

    public void deleteVm(VMDeployment vmDeployment) {
        String instanceId = InfrastructureDeployer.getVmId(vmDeployment);
        if (instanceId == null) {
            log.info("VM instance ID is null, nothing to delete");
            return;
        }
        String regionStr = getAwsRegion(vmDeployment);

        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(regionStr));

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds
            (instanceId);
        amazonEC2.terminateInstances(terminateInstancesRequest);

        Callable<DescribeInstancesResult> poll =
            () -> amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
        Predicate<DescribeInstancesResult> check =
            (result) -> "terminated".equals(result.getReservations().get(0).getInstances().get(0).getState().getName());
        synchronousPoller.poll(poll, check, 1, 600, "wait for instance " + instanceId + " to terminate.");

        deleteDnsRecord(vmDeployment);
    }

    public void deleteSecurityGroup(VMDeployment vmDeployment) {
        String id = AWSInfrastructureDeployer.getAwsSecurityGroupId(vmDeployment);
        if (id == null) {
            log.info("Security Group ID is null, nothing to delete");
            return;
        }

//        boolean notFound = getAmazonEC2(vmDeployment).describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupIds(Arrays.asList(id))).getSecurityGroups().isEmpty();
//        if (notFound) {
//            log.info("Security Group with ID " + id + " not found");
//            return;
//        }
        DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest().withGroupId(id);
        try {
            getAmazonEC2(vmDeployment).deleteSecurityGroup(request);
            log.info("Security Group with ID " + id + " deleted");
        } catch (Exception e) {
            log.info("Delete Security Group with ID " + id + " failed", e);
        }
    }

    public void updateVm(VMDeployment before, VMDeployment after, String instanceId) {
        String oldName = before.getConfigValue(VMDeployer.VM_NAME);
        String newName = after.getConfigValue(VMDeployer.VM_NAME);

        String regionStr = getAwsRegion(after);

        AmazonEC2 amazonEC2 = awsClientService.getAmazonEC2(RegionUtils.getRegion(regionStr));

        createTags(instanceId, after, amazonEC2);

        log.info("Obtaining information for AWS instance " + instanceId + "(" + newName + ")");
        DescribeInstancesResult result =
                amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
        Instance instance = result.getReservations().get(0).getInstances().get(0);

        // Maybe need to set shutdown behavior to "stop" if it is "terminate"

        String newInstanceType = getAwsInstanceType(after);
        boolean needUpdateInstanceType = !StringUtils.equals(instance.getInstanceType(), newInstanceType);
        boolean needResizeStorage = before.getVirtualMachineTemplate().getStorage() != after.getVirtualMachineTemplate().getStorage();
        if (needUpdateInstanceType || needResizeStorage) {
            log.info("Stopping AWS instance " + instanceId + "(" + newName + ")");
            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);
            amazonEC2.stopInstances(stopInstancesRequest);

            Callable<DescribeInstancesResult> poll =
                    () -> amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
            Predicate<DescribeInstancesResult> check =
                    (stopResult) -> "stopped".equals(stopResult.getReservations().get(0).getInstances().get(0).getState().getName());
            synchronousPoller.poll(poll, check, 1, 600, "wait for instance " + instanceId + " to stop.");
        }

        if (needUpdateInstanceType) {
            log.info("Changing instance type of AWS instance " + instanceId + "(" + newName + ") to " + newInstanceType);
            ModifyInstanceAttributeRequest modifyInstanceAttributeRequest =
                    new ModifyInstanceAttributeRequest().withInstanceId(instanceId).withInstanceType(newInstanceType);
            amazonEC2.modifyInstanceAttribute(modifyInstanceAttributeRequest);
        }

        if (needResizeStorage) {
            log.info("Resizing root volume of AWS instance " + instanceId + "(" + newName + ")");
            InstanceBlockDeviceMapping blockDeviceMapping = instance.getBlockDeviceMappings().get(0);
            String volumeId = blockDeviceMapping.getEbs().getVolumeId();
            log.info("Creating snapshot of AWS volume " + volumeId + " attached to instance " + instanceId + "(" + newName + ")");
            CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest(volumeId, "Resize instance " + instanceId);
            CreateSnapshotResult createSnapshotResult = amazonEC2.createSnapshot(createSnapshotRequest);
            String snapshotId = createSnapshotResult.getSnapshot().getSnapshotId();

            Callable<DescribeSnapshotsResult> poll =
                    () -> amazonEC2.describeSnapshots(new DescribeSnapshotsRequest().withSnapshotIds(snapshotId));
            Predicate<DescribeSnapshotsResult> check =
                    (snapshotResult) -> "completed".equals(snapshotResult.getSnapshots().get(0).getState());
            synchronousPoller.poll(poll, check, 1, 600, "wait for snapshot " + snapshotId + " to be completed.");

            DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest().withVolumeIds(volumeId);
            DescribeVolumesResult describeVolumesResult = amazonEC2.describeVolumes(describeVolumesRequest);
            String availabilityZone = describeVolumesResult.getVolumes().get(0).getAvailabilityZone();

            log.info("Creating new volume " + volumeId + " in availability zone " + availabilityZone + " from snapshot " + snapshotId);
            CreateVolumeRequest createVolumeRequest =
                    new CreateVolumeRequest(snapshotId, availabilityZone).withSize(after.getVirtualMachineTemplate().getStorage());
            CreateVolumeResult createVolumeResult = amazonEC2.createVolume(createVolumeRequest);
            String newVolumeId = createVolumeResult.getVolume().getVolumeId();

            log.info("New volume " + newVolumeId + " was created from snapshot " + snapshotId);

            Callable<DescribeVolumesResult> pollVol =
                    () -> amazonEC2.describeVolumes(new DescribeVolumesRequest().withVolumeIds(newVolumeId));
            Predicate<DescribeVolumesResult> checkVol =
                    (volResult) -> "available".equals(volResult.getVolumes().get(0).getState());
            synchronousPoller.poll(pollVol, checkVol, 1, 600, "wait for volume " + newVolumeId + " to become available.");

            log.info("Detaching old volume " + volumeId + " from " + instanceId + "(" + newName + ")");
            amazonEC2.detachVolume(new DetachVolumeRequest(volumeId));

            log.info("Attaching new volume " + newVolumeId + " to instance " + instanceId + "(" + newName + ")");
            AttachVolumeRequest attachVolumeRequest = new AttachVolumeRequest(newVolumeId, instanceId, blockDeviceMapping.getDeviceName());
            amazonEC2.attachVolume(attachVolumeRequest);

            log.info("Deleting old volume " + volumeId);
            amazonEC2.deleteVolume(new DeleteVolumeRequest(volumeId));

            log.info("Deleting snapshot " + snapshotId);
            amazonEC2.deleteSnapshot(new DeleteSnapshotRequest(snapshotId));
        }

        if (needUpdateInstanceType || needResizeStorage) {
            log.info("Starting AWS instance " + instanceId + "(" + newName + ")");
            amazonEC2.startInstances(new StartInstancesRequest().withInstanceIds(instanceId));

            Callable<DescribeInstancesResult> poll =
                    () -> amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
            Predicate<DescribeInstancesResult> check =
                    (describeResult) -> "running".equals(describeResult.getReservations().get(0).getInstances().get(0).getState().getName());
            synchronousPoller.poll(
                    poll, check,
                    1, 600, 20,
                    "wait for instance " + instanceId + " to become ready."
            );
        }

        if (
            !StringUtils.equals(oldName, newName) ||
            !StringUtils.equals(getAwsHostedZone(before), getAwsHostedZone(after)) ||
            !StringUtils.equals(getIP(before), instance.getPrivateIpAddress()) ||
            needUpdateInstanceType
        ) {
            log.info("Recreate route53 record for AWS instance " + instanceId);
            deleteDnsRecord(before);
            createDnsRecord(instance, after);
        }
    }

    private AmazonEC2 getAmazonEC2(VMDeployment deployment) {
        return awsClientService.getAmazonEC2(RegionUtils.getRegion(getAwsRegion(deployment)));
    }
}

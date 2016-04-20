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

package org.ecloudmanager.deployment.vm.infrastructure;

import org.ecloudmanager.actions.AWSVmActions;
import org.ecloudmanager.deployment.core.*;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.service.aws.AWSInstanceType;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;

public class AWSInfrastructureDeployer extends InfrastructureDeployer {
    public static final String AWS_REGION = "awsRegion";
    public static final String AWS_SUBNET = "awsSubnet";
    public static final String AWS_TAGS = "awsTags";
    public static final String AWS_SECURITY_GROUP = "awsSecurityGroup";
    private static final String AWS_CONFIG_NAME = "aws";
    private static final String AWS_AMI = "awsAmi";
    private static final String AWS_INSTANCE_TYPE = "awsInstanceType";
    private static final String AWS_KEYPAIR = "awsKeypairName";
    private static final String GROUP_AWS_TAG = "groupAwsTag";
    private static final String COST_CENTER_AWS_TAG = "costCenterAwsTag";
    private static final String AWS_HOSTED_ZONE = "awsHostedZone";

    private AWSVmActions awsVmActions = CDI.current().select(AWSVmActions.class).get();

    private static DeploymentObject getAWSConfig(VMDeployment deployment) {
        return deployment.createIfMissingAndGetConfig(AWS_CONFIG_NAME);
    }

    public static String getAwsRegion(Config config) {
        return config.getConfigValue(AWS_REGION);
    }

    public static String getAwsRegion(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_REGION);
    }

    public static String getAwsSubnet(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_SUBNET);
    }

    public static String getAwsSubnet(Config deploymentConstraint) {
        return deploymentConstraint.getConfigValue(AWS_SUBNET);
    }

    public static String getAwsAmi(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_AMI);
    }

    public static String getAwsInstanceType(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_INSTANCE_TYPE);
    }

    public static String getAwsKeypair(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_KEYPAIR);
    }

    public static String getAwsSecurityGroupId(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_SECURITY_GROUP);
    }

    public static String getGroupAwsTag(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(GROUP_AWS_TAG);
    }

    public static String getCostCenterAwsTag(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(COST_CENTER_AWS_TAG);
    }

    public static String getAwsHostedZone(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_HOSTED_ZONE);
    }

    public static void addSecurityGroupId(VMDeployment deployment, String id) {
        getAWSConfig(deployment).addField(
                ConstraintField.builder()
                        .name(AWS_SECURITY_GROUP)
                        .description("AWS Security Group ID")
                        .readOnly(true)
                        .build()
        );
        getAWSConfig(deployment).setValue(AWS_SECURITY_GROUP, ConstraintValue.value(id));
    }

    public static void removeSecurityGroupId(VMDeployment deployment) {
        getAWSConfig(deployment).removeField(AWS_SECURITY_GROUP);
    }

    @Override
    public void specifyConstraints(VMDeployment vmDeployment) {
        DeploymentConstraint constraint = getAWSConfig(vmDeployment);
        constraint.addField(
                ConstraintField.builder()
                        .name(AWS_REGION)
                        .description("AWS Region")
                        .suggestionsProvider(AWSSuggestionsProviders.createRegionSuggestionsProvider())
                        .build()
            );
        constraint.addField(
                ConstraintField.builder()
                        .name(AWS_SUBNET)
                        .description("AWS Subnet")
                        .suggestionsProvider(AWSSuggestionsProviders.createSubnetSuggestionsProvider())
                        .build()
        );
        constraint.addField(
                ConstraintField.builder()
                        .name(AWS_AMI)
                        .description("AWS AMI")
                        //.suggestionsProvider(AWSSuggestionsProviders.createAmiSuggestionsProvider())
                        .build()
        );
        VirtualMachineTemplate vmTemplate = vmDeployment.getVirtualMachineTemplate();
        AWSInstanceType defaultInstanceType =
                AWSInstanceType.get(vmTemplate.getProcessorCount(), vmTemplate.getMemory());
        constraint.addField(ConstraintField.builder()
                .name(AWS_INSTANCE_TYPE)
                .description("AWS Instance Type")
                .defaultValue(defaultInstanceType.getInstanceTypeName())
                .suggestionsProvider(AWSSuggestionsProviders.createInstanceTypeSuggestionsProvider())
                .build()
            );
        constraint.addField(
                ConstraintField.builder()
                        .name(AWS_KEYPAIR)
                        .description("AWS Keypair Name")
                        .suggestionsProvider(AWSSuggestionsProviders.createKeypairSuggestionsProvider())
                        .build()
        );
        constraint.addField(
                ConstraintField.builder()
                        .name(AWS_HOSTED_ZONE)
                        .description("AWS Hosted Zone Name")
                        .suggestionsProvider(AWSSuggestionsProviders.createHostedZoneSuggestionsProvider())
                        .build()
        );
        constraint.addField(
                ConstraintField.builder()
                        .name(GROUP_AWS_TAG)
                        .description("Group AWS Tag")
                        .suggestionsProvider(AWSSuggestionsProviders.createTagSuggestionsProvider("Group"))
                        .build()
        );
        constraint.addField(
                ConstraintField.builder()
                        .name(COST_CENTER_AWS_TAG)
                        .description("Cost Center AWS Tag")
                        .suggestionsProvider(AWSSuggestionsProviders.createTagSuggestionsProvider("Cost Center"))
                        .build()
        );
//        constraint.addField(
//                ConstraintField.builder()
//                        .name(AWS_SECURITY_GROUP)
//                        .description("AWS Security Group Name")
//                        .suggestionsProvider(AWSSuggestionsProviders.createSecurityGroupSuggestionsProvider())
//                        .build()
//        );
        constraint.addOptionalField(AWS_TAGS, "AWS Tags");
    }

    @Override
    public Action getCreateAction(VMDeployment vmDeployment) {
        return awsVmActions.getCreateVmAction(vmDeployment);
    }

    @Override
    public Action getDeleteAction(VMDeployment vmDeployment) {
        return awsVmActions.getDeleteVmAction(vmDeployment);
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, VMDeployment before, VMDeployment after) {
        return awsVmActions.getUpdateVmAction(before, after);
    }

    @Override
    protected DeploymentObject getInfrastructureConfig(VMDeployment deployment) {
        return getAWSConfig(deployment);
    }

    @Override
    public boolean isRecreateActionRequired(VMDeployment before, VMDeployment after) {
        AWSInstanceType beforeInstanceType = AWSInstanceType.get(getAwsInstanceType(before));
        AWSInstanceType afterInstanceType = AWSInstanceType.get(getAwsInstanceType(after));
        if (beforeInstanceType == null || afterInstanceType == null || !afterInstanceType.isCompatible(beforeInstanceType)) {
            return true;
        }

        if (before.getVirtualMachineTemplate().getStorage() != after.getVirtualMachineTemplate().getStorage()) {
            if (
                !(beforeInstanceType.isEbs() && afterInstanceType.isEbs()) ||
                before.getVirtualMachineTemplate().getStorage() > after.getVirtualMachineTemplate().getStorage()
            ) {
                return true;
            }
        }

        return !getAwsRegion(after).equals(getAwsRegion(before)) ||
               !getAwsSubnet(after).equals(getAwsSubnet(before)) ||
               !getAwsAmi(after).equals(getAwsAmi(before)) ||
               !getAwsKeypair(after).equals(getAwsKeypair(before));
    }

}
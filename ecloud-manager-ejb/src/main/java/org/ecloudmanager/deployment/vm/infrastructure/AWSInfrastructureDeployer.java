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
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.service.aws.AWSInstanceType;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;

public class AWSInfrastructureDeployer extends InfrastructureDeployer {
    private static final String AWS_CONFIG_NAME = "aws";

    public static final String AWS_REGION = "awsRegion";
    public static final String AWS_SUBNET = "awsSubnet";
    public static final String AWS_TAGS = "awsTags";
    public static final String AWS_SECURITY_GROUP = "awsSecurityGroup";
    private static final String AWS_AMI = "awsAmi";
    private static final String AWS_INSTANCE_TYPE = "awsInstanceType";
    private static final String AWS_KEYPAIR = "awsKeypairName";
    private static final String GROUP_AWS_TAG = "groupAwsTag";
    private static final String COST_CENTER_AWS_TAG = "costCenterAwsTag";
    private static final String AWS_HOSTED_ZONE = "awsHostedZone";

    private AWSVmActions awsVmActions = CDI.current().select(AWSVmActions.class).get();

    @Override
    public void specifyConstraints(VMDeployment vmDeployment) {
        DeploymentConstraint constraint = getAWSConfig(vmDeployment);
        constraint.addField(AWS_REGION, "AWS Region");
        constraint.addField(AWS_SUBNET, "AWS Subnet");
        constraint.addField(AWS_AMI, "AWS AMI");
        VirtualMachineTemplate vmTemplate = vmDeployment.getVirtualMachineTemplate();
        AWSInstanceType defaultInstanceType =
                AWSInstanceType.get(vmTemplate.getProcessorCount(), vmTemplate.getMemory());
        constraint.addField(ConstraintField.builder()
                .name(AWS_INSTANCE_TYPE)
                .description("AWS Instance Type")
                .defaultValue(defaultInstanceType.getInstanceTypeName())
                .build()
            );
        constraint.addField(AWS_KEYPAIR, "AWS Keypair Name");
        constraint.addField(AWS_HOSTED_ZONE, "AWS Hosted Zone Name");
        constraint.addField(GROUP_AWS_TAG, "Group AWS Tag");
        constraint.addField(COST_CENTER_AWS_TAG, "Cost Center AWS Tag");
        constraint.addOptionalField(AWS_TAGS, "AWS Tags");
        constraint.addOptionalField(AWS_SECURITY_GROUP, "AWS Security Group Name");
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
        return null;
    }

    private static DeploymentObject getAWSConfig(VMDeployment deployment) {
        return deployment.createIfMissingAndGetConfig(AWS_CONFIG_NAME);
    }

    @Override
    protected DeploymentObject getInfrastructureConfig(VMDeployment deployment) {
        return getAWSConfig(deployment);
    }

    @Override
    public boolean isRecreateActionRequired(VMDeployment deployment, VMDeployment changeSet) {
        return false;
    }

    public static String getAwsRegion(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_REGION);
    }

    public static String getAwsSubnet(VMDeployment deployment) {
        return getAWSConfig(deployment).getConfigValue(AWS_SUBNET);
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

    public static String getAwsSecurityGroup(VMDeployment deployment) {
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
}
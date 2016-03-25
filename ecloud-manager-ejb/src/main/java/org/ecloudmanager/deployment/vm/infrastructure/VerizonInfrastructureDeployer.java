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

import org.ecloudmanager.actions.VerizonVmActions;
import org.ecloudmanager.deployment.core.Config;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;

public class VerizonInfrastructureDeployer extends InfrastructureDeployer {
    private static final String VERIZON_CONFIG_NAME = "verizon";
    private static final String VERIZON_ENVIRONMENT = "verizonEnvironment";
    private static final String VERIZON_CATALOG = "verizonCatalog";
    private static final String VERIZON_SUBNET = "verizonSubnet";
    private static final String VERIZON_GROUP = "verizonGroup";
    private static final String VERIZON_ROW = "verizonRow";
    private static final String VERIZON_TAGS = "verizonTags";
    private static final String VERIZON_IPS = "verizonIPs";

    private VerizonVmActions verizonVmActions = CDI.current().select(VerizonVmActions.class).get();

    public VerizonInfrastructureDeployer() {
    }

    @Override
    public void specifyConstraints(VMDeployment vmDeployment) {
        DeploymentConstraint constraint = getVerizonConfig(vmDeployment);

        constraint.addField(ConstraintField.builder()
            .name(VERIZON_ENVIRONMENT)
            .description("Verizon Environment")
            .suggestionsProvider(VerizonSuggestionsProviders.createEnvironmentSuggestionsProvider())
            .build()
        );
        constraint.addField(ConstraintField.builder()
            .name(VERIZON_SUBNET)
            .description("Verizon Subnet")
            .suggestionsProvider(VerizonSuggestionsProviders.createNetworkSuggestionsProvider())
            .build()
        );
        constraint.addField(ConstraintField.builder()
            .name(VERIZON_ROW)
            .description("Verizon Row")
            .suggestionsProvider(VerizonSuggestionsProviders.createRowSuggestionsProvider())
            .build()
        );
        constraint.addField(ConstraintField.builder()
            .name(VERIZON_GROUP)
            .description("Verizon Group")
            .suggestionsProvider(VerizonSuggestionsProviders.createGroupSuggestionsProvider())
            .build()
        );
        constraint.addField(ConstraintField.builder()
            .name(VERIZON_CATALOG)
            .description("Verizon VM Catalog Entry")
            .suggestionsProvider(VerizonSuggestionsProviders.createCatalogSuggestionsProvider())
            .build()
        );
        constraint.addOptionalField(VERIZON_TAGS, "Verizon Tags");
        constraint.addOptionalField(VERIZON_IPS, "Verizon Public IPs");
    }

    @Override
    public Action getCreateAction(VMDeployment vmDeployment) {
        Action createVmAction = verizonVmActions.getCreateVmAction(vmDeployment);
        Action startupVmAction = verizonVmActions.getStartupVmAction(vmDeployment);
        Action detectVmIpAddressAction = verizonVmActions.getDetectVmIpAddressAction(vmDeployment);
        return Action.actionSequence(
            "Create, startup VM and detect IP",
            createVmAction, startupVmAction, detectVmIpAddressAction
        );
    }

    @Override
    public Action getDeleteAction(VMDeployment vmDeployment) {
        Action shutdownVmAction = verizonVmActions.getShutdownVmAction(vmDeployment);
        Action deleteVmAction = verizonVmActions.getDeleteVmAction(vmDeployment);
        return Action.actionSequence(
            "Shutdown and delete VM",
            shutdownVmAction, deleteVmAction
        );
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, VMDeployment before, VMDeployment after) {
        return verizonVmActions.getUpdateVmAction(after);
    }

    private static DeploymentObject getVerizonConfig(VMDeployment deployment) {
        return deployment.createIfMissingAndGetConfig(VERIZON_CONFIG_NAME);
    }

    public static String getCatalog(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_CATALOG);
    }

    public static String getEnvironment(Config config) {
        return config.getConfigValue(VERIZON_ENVIRONMENT);
    }

    public static String getEnvironment(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_ENVIRONMENT);
    }

    public static String getSubnet(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_SUBNET);
    }

    public static String getGroup(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_GROUP);
    }

    public static String getRow(Config config) {
        return config.getConfigValue(VerizonInfrastructureDeployer.VERIZON_ROW);
    }

    public static String getRow(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_ROW);
    }

    public static String getTags(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_TAGS);
    }

    public static String getIps(VMDeployment deployment) {
        return getVerizonConfig(deployment).getConfigValue(VERIZON_IPS);
    }

    @Override
    protected DeploymentObject getInfrastructureConfig(VMDeployment deployment) {
        return getVerizonConfig(deployment);
    }

    @Override
    public boolean isRecreateActionRequired(VMDeployment before, VMDeployment after) {
        return !getEnvironment(before).equals(getEnvironment(after)) ||
            !getSubnet(before).equals(getSubnet(after)) ||
            !getCatalog(before).equals(getCatalog(after));
    }
}
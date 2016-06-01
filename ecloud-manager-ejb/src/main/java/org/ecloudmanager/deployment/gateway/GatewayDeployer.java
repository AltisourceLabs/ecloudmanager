/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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

package org.ecloudmanager.deployment.gateway;

import org.ecloudmanager.deployment.core.AbstractDeployer;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.ConstraintValue;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.ChefProvisioningDeployer;
import org.ecloudmanager.service.execution.Action;
import org.jetbrains.annotations.NotNull;

public class GatewayDeployer extends AbstractDeployer<GatewayDeployment> {
    public static final String ETCD_ADDRESS = "etcdAddress";
    public static final String ETCD_PATH = "etcdPath";
    private static final String ETCD_CONFIG_NAME = "etcd";

    @Override
    public Action getBeforeChildrenCreatedAction(GatewayDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenCreatedAction(GatewayDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenDeletedAction(GatewayDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenDeletedAction(GatewayDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenUpdatedAction(GatewayDeployment before, GatewayDeployment after) {
        return null;
    }

    @Override
    public Action getAfterChildrenUpdatedAction(GatewayDeployment before, GatewayDeployment after) {
        return null;
    }

    @Override
    public void specifyConstraints(GatewayDeployment deployment) {
        @NotNull DeploymentObject etcdConfig = getEtcdConfig(deployment);
        etcdConfig.addField(ConstraintField.builder()
                .name(ETCD_ADDRESS)
                .description("ETCD address (e.g. http://127.0.0.1:2379)")
                .build()
            );
        etcdConfig.addField(ConstraintField.builder()
                .name(ETCD_PATH)
                .description("Path to HAProxy configuration in ETCD")
                .defaultValue("/gateways/" + deployment.getName())
                .build()
            );

        deployment.children(GatewayVMDeployment.class).forEach(gatewayVMDeployment -> {
            DeploymentObject nodeConfig = ChefProvisioningDeployer.getNodeConfig(gatewayVMDeployment);
            nodeConfig.setValue("node_name", ConstraintValue.reference("/" + gatewayVMDeployment.getName() + "/vmName"));
            nodeConfig.setValue(GatewayVMDeployment.ETCD_NODE, ConstraintValue.reference("/" + ETCD_CONFIG_NAME + "/" + ETCD_ADDRESS));
            nodeConfig.setValue(GatewayVMDeployment.ETCD_PATH, ConstraintValue.reference("/" + ETCD_CONFIG_NAME + "/" + ETCD_PATH));
        });
    }

    @NotNull
    private static DeploymentObject getEtcdConfig(GatewayDeployment deployment) {
        return deployment.createIfMissingAndGetConfig(ETCD_CONFIG_NAME, "ETCD server configuration");
    }

    public static String getEtcdAddress(GatewayDeployment deployment) {
        return getEtcdConfig(deployment).getConfigValue(ETCD_ADDRESS);
    }
    public static String getEtcdPath(GatewayDeployment deployment) {
        return getEtcdConfig(deployment).getConfigValue(ETCD_PATH);
    }
}

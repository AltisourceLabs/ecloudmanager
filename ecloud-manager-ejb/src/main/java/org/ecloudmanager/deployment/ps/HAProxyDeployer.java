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

package org.ecloudmanager.deployment.ps;

import org.ecloudmanager.deployment.core.*;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.provisioning.HAProxyConfigurator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HAProxyDeployer extends AbstractDeployer<ProducedServiceDeployment> {
    public static final String PORT = "port";
    public static final String BIND_IP = "bind_ip";
    public static final String HAPROXY_IP = "ha_proxy_ip";
    public static final String HAPROXY_MONITORING = "ha_proxy_monitoring";
    private static final String ETCD_ADDRESS = "etcdAddress";
    private static final String ETCD_PATH = "etcdPath";
    private static final String ETCD_CONFIG_NAME = "etcd";

    private HAProxyConfigurator haProxyConfigurator;

    public static DeploymentObject getEtcdConfig(DeploymentObject deployment) {
        return deployment.getTop().getChildByName(ETCD_CONFIG_NAME);
    }

    private static DeploymentObject createEtcdConfig(DeploymentObject deployment) {
        DeploymentObject cfg = new Config(ETCD_CONFIG_NAME, "ETCD server configuration");
        deployment.getTop().addChild(cfg);
        return cfg;
    }

    private HAProxyConfigurator getConfigurator(DeploymentObject deployment) {
        if (haProxyConfigurator == null) {

            String etcdAddress = getEtcdConfig(deployment).getConfigValue(ETCD_ADDRESS);
            String etcdPath = getEtcdConfig(deployment).getConfigValue(ETCD_PATH);
            haProxyConfigurator = HAProxyConfigurator.create(etcdPath, etcdAddress);
        }
        return haProxyConfigurator;
    }

    public void configure(ProducedServiceDeployment serviceDeployment) {
        List<String> config = getFrontendConfigWithBind(serviceDeployment);
        getConfigurator(serviceDeployment).saveFrontend(serviceDeployment.getName(), config);
        for (ComponentGroupDeployment cg : serviceDeployment.getComponentGroups()) {
            configure(cg);
        }
    }

    @NotNull
    private List<String> getFrontendConfigWithBind(ProducedServiceDeployment serviceDeployment) {
        List<String> config = new ArrayList<>();

        String port = serviceDeployment.getConfigValue(PORT);
        String ip = serviceDeployment.getConfigValue(BIND_IP);


        config.add("bind " + ip + ":" + port);
        config.addAll(serviceDeployment.getHaProxyFrontendConfig().getConfig());
        return config;
    }

    public void configure(ComponentGroupDeployment componentGroupDeployment) {
        getConfigurator(componentGroupDeployment).saveBackend(componentGroupDeployment.getName(),
            componentGroupDeployment.getHaProxyBackendConfig().getConfig());
    }

    @Override
    public void specifyConstraints(ProducedServiceDeployment deployment) {
        deployment.addField(ConstraintField.builder().name(PORT).description("Service port").defaultValue("22").type
            (ConstraintField.Type.NUMBER).build());
        deployment.addField(ConstraintField.builder().name(BIND_IP).description("Bind IP").defaultValue("*").build());
        // TODO support multiple IP addresses??
        deployment.addField(ConstraintField.builder().name(HAPROXY_IP).description("HAProxy IP address").required(true).build());
        deployment.addField(ConstraintField.builder()
                .name(HAPROXY_MONITORING)
                .description("HAProxy Monitoring Enabled")
                .required(false)
                .readOnly(true)
                .allowReference(false)
                .build()
        );
        DeploymentConstraint etcdConfig = getEtcdConfig(deployment);
        if (etcdConfig == null) {
            etcdConfig = createEtcdConfig(deployment);
        }
        etcdConfig.addField(ConstraintField.builder().name(ETCD_ADDRESS).description("ETCD address").defaultValue
            ("http://127.0.0.1:2379").build());
        etcdConfig.addField(ConstraintField.builder().name(ETCD_PATH).description("Path to HAProxy configuration in " +
            "ETCD").defaultValue("/services/haproxy").build());
    }

    @Override
    public Action getBeforeChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return Action.single("Configure HAProxy frontend", () -> configure(deployable), deployable);
    }

    @Override
    public Action getAfterChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return Action.single("Delete HAProxy configuration", () -> {
            getConfigurator(deployable).deleteFrontend(deployable.getName());
            for (ComponentGroupDeployment cg : deployable.getComponentGroups()) {
                getConfigurator(deployable).deleteBackend(cg.getName());
            }
        }, deployable);
    }

    @Override
    public Action getBeforeChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        // Doesn't make any sense to check if update is needed as this operation is pretty fast
        return Action.actionSequence(
            "Update HAProxy configuration for " + after.getName(),
            getAfterChildrenDeletedAction(before),
            getBeforeChildrenCreatedAction(after)
        );
    }

    @Override
    public Action getAfterChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        return null;
    }

}

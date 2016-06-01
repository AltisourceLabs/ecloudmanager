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

import org.ecloudmanager.deployment.core.AbstractDeployer;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.gateway.GatewayDeployer;
import org.ecloudmanager.deployment.gateway.GatewayDeployment;
import org.ecloudmanager.deployment.gateway.GatewaySuggestionsProvider;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.repository.deployment.GatewayRepository;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.provisioning.HAProxyConfigurator;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;

public class HAProxyDeployer extends AbstractDeployer<ProducedServiceDeployment> {
    public static final String PORT = "port";
    public static final String BIND_IP = "bind_ip";
    public static final String HAPROXY_MONITORING = "ha_proxy_monitoring";
    public static final String GATEWAY = "gateway";

    private HAProxyConfigurator haProxyConfigurator;

    private HAProxyConfigurator getConfigurator(ProducedServiceDeployment deployment) {
        if (haProxyConfigurator == null) {
            GatewayDeployment gatewayDeployment = getGatewayDeployment(deployment);

            String etcdAddress = GatewayDeployer.getEtcdAddress(gatewayDeployment);
            String etcdPath = GatewayDeployer.getEtcdPath(gatewayDeployment);
            haProxyConfigurator = HAProxyConfigurator.create(etcdPath, etcdAddress);
        }
        return haProxyConfigurator;
    }

    private static GatewayDeployment getGatewayDeployment(ProducedServiceDeployment deployment) {
        String gateway = deployment.getConfigValue(GATEWAY);
        GatewayRepository gatewayRepository = CDI.current().select(GatewayRepository.class).get();
        return gatewayRepository.find(gateway);
    }

    public static String getHaproxyIp(ProducedServiceDeployment deployment) {
        GatewayDeployment gatewayDeployment = getGatewayDeployment(deployment);
        return gatewayDeployment == null ?
                null :
                InfrastructureDeployer.getIP(gatewayDeployment.children(GatewayVMDeployment.class).get(0));
    }

    private void configure(ProducedServiceDeployment serviceDeployment) {
        List<String> config = getFrontendConfigWithBind(serviceDeployment);
        HAProxyConfigurator configurator = getConfigurator(serviceDeployment);
        configurator.saveFrontend(serviceDeployment.getName(), config);
        for (ComponentGroupDeployment componentGroupDeployment : serviceDeployment.getComponentGroups()) {
            configurator.saveBackend(componentGroupDeployment.getName(), componentGroupDeployment.getHaProxyBackendConfig().getConfig());

            componentGroupDeployment.stream(VMDeployment.class).forEach(vmDeployment -> {
                String serverName = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
                String ip = InfrastructureDeployer.getIP(vmDeployment);
                @NotNull List<EndpointTemplate> endpoints = vmDeployment.getVirtualMachineTemplate().getEndpoints();
                // TODO - the user shuould be able to select an endpoint for the backend server when there's more then one in a runlist
                String port = endpoints.get(0).getPort().toString();
                configurator.saveServer(
                        componentGroupDeployment.getName(),
                        serverName,
                        ip,
                        port,
                        componentGroupDeployment.getHaProxyBackendConfig().getServerOptions()
                    );
            });
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

    @Override
    public void specifyConstraints(ProducedServiceDeployment deployment) {
        deployment.addField(ConstraintField.builder().name(PORT).description("Service port").type(ConstraintField.Type.NUMBER).build());
        deployment.addField(ConstraintField.builder().name(BIND_IP).description("Bind IP").defaultValue("*").build());
        // TODO support multiple IP addresses??
        deployment.addField(ConstraintField.builder()
                .name(GATEWAY)
                .description("HAProxy Gateway")
                .allowReference(false)
                .suggestionsProvider(new GatewaySuggestionsProvider())
                .build()
        );
        deployment.addField(ConstraintField.builder()
                .name(HAPROXY_MONITORING)
                .description("HAProxy Monitoring Enabled")
                .required(false)
                .readOnly(true)
                .allowReference(false)
                .build()
        );
    }

    @Override
    public Action getBeforeChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenCreatedAction(ProducedServiceDeployment deployable) {
        return Action.single("Configure HAProxy frontend", () -> configure(deployable), deployable);
    }

    @Override
    public Action getAfterChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return Action.single("Delete HAProxy configuration", () -> {
            HAProxyConfigurator configurator = getConfigurator(deployable);
            configurator.deleteFrontend(deployable.getName());
            for (ComponentGroupDeployment cg : deployable.getComponentGroups()) {
                configurator.deleteBackend(cg.getName());
                cg.stream(VMDeployment.class).forEach(vmDeployment -> {
                    String serverName = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
                    configurator.deleteServer(cg.getName(), serverName);
                });
            }
        }, deployable);
    }

    @Override
    public Action getBeforeChildrenDeletedAction(ProducedServiceDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        return null;
    }

    @Override
    public Action getAfterChildrenUpdatedAction(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        // Doesn't make any sense to check if update is needed as this operation is pretty fast
        return Action.actionSequence(
                "Update HAProxy configuration for " + after.getName(),
                getAfterChildrenDeletedAction(before),
                getAfterChildrenCreatedAction(after)
        );
    }

}

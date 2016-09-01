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

package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployerImpl;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.FirewallRule;
import org.ecloudmanager.node.model.FirewallUpdate;
import org.ecloudmanager.repository.deployment.LoggingEventRepository;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.execution.Action;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static org.ecloudmanager.node.LoggableFuture.waitFor;

@Service
public class HAProxyActions {
    @Inject
    NodeAPIConfigurationService nodeAPIProvider;
    @Inject
    @Named("contextExecutorService")
    ExecutorService executorService;
    @Inject
    private LoggingEventRepository loggingEventRepository;

    public Action getCreatePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        String actionId = Action.newId();
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(HAProxyActions.class, actionId);
        return Action.single("Create Firewall Rules for Public Endpoints",
                () -> {
                    ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
                    // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
                    // Create firewall rule for haproxy frontend if there's a public endpoint
                    producedServiceDeployment.children(Endpoint.class).forEach(endpoint -> {
                        int port = endpoint.getPort();
                        String publicEndpointName = producedServiceDeployment.getName() + ":" + endpoint.getName();
                        if (ad.getPublicEndpoints().contains(publicEndpointName)) {
                            GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
                            String vmId = InfrastructureDeployerImpl.getVmId(gatewayVmDeployment);
                            String apiId = ((ApplicationDeployment) (gatewayVmDeployment.getTop())).getInfrastructure();
                            FirewallRule rule = new FirewallRule().type(FirewallRule.TypeEnum.ANY).port(port).protocol("TCP");
                            AsyncNodeAPI nodeAPI = nodeAPIProvider.getAPI(apiId);
                            try {
                                waitFor(nodeAPI.updateNodeFirewallRules(nodeAPIProvider.getCredentials(apiId), vmId, new FirewallUpdate().create(Arrays.asList(rule))), actionLog);
                            } catch (Exception e) {
                                actionLog.error("Failed to create firewall rule on node " + vmId, e);
                            }
                        }
                    });
            return null;
                }, producedServiceDeployment, actionId);
    }

    public Action getDeletePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        String actionId = Action.newId();
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(HAProxyActions.class, actionId);
        return Action.single("Delete Firewall Rules for Public Endpoints", () -> {
            ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
            // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
            // Delete firewall rule for haproxy frontend if there's a public endpoint
            producedServiceDeployment.children(Endpoint.class).forEach(endpoint -> {
                int port = endpoint.getPort();
                String publicEndpointName = producedServiceDeployment.getName() + ":" + endpoint.getName();
                if (ad.getPublicEndpoints().contains(publicEndpointName)) {
                    GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
                    String vmId = InfrastructureDeployerImpl.getVmId(gatewayVmDeployment);
                    String apiId = ((ApplicationDeployment) (gatewayVmDeployment.getTop())).getInfrastructure();

                    AsyncNodeAPI nodeAPI = nodeAPIProvider.getAPI(apiId);
                    FirewallRule rule = new FirewallRule().type(FirewallRule.TypeEnum.ANY).port(port).protocol("TCP");
                    try {
                        waitFor(nodeAPI.updateNodeFirewallRules(nodeAPIProvider.getCredentials(apiId), vmId, new FirewallUpdate().delete(Arrays.asList(rule))), actionLog);
                    } catch (Exception e) {
                        actionLog.error("Failed to delete firewall rule on node " + vmId, e);
                    }
                }
            });
            return null;
        }, producedServiceDeployment, actionId);
    }

}

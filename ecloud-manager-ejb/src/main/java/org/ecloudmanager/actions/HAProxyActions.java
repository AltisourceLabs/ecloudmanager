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
import org.ecloudmanager.node.NodeAPI;
import org.ecloudmanager.node.model.FirewallRule;
import org.ecloudmanager.node.model.FirewallUpdate;
import org.ecloudmanager.service.NodeAPIProvider;
import org.ecloudmanager.service.execution.Action;

import javax.inject.Inject;
import java.util.Arrays;

@Service
public class HAProxyActions {
    //    @Inject
//    private AWSVmService vmService;
    @Inject
    NodeAPIProvider nodeAPIProvider;

    public Action getCreatePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        return Action.single("Create Firewall Rules for Public Endpoints", () -> {
            createFirewallRules(producedServiceDeployment);
            return null;
        }, producedServiceDeployment);
    }

    public Action getDeletePublicEndpointFirewallRulesAction(ProducedServiceDeployment producedServiceDeployment) {
        return Action.single("Delete Firewall Rules for Public Endpoints", () -> {
            deleteFirewallRules(producedServiceDeployment);
            return null;
        }, producedServiceDeployment);
    }

    public void deleteFirewallRules(ProducedServiceDeployment producedServiceDeployment) throws Exception {
        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
        // Delete firewall rule for haproxy frontend if there's a public endpoint
        producedServiceDeployment.children(Endpoint.class).forEach(endpoint -> {
            int port = Integer.parseInt(endpoint.getConfigValue("port"));
            String publicEndpointName = producedServiceDeployment.getName() + ":" + endpoint.getName();
            if (ad.getPublicEndpoints().contains(publicEndpointName)) {
                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
                String vmId = InfrastructureDeployerImpl.getVmId(gatewayVmDeployment);
                String apiId = ((ApplicationDeployment) (gatewayVmDeployment.getTop())).getInfrastructure();

                NodeAPI nodeAPI = nodeAPIProvider.getAPI(apiId);
                FirewallRule rule = new FirewallRule().port(Integer.toString(port)).protocol("TCP").from(Arrays.asList("0.0.0.0/0"));

                try {
                    nodeAPI.updateNodeFirewallRules(nodeAPIProvider.getCredentials(apiId), vmId, new FirewallUpdate().delete(Arrays.asList(rule)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void createFirewallRules(ProducedServiceDeployment producedServiceDeployment) {
        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
        // Create firewall rule for haproxy frontend if there's a public endpoint
        producedServiceDeployment.children(Endpoint.class).forEach(e -> {
            int port = Integer.parseInt(e.getConfigValue("port"));
            String publicEndpointName = producedServiceDeployment.getName() + ":" + e.getName();
            if (ad.getPublicEndpoints().contains(publicEndpointName)) {

                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
                String vmId = InfrastructureDeployerImpl.getVmId(gatewayVmDeployment);
                String apiId = ((ApplicationDeployment) (gatewayVmDeployment.getTop())).getInfrastructure();
                FirewallRule rule = new FirewallRule().port(Integer.toString(port)).protocol("TCP").from(Arrays.asList("0.0.0.0/0"));
                NodeAPI nodeAPI = nodeAPIProvider.getAPI(apiId);

                try {
                    nodeAPI.updateNodeFirewallRules(nodeAPIProvider.getCredentials(apiId), vmId, new FirewallUpdate().delete(Arrays.asList(rule)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}

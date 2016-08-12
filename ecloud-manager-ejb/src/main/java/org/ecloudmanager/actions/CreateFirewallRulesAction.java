package org.ecloudmanager.actions;

import org.apache.commons.lang3.tuple.Pair;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.node.NodeAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.FirewallRule;
import org.ecloudmanager.node.model.FirewallUpdate;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CreateFirewallRulesAction extends SingleAction {
    private CreateFirewallRulesAction() {
        super();
    }

    public CreateFirewallRulesAction(VMDeployment deployment, NodeAPI nodeAPI, Credentials credentials) {
        super(null, "Create Firewall Rules", deployment);
        setCallable(() -> {
            ApplicationDeployment ad = (ApplicationDeployment) deployment.getTop();
            List<FirewallRule> rules = new ArrayList<FirewallRule>();
            if (deployment.getTop() == deployment.getParent()) {
                // TODO move public IP firewall rules creation to ApplicationDeployment?
                deployment.getEndpoints().forEach(e -> {
                    if (ad.getPublicEndpoints().contains(deployment.getName() + ":" + e.getName())) {
                        rules.add(new FirewallRule().type(FirewallRule.TypeEnum.ANY).port(e.getConfigValue("port")).protocol("TCP"));
                    }
                });
            } else {
                // TODO move haproxy IP firewall rules creation to ComponentGroupDeployment?
                ProducedServiceDeployment producedServiceDeployment = (ProducedServiceDeployment) deployment.getParent().getParent();
                String haproxyId = HAProxyDeployer.getHaproxyNodeId(producedServiceDeployment);

                deployment.getEndpoints().forEach(e -> {
                    if (e.getPort() != null) {
                        int port = e.getPort();
                        rules.add(new FirewallRule().type(FirewallRule.TypeEnum.NODE_ID).port(Integer.toString(port)).protocol("TCP").from(haproxyId));
                    }
                });
            }
            nodeAPI.updateNodeFirewallRules(credentials, InfrastructureDeployer.getVmId(deployment), new FirewallUpdate().create(rules));
            List<Pair<DeploymentObject, Endpoint>> required = deployment.getLinkedRequiredEndpoints();
            required.forEach(e -> {
                DeploymentObject d = e.getLeft();
                if (d instanceof VMDeployment) {
                    // FIXME should be moved to 'd' vm creation?
                    VMDeployment supplier = (VMDeployment) d;
                    FirewallRule rule = new FirewallRule().type(FirewallRule.TypeEnum.NODE_ID).port(Integer.toString(e.getRight().getPort())).protocol("TCP").from(InfrastructureDeployer.getVmId(deployment));
                    try {
                        nodeAPI.updateNodeFirewallRules(credentials, InfrastructureDeployer.getVmId(supplier), new FirewallUpdate().create(Arrays.asList(rule)));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

//                    AuthorizeSecurityGroupEgressRequest outRule = new AuthorizeSecurityGroupEgressRequest()
//                            .withGroupId(securityGroupId)
//                            .withFromPort(port).withToPort(port)
//                            .withIpProtocol("TCP")
//                            .withCidrIp(InfrastructureDeployer.getIP(supplier) + "/32");
//                    getAmazonEC2(deployment).authorizeSecurityGroupEgress(outRule);
                } else {
                    throw new RuntimeException("Unsupported endpoint:" + d);
                }
            });
            return null;
        });

    }


    @Override
    public List<Action> getDependencies(Action fullAction) {
        List<Action> result = new ArrayList<>();
        VMDeployment deployment = (VMDeployment) getDeployable();
        List<Pair<DeploymentObject, Endpoint>> required = deployment.getLinkedRequiredEndpoints();
        required.forEach(e -> {
            DeploymentObject d = e.getLeft();
            if (d instanceof VMDeployment) {
                VMDeployment supplier = (VMDeployment) d;
                Optional<SingleAction> createVMAction = fullAction.stream(SingleAction.class).filter(singleAction -> singleAction.getDeployable().equals(supplier))
                        .filter(singleAction -> VmActions.CREATE_VM.equals(singleAction.getDescription())).findAny();
                if (createVMAction.isPresent()) {
                    result.add(createVMAction.get());
                }
            } else {
                // TODO implement managed HAProxy
            }
        });
        result.addAll(super.getDependencies(fullAction));
        return result;

    }
}

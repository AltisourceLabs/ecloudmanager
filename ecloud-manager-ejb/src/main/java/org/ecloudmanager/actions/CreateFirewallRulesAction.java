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
import org.ecloudmanager.node.model.ExecutionDetails;
import org.ecloudmanager.node.model.FirewallRule;
import org.ecloudmanager.node.model.FirewallUpdate;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;

import java.util.ArrayList;
import java.util.Collections;
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
                        rules.add(new FirewallRule().type(FirewallRule.TypeEnum.ANY).port(e.getPort()).protocol("TCP"));
                    }
                });
            } else {
                // TODO move haproxy IP firewall rules creation to ComponentGroupDeployment?
                ProducedServiceDeployment producedServiceDeployment = (ProducedServiceDeployment) deployment.getParent().getParent();
                String haproxyId = HAProxyDeployer.getHaproxyNodeId(producedServiceDeployment);

                deployment.getEndpoints().forEach(e -> {
                    if (e.getPort() != null) {
                        int port = e.getPort();
                        rules.add(new FirewallRule().type(FirewallRule.TypeEnum.NODE_ID).port(port).protocol("TCP").from(haproxyId));
                    }
                });
            }
            ExecutionDetails details = nodeAPI.updateNodeFirewallRules(credentials, InfrastructureDeployer.getVmId(deployment), new FirewallUpdate().create(rules));
            for (Pair<DeploymentObject, Endpoint> e : deployment.getLinkedRequiredEndpoints()) {
                DeploymentObject d = e.getLeft();
                if (d instanceof VMDeployment) {
                    // FIXME should be moved to 'd' vm creation?
                    VMDeployment supplier = (VMDeployment) d;
                    String supplierId = InfrastructureDeployer.getVmId(supplier);
                    FirewallRule rule = new FirewallRule().type(FirewallRule.TypeEnum.NODE_ID).port(e.getRight().getPort()).protocol("TCP").from(InfrastructureDeployer.getVmId(deployment));
                    try {
                        ExecutionDetails ed = nodeAPI.updateNodeFirewallRules(credentials, supplierId, new FirewallUpdate().create(Collections.singletonList(rule)));
                        details = NodeUtil.merge(details, ed);
                    } catch (Exception ex) {
                        NodeUtil.logError(details, "Failed to create firewall rules for: " + supplierId);
                    }

                } else {
                    NodeUtil.logWarn(details, "Unsupported endpoint: " + d.getClass().getName());
                }
            }
            ;
            return details;
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

package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;
import org.ecloudmanager.service.verizon.VmService;
import org.mongodb.morphia.annotations.Transient;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VerizonCreateFirewallRulesAction extends SingleAction {
    @Transient
    private VmService vmService = CDI.current().select(VmService.class).get();
    @Transient
    private ApplicationDeploymentService applicationDeploymentService = CDI.current().select(ApplicationDeploymentService.class).get();

    private VerizonCreateFirewallRulesAction() {
        super();
    }

    public VerizonCreateFirewallRulesAction(VMDeployment deployable) {
        super(null, "Create Firewall Rules", deployable);
        setRunnable(() -> {
            vmService.createFirewallRules(deployable);
            applicationDeploymentService.update((ApplicationDeployment) deployable.getTop());
        });
    }

    @Override
    public List<Action> getDependencies(Action fullAction) {
        List<Action> result = new ArrayList<>();
        VMDeployment deployment = (VMDeployment) getDeployable();
        List<Endpoint> required = deployment.getLinkedRequiredEndpoints();
        required.forEach(e -> {
            DeploymentObject d = e.getParent();
            if (d instanceof VMDeployment) {
                VMDeployment supplier = (VMDeployment) d;
                Optional<SingleAction> createVMAction = fullAction.stream(SingleAction.class).filter(singleAction -> singleAction.getDeployable().equals(supplier))
                        .filter(singleAction -> VerizonVmActions.ASSIGN_IP_ACTION.equals(singleAction.getDescription())).findAny();
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

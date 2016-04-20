package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.service.aws.AWSVmService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AWSCreateFirewallRulesAction extends SingleAction {
    private AWSCreateFirewallRulesAction() {
        super();
    }

    public AWSCreateFirewallRulesAction(VMDeployment deployable, AWSVmService vmService, ApplicationDeploymentService applicationDeploymentService) {
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
        List<Endpoint> required = deployment.getRequiredEndpoints();
        required.forEach(e -> {
            DeploymentObject d = e.getParent();
            if (d instanceof VMDeployment) {
                VMDeployment supplier = (VMDeployment) d;
                Optional<SingleAction> createVMAction = fullAction.stream(SingleAction.class).filter(singleAction -> singleAction.getDeployable().equals(supplier))
                        .filter(singleAction -> AWSVmActions.CREATE_VM.equals(singleAction.getDescription())).findAny();
                if (createVMAction.isPresent()) {
                    result.add(createVMAction.get());
                }
            } else {
                throw new RuntimeException("Unsupported endpoint:" + d);
            }
        });
        result.addAll(super.getDependencies(fullAction));
        return result;

    }
}

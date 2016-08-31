package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;
import org.ecloudmanager.service.provisioning.GlobalProvisioningService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProvisionVmAction extends SingleAction {
    private static String VM_PROVISION_ACTION = "VM Provision";
    private static String VM_PROVISION_UPDATE_ACTION = "VM Provision update";

    private ProvisionVmAction() {
        super();
    }

    public ProvisionVmAction(VMDeployment deployable, AsyncNodeAPI api, Credentials credentials, GlobalProvisioningService globalProvisioningService, boolean update) {
        super(null, update ? VM_PROVISION_UPDATE_ACTION : VM_PROVISION_ACTION, deployable);
        setCallable(() -> {
            globalProvisioningService.provisionVm(getId(), deployable, api, credentials, !update);
            return null;
        });
    }

    @Override
    public List<Action> getDependencies(Action fullAction) {
        List<Action> result = new ArrayList<>();
        Optional<SingleAction> createEnvAction = fullAction.stream(SingleAction.class).filter(a -> ChefEnvironment.class.isInstance(a.getDeployable()))
                .filter(singleAction -> ChefActions.CREATE_ENVIRONMENT_ACTION.equals(singleAction.getDescription())).findAny();
        if (createEnvAction.isPresent()) {
            result.add(createEnvAction.get());
        }
        result.addAll(super.getDependencies(fullAction));
        return result;
    }

}

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

package org.ecloudmanager.deployment.vm;

import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.provisioning.ChefProvisioningDeployer;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SingleAction;

import java.util.stream.Stream;

public class VMDeployer implements Deployer<VMDeployment> {
    public static final String VM_NAME = "vmName";

    private InfrastructureDeployer infrastructureDeployer;
    private ChefProvisioningDeployer chefProvisioningDeployer = new ChefProvisioningDeployer();

    public VMDeployer(InfrastructureDeployer infrastructureDeployer) {
        this.infrastructureDeployer = infrastructureDeployer;
    }

    @Override
    public void specifyConstraints(VMDeployment deployment) {
        String vmName = deployment.getName();
        deployment.addField(ConstraintField.builder()
            .name(VM_NAME)
            .description("VM Name")
            .defaultValue(vmName)
            .build()
        );

        infrastructureDeployer.specifyConstraints(deployment);
        chefProvisioningDeployer.specifyConstraints(deployment);
    }

    @Override
    public Action getCreateAction(VMDeployment deployable) {
        return Action.actionSequence("Create " + deployable.getName(),
            infrastructureDeployer.getCreateAction(deployable),
            chefProvisioningDeployer.getCreateAction(deployable));
    }

    @Override
    public Action getDeleteAction(VMDeployment deployable) {
        return Action.actionSequence("Delete " + deployable.getName(),
            chefProvisioningDeployer.getDeleteAction(deployable),
            infrastructureDeployer.getDeleteAction(deployable));
    }

    public InfrastructureDeployer getInfrastructureDeployer() {
        return infrastructureDeployer;
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, VMDeployment before, VMDeployment after) {
        Stream<SingleAction> thisVmActionsLastAttempt = lastAttempt.getAction().stream(SingleAction.class)
            .filter(a -> a.getDeployable() != null)
            .filter(a -> a.getDeployable().equals(before));
        boolean lastAttemptFailed = thisVmActionsLastAttempt.anyMatch(a -> a.getStatus() != Action.Status.SUCCESSFUL);
        if (lastAttemptFailed || infrastructureDeployer.isRecreateActionRequired(before, after)) {
            return Action.actionSequence("Recreate " + after.getName(), getDeleteAction(before), getCreateAction
                (after));
        }
        return Action.actionSequence("Update " + after.getName(),
            infrastructureDeployer.getUpdateAction(lastAttempt, before, after),
            chefProvisioningDeployer.getUpdateAction(lastAttempt, before, after)
        );
    }

}

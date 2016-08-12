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

package org.ecloudmanager.deployment.vm.infrastructure;

import org.ecloudmanager.actions.HAProxyActions;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;

public class InfrastructureHAProxyDeployer implements Deployer<ProducedServiceDeployment> {
    private HAProxyActions haProxyActions = CDI.current().select(HAProxyActions.class).get();

    @Override
    public void specifyConstraints(ProducedServiceDeployment deployment) {
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, ProducedServiceDeployment before, ProducedServiceDeployment after) {
        return Action.actionSequence(
                "Recreate  " + before.getName(),
                getDeleteAction(before),
                getCreateAction(after)
        );
    }

    @Override
    public boolean isRecreateActionRequired(ProducedServiceDeployment before, ProducedServiceDeployment after) {
        return true;
    }

    @Override
    public Action getCreateAction(ProducedServiceDeployment deployable) {
        // create public endpoints for haproxy
        return haProxyActions.getCreatePublicEndpointFirewallRulesAction(deployable);
    }

    @Override
    public Action getDeleteAction(ProducedServiceDeployment deployable) {
        // delete public endpoints for haproxy
        return haProxyActions.getDeletePublicEndpointFirewallRulesAction(deployable);
    }

}

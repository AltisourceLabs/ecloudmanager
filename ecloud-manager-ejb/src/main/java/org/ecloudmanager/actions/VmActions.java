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

package org.ecloudmanager.actions;

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployerImpl;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.NodeInfo;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.repository.deployment.ActionLogger;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.deployment.LoggingEventRepository;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.SynchronousPoller;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.ecloudmanager.node.LoggableFuture.waitFor;

@Service
public class VmActions {
    static String CREATE_VM = "Create VM";
    @Inject
    SynchronousPoller synchronousPoller;
    @Inject
    private ApplicationDeploymentService applicationDeploymentService;

    @Inject
    private NodeAPIConfigurationService nodeAPIProvider;
    @Inject
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private LoggingEventRepository loggingEventRepository;

    public Action getCreateVmAction(VMDeployment vmDeployment, AsyncNodeAPI api, Credentials credentials, Map<String, String> parameters) {

        return Action.actionSequence("Create and Start VM",
                Action.single("Create VM", (ExecutorService executor, ActionLogger actionLog) -> {
                    clearVmConstraints(vmDeployment);
                    NodeInfo node = waitFor(api.createNode(credentials, parameters), actionLog);
                    InfrastructureDeployer.addVMId(vmDeployment, node.getId());
                    applicationDeploymentService.update((ApplicationDeployment) vmDeployment.getTop());
                    return node;
                }, vmDeployment),
                Action.single("Wait for node to be ready", (ExecutorService executor, ActionLogger actionLog) -> {
                    try {
                        NodeInfo node = NodeUtil.wait(api, credentials, InfrastructureDeployer.getVmId(vmDeployment));
                        actionLog.info("Node IP: " + node.getIp());
                        return node;
                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                }, vmDeployment),
                Action.single("Configure VM", (ExecutorService executor, ActionLogger actionLog) -> {
                    NodeInfo node = waitFor(api.configureNode(credentials, InfrastructureDeployer.getVmId(vmDeployment), parameters), actionLog);
                    return node;
                }, vmDeployment),
                Action.single("Wait for node to be ready", (ExecutorService executor, ActionLogger actionLog) -> {
                    try {
                        NodeInfo node = NodeUtil.wait(api, credentials, InfrastructureDeployer.getVmId(vmDeployment));
                        actionLog.info("Node IP: " + node.getIp());
                        InfrastructureDeployer.addIP(vmDeployment, node.getIp());
                        applicationDeploymentService.update((ApplicationDeployment) vmDeployment.getTop());
                        return node;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, vmDeployment),
                new CreateFirewallRulesAction(vmDeployment, api, credentials, loggingEventRepository));
    }


    public Action getDeleteVmAction(VMDeployment vmDeployment, AsyncNodeAPI api, Credentials credentials) {
        return Action.single("Delete VM",
                (ExecutorService executor, ActionLogger actionLog) -> {
                    String nodeId = InfrastructureDeployerImpl.getVmId(vmDeployment);
                    if (nodeId == null || nodeId.isEmpty()) {
                        actionLog.info("No node id, nothing to do");
                        return null;
                    }
                    Void result = waitFor(api.deleteNode(credentials, nodeId), actionLog);
                    clearVmConstraints(vmDeployment);
                    return result;
                }, vmDeployment);
    }

    public Action getUpdateVmAction(VMDeployment before, VMDeployment after) {
        return Action.single("Update VM", (ExecutorService executor, ActionLogger actionLog) -> {
            String vmId = InfrastructureDeployer.getVmId(before);
            //vmService.updateVm(before, after, vmId);
            return null;
        }, after);
    }


    private void clearVmConstraints(VMDeployment vmDeployment) {
        // This method can be called with a vmDeployment from the deployment history.
        // So, first we try to find real deployment and then update it if needed
        ObjectId id = vmDeployment.getTop().getId();
        ApplicationDeployment applicationDeployment = applicationDeploymentRepository.get(id);
        if (applicationDeployment != null) {
            applicationDeployment.stream(VMDeployment.class)
                    .filter(vd -> vd.getId() != null && vd.getId().equals(vmDeployment.getId()))
                    .findAny()
                    .ifPresent(realVmDeployment -> {
                        // Remove VM id and ip address and update DB
                        InfrastructureDeployer.removeVMId(realVmDeployment);
                        InfrastructureDeployer.removeIP(realVmDeployment);
                        applicationDeploymentService.update(applicationDeployment);
                    });
        }
        // Remove VM id and ip address in memory, to keep it in synch, just in case...
        InfrastructureDeployer.removeVMId(vmDeployment);
        InfrastructureDeployer.removeIP(vmDeployment);
    }

}

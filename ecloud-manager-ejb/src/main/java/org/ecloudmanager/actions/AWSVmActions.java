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

import com.amazonaws.services.ec2.model.Instance;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.AWSInfrastructureDeployer;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.service.aws.AWSVmService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.execution.Action;

import javax.inject.Inject;

@Service
public class AWSVmActions {
    @Inject
    private ApplicationDeploymentService applicationDeploymentService;
    @Inject
    private AWSVmService vmService;
    @Inject
    private ApplicationDeploymentRepository applicationDeploymentRepository;

    public Action getCreateVmAction(VMDeployment vmDeployment) {
        return Action.single("Create and start VM", () ->
        {
            Instance instance = vmService.createVm(vmDeployment);
            clearVmConstraints(vmDeployment);

            // Save VM id for future use
            InfrastructureDeployer.addVMId(vmDeployment, instance.getInstanceId());

            // Set ssh configuration name to the selected environment name
            InfrastructureDeployer.addSshConfiguration(vmDeployment, AWSInfrastructureDeployer.getAwsKeypair
                (vmDeployment));

            // Set IP address
            if (instance.getPrivateIpAddress() != null) {
                InfrastructureDeployer.addIP(vmDeployment, instance.getPrivateIpAddress());
            }

            applicationDeploymentService.update((ApplicationDeployment) vmDeployment.getTop());
        }, vmDeployment);
    }

//    public Action getDetectVmIpAddressAction(VMDeployment vmDeployment) {
//        return Action.single("Detect VM IP address",
//            () -> {
//                String vmId = VerizonInfrastructureDeployer.getVmId(vmDeployment);
//                String ipAddress = vmService.getIpAddress(vmId, 180);
//                InfrastructureDeployer.addIP(vmDeployment, ipAddress);
//                applicationDeploymentService.update((ApplicationDeployment) vmDeployment.getTop());
//            },
//            vmDeployment);
//    }

    public Action getDeleteVmAction(VMDeployment vmDeployment) {
        return Action.single("Delete VM",
            () -> {
                vmService.deleteVm(vmDeployment);
                clearVmConstraints(vmDeployment);
            }, vmDeployment);
    }

    public Action getUpdateVmAction(VMDeployment before, VMDeployment after) {
        return Action.single("Update VM", () -> {
            String vmId = InfrastructureDeployer.getVmId(before);
            vmService.updateVm(before, after, vmId);
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
                    InfrastructureDeployer.removeSSHConfiguration(realVmDeployment);
                    applicationDeploymentService.update(applicationDeployment);
                });
        }
        // Remove VM id and ip address in memory, to keep it in synch, just in case...
        InfrastructureDeployer.removeVMId(vmDeployment);
        InfrastructureDeployer.removeIP(vmDeployment);
        InfrastructureDeployer.removeSSHConfiguration(vmDeployment);
    }
}

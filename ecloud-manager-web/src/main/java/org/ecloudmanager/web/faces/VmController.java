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

package org.ecloudmanager.web.faces;

import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.provisioning.GlobalProvisioningService;
import org.ecloudmanager.service.provisioning.SshLog;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.TaskService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Controller
public class VmController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = 3041855386734692654L;

    @Inject
    private VirtualMachineService virtualMachineService;

    @Inject
    private NetworkService networkService;

    @Inject
    private TaskService taskService;

    @Inject
    private GlobalProvisioningService provisioningService;

    @Inject
    private transient VirtualMachineTemplateRepository vmtemplateRepo;

    private VirtualMachineType selectedVm = null;

    private String vmId = null;

    private String creationTaskId = null;

    private List<VirtualMachineTemplate> vmtemplates;

    private VirtualMachineTemplate selectedTemplate = null;

    private boolean poweredOn;

    private boolean powerOn;

    private TaskType creationTask;

    private TaskType currentTask;

    private String provisioningLog;

    public VmController() {
    }

    @PostConstruct
    private void init() {
        vmId = getRequestParameter("vmId");
        if (hasRequestParameter("creationTaskId")) {
            creationTaskId = getRequestParameter("creationTaskId");
            creationTask = taskService.getTaskById(creationTaskId);
            currentTask = creationTask;
        }
        selectedVm = virtualMachineService.getVirtualMachineById(vmId);
        poweredOn = selectedVm.getPoweredOn().getValue();
        powerOn = poweredOn;
        vmtemplates = vmtemplateRepo.getAll();
    }

    public void fetchSelectedVmStatus() {
        if (currentTask != null) {
            currentTask = taskService.getTaskById(TmrkUtils.getIdFromHref(currentTask.getHref()));
            if (isTaskInProgress()) {
                //                currentTask = null;
            } else {
                selectedVm = virtualMachineService.getVirtualMachineById(vmId);
                poweredOn = selectedVm.getPoweredOn().getValue();
                //                powerOn = poweredOn;
            }
        }
    }

    public void powerOnOff(AjaxBehaviorEvent ev) {
        if (isTaskInProgress()) {
            return;
        }
        if (powerOn) {
            TaskType task = virtualMachineService.actionPowerOnMachine(vmId);
            currentTask = task;
        } else {
            TaskType task = virtualMachineService.actionPowerOffMachine(vmId);
            currentTask = task;
        }
        String summary = powerOn ? "Checked" : "Unchecked";
        addMessage(null, new FacesMessage(summary));
    }

    public void selectProvisioning(ValueChangeEvent ev) {
        selectedTemplate = (VirtualMachineTemplate) ev.getNewValue();
    }

    public boolean isTaskInProgress() {
        if (currentTask == null || currentTask.getStatus() == null) {
            return false;
        }
        TaskStatus status = currentTask.getStatus().getValue();

        return TaskStatus.QUEUED.equals(status) || TaskStatus.RUNNING.equals(status);
    }

    public void provisionMachine(ActionEvent ev) {
        System.out.println("starting provisioning machine");
        HardwareConfigurationType hwconfig = selectedVm.getHardwareConfiguration().getValue();
        NicsType nics = hwconfig.getNics().getValue();
        NetworkReferenceType network = nics.getNic().get(0).getNetwork();
        String networkId = network.getHref().substring(network.getHref().lastIndexOf("/") + 1, network.getHref()
            .length());

        NetworkType networkType = networkService.getNetworkById(networkId);

        String selectedVmIpAddress = null;

        List<IpAddressType> ipAddresses = networkType.getIpAddresses().getValue().getIpAddress();
        for (IpAddressType ipAddress : ipAddresses) {
            if (ipAddress.getDetectedOn() != null) {
                if (ipAddress.getDetectedOn().getValue().getName().equals(selectedVm.getName())) {
                    selectedVmIpAddress = ipAddress.getName();
                }
            }
        }

        try {
            provisioningService.provisionVm(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Produces
    @Named
    public VirtualMachineType getSelectedVm() {
        return selectedVm;
    }

    public void setSelectedVm(VirtualMachineType selectedVm) {
        this.selectedVm = selectedVm;
    }

    public List<VirtualMachineTemplate> getTemplates() {
        return vmtemplates;
    }

    public VirtualMachineTemplate getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(VirtualMachineTemplate selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public boolean isPoweredOn() {
        return poweredOn;
    }

    public void setPoweredOn(boolean poweredOn) {
        this.poweredOn = poweredOn;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    public String getProvisioningLog() {
        return provisioningLog;
    }

    public void updateProvisioningLog(@Observes SshLog log) {
        provisioningLog += log.getOutput();
        //        requestContext().execute("PrimeFaces.info('" + log.getOutput()+ "');");
        //        requestContext().update("j_idt8:log");
        requestContext().update("@all");
    }
}

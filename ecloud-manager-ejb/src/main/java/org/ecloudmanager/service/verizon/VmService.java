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

package org.ecloudmanager.service.verizon;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.infrastructure.VerizonInfrastructureDeployer;
import org.ecloudmanager.domain.verizon.deployment.VirtualMachine;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.VirtualMachineRepository;
import org.ecloudmanager.service.deployment.CreateVm;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.TaskService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Stateless
public class VmService extends ServiceSupport {
    private static final Set<Integer> ALLOWED_CPU_VALUES = Sets.newHashSet(1, 2, 4, 8);

    @Inject
    private Logger log;

    @Inject
    private VirtualMachineService virtualMachineService;

    @Inject
    private TaskService taskService;

    @Inject
    private VmByNamesAdapterService vmByNamesAdapterService;

    @Inject
    private VirtualMachineRepository vmRepository;

    @Inject
    private NetworkService networkService;

    @Inject
    private CloudCachedEntityService cacheService;

    private ObjectFactory objectFactory = new ObjectFactory();

    public String createVm(VMDeployment vmDeployment) {
        // Obtain the vm creation parameters from constraints
        VirtualMachineTemplate vmTemplate = vmDeployment.getVirtualMachineTemplate();
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
        int cpu = vmTemplate.getProcessorCount();
        int memory = vmTemplate.getMemory();

        String envStr = VerizonInfrastructureDeployer.getEnvironment(vmDeployment);
        String netStr = VerizonInfrastructureDeployer.getSubnet(vmDeployment);
        String grpStr = VerizonInfrastructureDeployer.getGroup(vmDeployment);
        String rowStr = VerizonInfrastructureDeployer.getRow(vmDeployment);
        String catStr = VerizonInfrastructureDeployer.getCatalog(vmDeployment);

        CreateVm createVm = new CreateVm();
        createVm.setVmName(name);
        createVm.setCatalog(catStr);
        createVm.setCpuCount(Integer.toString(cpu));
        createVm.setEnvironment(envStr);
        createVm.setGroup(grpStr);
        createVm.setRow(rowStr);
        createVm.setMemory(Integer.toString(memory));
        createVm.setNetwork(netStr);
        // For now - use the first available compute pool
        //createVm.setComputePool();

        // Create vm
        String vmId = vmByNamesAdapterService.importVmFromCatalog(createVm);

        // Allocate the disk space if needed
        updateHardwareConfiguration(vmId, vmDeployment);

        return vmId;
    }


    public String deployVm(String computePoolId, ImportVirtualMachineType virtualMachine) {
        VirtualMachine vm = null;
        String name = virtualMachine.getName();

        log.info("Creating " + name);
        VirtualMachineType createdVm = virtualMachineService.importVirtualMachineFromCatalog(computePoolId,
            virtualMachine);

        vm = new VirtualMachine();
        vm.setName(name);
        vm.setHref(createdVm.getHref());

        log.info("Saving " + vm.getName());
        save(vm);
        fireEntityCreated(vm);

        return TmrkUtils.getIdFromHref(createdVm.getHref());
    }

//    public String getVmId(ComponentGroupDeployment componentGroupDeployment) {
//        VirtualMachineTemplate vmTemplate = componentGroupDeployment.getVirtualMachineTemplate();
//        String name = vmTemplate.getName();
//        VirtualMachine virtualMachine = vmRepository.find("name", name);
//        if (virtualMachine == null) {
//            // TODO - try to search using api
//            log.error("Cannot find virtual machine " + name);
//            return null;
//        }
//
//        // TODO - verify that vm exists
//        return TmrkUtils.getIdFromHref(virtualMachine.getHref());
//    }

    public void updateVmNameAndLayout(String vmId, VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);

        VirtualMachineType vm = virtualMachineService.getVirtualMachineById(vmId);

        if (!vm.getName().equals(name)) {
            log.info("Updating VM name for " + vmId);

            vm.setName(name);
            TaskType task = virtualMachineService.editVirtualMachine(vmId, vm);
            waitUntilTaskNotFinished(task);
        }

        String group = VerizonInfrastructureDeployer.getGroup(vmDeployment);
        String row = VerizonInfrastructureDeployer.getRow(vmDeployment);

        String currentGroup = vm.getLayout().getValue().getGroup().getValue().getName();
        String currentRow = vm.getLayout().getValue().getRow().getValue().getName();

        if (!currentGroup.equals(group) || !currentRow.equals(row)) {
            log.info("Updating VM layout for " + vmId);

            String envStr = VerizonInfrastructureDeployer.getEnvironment(vmDeployment);
            EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, envStr);
            LayoutRequestType layoutRequest = vmByNamesAdapterService.createLayoutRequest(row, group, env);
            virtualMachineService.moveVirtualMachine(vmId, layoutRequest);
        }
    }

    public void updateHardwareConfiguration(String vmId, VMDeployment vmDeployment) {
        log.info("Updating hardware configuration of VM " + vmId);

        int storage = vmDeployment.getVirtualMachineTemplate().getStorage();
        int cpu = vmDeployment.getVirtualMachineTemplate().getProcessorCount();
        int memory = vmDeployment.getVirtualMachineTemplate().getMemory();

        if (storage > 512) {
            log.error("Cannot set storage to more than 512GB, but value is: " + storage);
            return;
        }
        if (memory % 4 != 0) {
            log.error("Memory should be multiple of 4 but was: " + memory);
            return;
        }
        if (!ALLOWED_CPU_VALUES.contains(cpu)) {
            log.error("Invalid cpu count: " + cpu);
            return;
        }

        waitUntilMachineIsNotReady(vmId, 1000);

        boolean needShutdown = false;
        boolean nothingToDo = true;

        HardwareConfigurationType hardwareConfiguration = virtualMachineService
            .getVirtualMachineHardwareConfiguration(vmId);
        VirtualDiskType disk1 = hardwareConfiguration.getDisks().getValue().getDisk().get(0);
        int currentStorage = disk1.getSize().getValue().intValue();
        if (currentStorage != storage) {
            if (currentStorage > storage) {
                log.error("Cannot set storage to a value less than current (shrink drive). Current: " +
                    currentStorage + ", new: " + storage);
                return;
            }
            nothingToDo = false;
            disk1.getSize().setValue(BigDecimal.valueOf(storage));
        }
        if (hardwareConfiguration.getMemory().getValue().getValue().intValue() != memory) {
            needShutdown = true;
            nothingToDo = false;
            hardwareConfiguration.getMemory().getValue().setValue(BigDecimal.valueOf(memory));
        }
        if (hardwareConfiguration.getProcessorCount() != cpu) {
            needShutdown = true;
            nothingToDo = false;
            hardwareConfiguration.setProcessorCount(cpu);
        }

        if (nothingToDo) {
            return;
        }

        boolean needStartup = false;
        if (needShutdown) {
            VirtualMachineType vm = virtualMachineService.getVirtualMachineById(vmId);
            boolean poweredOn = vm.getPoweredOn().getValue();
            if (poweredOn) {
                shutdownVm(vmId);
                needStartup = true;
            }
        }

        TaskType task = virtualMachineService.editVirtualMachineHardwareConfiguration(vmId, objectFactory
            .createHardwareConfiguration(hardwareConfiguration));
        waitUntilTaskNotFinished(task);

        if (needStartup) {
            startupVm(vmId);
        }
    }

    public void startupVm(String vmId) {
        log.info("Starting VM " + vmId);
        waitUntilMachineIsNotReady(vmId, 1000);

        TaskType task = virtualMachineService.actionPowerOnMachine(vmId);
        waitUntilTaskNotFinished(task);
    }

    private VirtualMachineType waitUntilMachineIsNotReady(String vmId, int timeout) {
        VirtualMachineType result = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            List<VirtualMachineStatus> badStatuses = Arrays.asList(
                VirtualMachineStatus.COPY_IN_PROGRESS,
                VirtualMachineStatus.TASK_IN_PROGRESS,
                VirtualMachineStatus.NOT_DEPLOYED,
                VirtualMachineStatus.ORPHANED
            );
            List<Future<VirtualMachineType>> futures = executor.invokeAll(Arrays.asList(
                (Callable<VirtualMachineType>) () -> {
                while (true) {
                    VirtualMachineType virtualMachineType = virtualMachineService.getVirtualMachineById(vmId);
                    VirtualMachineStatus status = virtualMachineType.getStatus().getValue();
                    if (!badStatuses.contains(status)) {
                        return virtualMachineType;
                    }
                }
            }), timeout, TimeUnit.SECONDS);
            result = futures.get(0).get();
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Timeout while waiting for node become ready", e);
        } catch (ExecutionException e) {
            log.log(Level.ERROR, "Virtual machine " + vmId + " not ready", e);
        }
        executor.shutdown();
        return result;
    }

    public String getIpAddress(String vmId, int timeout) {
        String address = null;
        long start = System.currentTimeMillis();
        while (address == null && System.currentTimeMillis() - start < timeout * 1000) {
            address = getIpAddress(vmId);
        }

        if (address == null) {
            throw new ActionException("Cannot obtain IP address for VM " + vmId);
        } else {
            log.info("Successfully obtained IP address " + address + " for VM " + vmId);
        }

        return address;
    }

    private String getIpAddress(String vmId) {
        log.info("Trying to obtain IP address for VM " + vmId);
        VirtualMachineType selectedVm = virtualMachineService.getVirtualMachineById(vmId);

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

        if (selectedVmIpAddress == null) {
            log.info("IP address for VM " + vmId + " is not available.");
        }

        return selectedVmIpAddress;
    }

    public void shutdownVm(String vmId) {
        log.info("Shutting down VM " + vmId);

        VirtualMachineType vm = waitUntilMachineIsNotReady(vmId, 1000);
        if (vm == null) {
            log.warn("VM " + vmId + " not ready - skip shutdown");
            return;
        }

        if (!vm.getPoweredOn().getValue()) {
            log.info("VM " + vmId + " is not powered on - skip shutdown");
            return;
        }

        TaskType task = virtualMachineService.actionShutdownMachine(vmId);
        waitUntilTaskNotFinished(task);
        waitUntilMachineIsPoweredOff(vmId, 1000);
    }

    public void deleteVm(String vmId) {
        log.info("Deleting VM " + vmId);

        VirtualMachineType vm = waitUntilMachineIsNotReady(vmId, 1000);
        if (vm == null) {
            log.info("VM " + vmId + " not ready - skip deletion");
            return;
        }
        TaskType task = virtualMachineService.removeVirtualMachine(vmId);
        waitUntilTaskNotFinished(task);
    }

    private void waitUntilTaskNotFinished(TaskType task) {
        TaskStatus status = task.getStatus().getValue();

        while (TaskStatus.QUEUED.equals(status) || TaskStatus.RUNNING.equals(status)) {
            task = taskService.getTaskById(TmrkUtils.getIdFromHref(task.getHref()));
            status = task.getStatus().getValue();
        }
    }

    private void waitUntilMachineIsPoweredOff(String vmId, int timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.invokeAll(Arrays.asList((Callable<Boolean>) () -> {
                while (true) {
                    VirtualMachineType virtualMachineType = virtualMachineService.getVirtualMachineById(vmId);
                    if (!virtualMachineType.getPoweredOn().getValue()) {
                        return true;
                    }
                }
            }), timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Timeout while waiting for node to become powered off", e);
        }
        executor.shutdown();
    }

}

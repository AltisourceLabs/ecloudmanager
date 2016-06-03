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
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.infrastructure.VerizonInfrastructureDeployer;
import org.ecloudmanager.domain.verizon.deployment.VirtualMachine;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.VirtualMachineRepository;
import org.ecloudmanager.service.deployment.CreateVm;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.execution.SynchronousPoller;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.TaskService;
import org.ecloudmanager.tmrk.cloudapi.service.network.FirewallRuleService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Stateless
public class VmService extends ServiceSupport {
    private static final Set<Integer> ALLOWED_CPU_VALUES = Sets.newHashSet(1, 2, 4, 8);
    private static final long VZ_TASK_TIMEOUT_SEC = 1000;
    @Inject
    SynchronousPoller synchronousPoller;
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
    @Inject
    private FirewallRuleService firewallRuleService;
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
            task.setName("edit virtual machine");
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
        task.setName("edit virtual machine hardware configuration");
        waitUntilTaskNotFinished(task);

        if (needStartup) {
            startupVm(vmId);
        }
    }

    public void startupVm(String vmId) {
        log.info("Starting VM " + vmId);
        waitUntilMachineIsNotReady(vmId, 1000);

        TaskType task = virtualMachineService.actionPowerOnMachine(vmId);
        task.setName("power on virtual machine");
        waitUntilTaskNotFinished(task);
    }

    private VirtualMachineType waitUntilMachineIsNotReady(String vmId, int timeout) {
        List<VirtualMachineStatus> badStatuses = Arrays.asList(
                VirtualMachineStatus.COPY_IN_PROGRESS,
                VirtualMachineStatus.TASK_IN_PROGRESS,
                VirtualMachineStatus.NOT_DEPLOYED,
                VirtualMachineStatus.ORPHANED
        );

        Callable<VirtualMachineType> poll = () -> virtualMachineService.getVirtualMachineById(vmId);
        Predicate<VirtualMachineType> check = (virtualMachineType) -> {
            VirtualMachineStatus status = virtualMachineType.getStatus().getValue();
            return !badStatuses.contains(status);
        };

        VirtualMachineType result = null;

        try {
            result = synchronousPoller.poll(
                    poll, check,
                    1, VZ_TASK_TIMEOUT_SEC,
                    "waiting for virtual machine " + vmId + " to become ready"
            );
        } catch (ActionException e) {
            log.error("Virtual machine " + vmId + " is not ready", e);
        }

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
        task.setName("shutdown virtual machine");
        waitUntilTaskNotFinished(task);
        waitUntilMachineIsPoweredOff(vmId);
    }

    public void deleteVm(String vmId) {
        log.info("Deleting VM " + vmId);

        VirtualMachineType vm = waitUntilMachineIsNotReady(vmId, 1000);
        if (vm == null) {
            log.info("VM " + vmId + " not ready - skip deletion");
            return;
        }
        TaskType task = virtualMachineService.removeVirtualMachine(vmId);
        task.setName("remove virtual machine");
        waitUntilTaskNotFinished(task);
    }

    private void waitUntilTaskNotFinished(TaskType task) {
        String taskHref = task.getHref();

        Callable<TaskStatus> poll = () -> {
            TaskType obtainedTask = taskService.getTaskById(TmrkUtils.getIdFromHref(taskHref));
            return obtainedTask.getStatus().getValue();
        };
        Predicate<TaskStatus> check =
                (status) -> !(TaskStatus.QUEUED.equals(status) || TaskStatus.RUNNING.equals(status));
        synchronousPoller.poll(
                poll, check,
                1, VZ_TASK_TIMEOUT_SEC,
                "waiting for task '" + task.getName() + "' to complete."
        );
    }

    private void waitUntilMachineIsPoweredOff(String vmId) {
        Callable<Boolean> poll = () -> {
            VirtualMachineType virtualMachineType = virtualMachineService.getVirtualMachineById(vmId);
            return !virtualMachineType.getPoweredOn().getValue();
        };
        synchronousPoller.poll(
                poll, x -> x,
                1, VZ_TASK_TIMEOUT_SEC,
                "waiting for node " + vmId + " to become powered off"
        );
    }

//    public void deleteFirewallRules(ProducedServiceDeployment producedServiceDeployment) {
//        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
//        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
//        // Delete firewall rule for haproxy frontend if there's a public endpoint
//        producedServiceDeployment.children(Endpoint.class).forEach(endpoint -> {
//            int port = Integer.parseInt(endpoint.getConfigValue("port"));
//            String publicEndpointName = producedServiceDeployment.getName() + ":" + endpoint.getName();
//            if (ad.getPublicEndpoints().contains(publicEndpointName)) {
//                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
//                // TODO - delete firewall rule
//            }
//        });
//    }
//
//    public void createFirewallRules(ProducedServiceDeployment producedServiceDeployment) {
//        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
//        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
//        // Create firewall rule for haproxy frontend if there's a public endpoint
//        producedServiceDeployment.children(Endpoint.class).forEach(e -> {
//            int port = Integer.parseInt(e.getConfigValue("port"));
//            String publicEndpointName = producedServiceDeployment.getName() + ":" + e.getName();
//            if (ad.getPublicEndpoints().contains(publicEndpointName)) {
//                log.info("Creating firewall rule for public endpoint " + publicEndpointName);
//                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
//                FirewallAclType result = createFirewallRule(getEnv(gatewayVmDeployment), firewallAclEndpointTypeAny(), firewallAclEndpointType(gatewayVmDeployment), port);
//            }
//        });
//    }

    public void createFirewallRules(VMDeployment deployment) {
        ApplicationDeployment ad = (ApplicationDeployment) deployment.getTop();
        if (deployment.getTop() == deployment.getParent()) {
            // TODO move public IP firewall rules creation to ApplicationDeployment?
            deployment.children(Endpoint.class).forEach(e -> {
                if (ad.getPublicEndpoints().contains(deployment.getName() + ":" + e.getName())) {
                    int port = Integer.parseInt(e.getConfigValue("port"));
//                    FirewallAclType result = createFirewallRule(getEnv(deployment), firewallAclEndpointTypeAny(), firewallAclEndpointType(deployment), port);
//                    String href = result.getHref();
                }
            });
        } else {
            // TODO move haproxy IP firewall rules creation to ComponentGroupDeployment?
            ProducedServiceDeployment psd = (ProducedServiceDeployment) deployment.getParent().getParent();
            deployment.getVirtualMachineTemplate().getEndpoints().forEach(e -> {
                if (e.getPort() != null) {
                    int port = e.getPort();
                    FirewallAclType result = createFirewallRule(getEnv(deployment), haProxyFirewallAclEndpointType(psd), firewallAclEndpointType(deployment), port);
//                    String href = result.getHref();
                }
            });
        }

        List<Endpoint> required = deployment.getRequiredEndpoints();
        required.forEach(e -> {
            int port = Integer.parseInt(e.getConfigValue("port"));
            DeploymentObject d = e.getParent();
            if (d instanceof VMDeployment) {
                VMDeployment target = (VMDeployment) d;
                FirewallAclType result = createFirewallRule(getEnv(target), firewallAclEndpointType(deployment), firewallAclEndpointType(target), port);

            } else {
                log.warn("Unsupported endpoint:" + d);
            }
        });

    }

    public FirewallAclType createFirewallRule(String envId, FirewallAclEndpointType source, FirewallAclEndpointType dest, long port) {
        log.info("Create firewall rule from " + source + " to " + dest + ", port " + port);

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.TCP);

        firewallAcl.setSource(source);

        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(dest));
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(port);
        portRange.setEnd(port);
        firewallAcl.setPortRange(objectFactory.createPortRange(portRange));
        FirewallAclType result = null;
        JAXBElement<CreateFirewallAclType> request = objectFactory.createCreateFirewallAcl(firewallAcl);
        try {
            result = firewallRuleService.createFirewallRuleForEnvironment(envId, request);
        } catch (Throwable t) {
            log.error("Failed to create firewall rule", t);
            throw t;
        }

        if (result != null) {
            log.info("Firewall rule was created successfully: " + result.getHref());
        }

        return result;
    }

    private FirewallAclEndpointType firewallAclEndpointType(VMDeployment deployment) {

        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        IpAddressReferenceType v = objectFactory.createIpAddressReferenceType();
        String ip = InfrastructureDeployer.getIP(deployment);
        String network = getNetwork(deployment);
        String href = "/cloudapi/ecloud/ipaddresses/networks/" + network + "/" + ip;
        v.setHref(href);
        v.setName(ip);
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(v));
        return fwaclet;
    }


    private FirewallAclEndpointType firewallAclEndpointTypeAny() {
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.ANY);
        return fwaclet;
    }

    private String getEnv(VMDeployment deployment) {
        EnvironmentType href = cacheService.getByHrefOrName(EnvironmentType.class, VerizonInfrastructureDeployer.getEnvironment(deployment));
        return TmrkUtils.getIdFromHref(href.getHref());
    }

    private String getNetwork(VMDeployment deployment) {
        String vmId = InfrastructureDeployer.getVmId(deployment);
        VirtualMachineType selectedVm = virtualMachineService.getVirtualMachineById(vmId);

        HardwareConfigurationType hwconfig = selectedVm.getHardwareConfiguration().getValue();
        NicsType nics = hwconfig.getNics().getValue();
        NetworkReferenceType network = nics.getNic().get(0).getNetwork();
        String networkId = network.getHref().substring(network.getHref().lastIndexOf("/") + 1, network.getHref()
                .length());

        return networkId;
    }

    private FirewallAclEndpointType haProxyFirewallAclEndpointType(ProducedServiceDeployment deployment) {
        GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(deployment);
        return firewallAclEndpointType(gatewayVmDeployment);
    }

    public void assignIp(String vmId, String network, String ipAddress) {
        AssignedIpAddressesType assignedIp = objectFactory.createAssignedIpAddressesType();
        ArrayOfActionType aat = objectFactory.createArrayOfActionType();
        ActionType at = objectFactory.createActionType();
        at.setHref("/cloudapi/ecloud/virtualMachines/" + vmId + "/assignedIps");
        at.setName("edit");
        aat.getAction().add(at);
        assignedIp.setActions(objectFactory.createResourceTypeActions(aat));
        DeviceNetworkType dn = objectFactory.createDeviceNetworkType();
        DeviceNetworksType dnt = objectFactory.createDeviceNetworksType();
        dn.setName(network);
        NetworksType networks = cacheService.getByHrefOrName(NetworksType.class, network);

        String networkHref = networks.getHref();
        dn.setHref(networkHref);
        DeviceIpsType ips = objectFactory.createDeviceIpsType();

        ips.getIpAddress().add(ipAddress);

        dn.setIpAddresses(objectFactory.createDeviceNetworkTypeIpAddresses(ips));
        dnt.getNetwork().add(dn);
        assignedIp.setNetworks(objectFactory.createAssignedIpAddressesTypeNetworks(dnt));
        TaskType task = virtualMachineService.editVirtulMachineAssignedIp(vmId, assignedIp);
        task.setName("assign ip address " + ipAddress + " to " + vmId);
        waitUntilTaskNotFinished(task);
    }
}

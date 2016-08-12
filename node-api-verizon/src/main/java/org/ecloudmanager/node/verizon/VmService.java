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

package org.ecloudmanager.node.verizon;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.node.util.SynchronousPoller;
import org.ecloudmanager.tmrk.cloudapi.model.ObjectFactory;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.TaskService;
import org.ecloudmanager.tmrk.cloudapi.service.network.FirewallRuleService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;

import java.util.Set;

public class VmService {
    private static final Set<Integer> ALLOWED_CPU_VALUES = Sets.newHashSet(1, 2, 4, 8);
    private static final long VZ_TASK_TIMEOUT_SEC = 1000;
    SynchronousPoller synchronousPoller = new SynchronousPoller();
    SecretKey credentials;
    CloudServicesRegistry registry;
    VirtualMachineService virtualMachineService;
    private TaskService taskService;
    //private VirtualMachineRepository vmRepository;
    private NetworkService networkService;
    private CloudCachedEntityService cacheService;
    private FirewallRuleService firewallRuleService;
    private ObjectFactory objectFactory = new ObjectFactory();
    private Logger log;


    public VmService(CloudServicesRegistry registry, SecretKey credentials) {
        this.credentials = credentials;
        registry = new CloudServicesRegistry(credentials.getName(), credentials.getSecret());
        virtualMachineService = registry.getVirtualMachineService();
        taskService = registry.getTaskService();
        networkService = registry.getNetworkService();

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

//    public void updateVmNameAndLayout(String vmId, VMDeployment vmDeployment) {
//        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);
//
//        VirtualMachineType vm = virtualMachineService.getVirtualMachineById(vmId);
//
//        if (!vm.getName().equals(name)) {
//            log.info("Updating VM name for " + vmId);
//
//            vm.setName(name);
//            TaskType task = virtualMachineService.editVirtualMachine(vmId, vm);
//            task.setName("edit virtual machine");
//            waitUntilTaskNotFinished(task);
//        }
//
//        String group = VerizonInfrastructureDeployer.getGroup(vmDeployment);
//        String row = VerizonInfrastructureDeployer.getRow(vmDeployment);
//
//        String currentGroup = vm.getLayout().getValue().getGroup().getValue().getName();
//        String currentRow = vm.getLayout().getValue().getRow().getValue().getName();
//
//        if (!currentGroup.equals(group) || !currentRow.equals(row)) {
//            log.info("Updating VM layout for " + vmId);
//
//            String envStr = VerizonInfrastructureDeployer.getEnvironment(vmDeployment);
//            EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, envStr);
//            LayoutRequestType layoutRequest = vmByNamesAdapterService.createLayoutRequest(row, group, env);
//            virtualMachineService.moveVirtualMachine(vmId, layoutRequest);
//        }
//    }

//
//
////    public void deleteFirewallRules(ProducedServiceDeployment producedServiceDeployment) {
////        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
////        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
////        // Delete firewall rule for haproxy frontend if there's a public endpoint
////        producedServiceDeployment.children(Endpoint.class).forEach(endpoint -> {
////            int port = Integer.parseInt(endpoint.getConfigValue("port"));
////            String publicEndpointName = producedServiceDeployment.getName() + ":" + endpoint.getName();
////            if (ad.getPublicEndpoints().contains(publicEndpointName)) {
////                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
////                // TODO - delete firewall rule
////            }
////        });
////    }
////
////    public void createFirewallRules(ProducedServiceDeployment producedServiceDeployment) {
////        ApplicationDeployment ad = (ApplicationDeployment) producedServiceDeployment.getTop();
////        // TODO - here we use the same port from endpoint both for frontend and backend. They should be different.
////        // Create firewall rule for haproxy frontend if there's a public endpoint
////        producedServiceDeployment.children(Endpoint.class).forEach(e -> {
////            int port = Integer.parseInt(e.getConfigValue("port"));
////            String publicEndpointName = producedServiceDeployment.getName() + ":" + e.getName();
////            if (ad.getPublicEndpoints().contains(publicEndpointName)) {
////                log.info("Creating firewall rule for public endpoint " + publicEndpointName);
////                GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(producedServiceDeployment);
////                FirewallAclType result = createFirewallRule(getEnv(gatewayVmDeployment), firewallAclEndpointTypeAny(), firewallAclEndpointType(gatewayVmDeployment), port);
////            }
////        });
////    }
//
//    public void createFirewallRules(VMDeployment deployment) {
//        ApplicationDeployment ad = (ApplicationDeployment) deployment.getTop();
//        if (deployment.getTop() == deployment.getParent()) {
//            // TODO move public IP firewall rules creation to ApplicationDeployment?
//            deployment.children(Endpoint.class).forEach(e -> {
//                if (ad.getPublicEndpoints().contains(deployment.getName() + ":" + e.getName())) {
//                    int port = Integer.parseInt(e.getConfigValue("port"));
////                    FirewallAclType result = createFirewallRule(getEnv(deployment), firewallAclEndpointTypeAny(), firewallAclEndpointType(deployment), port);
////                    String href = result.getHref();
//                }
//            });
//        } else {
//            // TODO move haproxy IP firewall rules creation to ComponentGroupDeployment?
//            ProducedServiceDeployment psd = (ProducedServiceDeployment) deployment.getParent().getParent();
//            deployment.getVirtualMachineTemplate().getEndpoints().forEach(e -> {
//                if (e.getPort() != null) {
//                    int port = e.getPort();
//                    FirewallAclType result = createFirewallRule(getEnv(deployment), haProxyFirewallAclEndpointType(psd), firewallAclEndpointType(deployment), port);
////                    String href = result.getHref();
//                }
//            });
//        }
//
//        List<Pair<DeploymentObject, Endpoint>> required = deployment.getLinkedRequiredEndpoints();
//        required.forEach(e -> {
//            int port = e.getRight().getPort();
//            DeploymentObject d = e.getLeft();
//            if (d instanceof VMDeployment) {
//                VMDeployment target = (VMDeployment) d;
//                FirewallAclType result = createFirewallRule(getEnv(target), firewallAclEndpointType(deployment), firewallAclEndpointType(target), port);
//
//            } else {
//                log.warn("Unsupported endpoint:" + d);
//            }
//        });
//
//    }
//
//    public FirewallAclType createFirewallRule(String envId, FirewallAclEndpointType source, FirewallAclEndpointType dest, long port) {
//        log.info("Create firewall rule from " + source + " to " + dest + ", port " + port);
//
//        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();
//
//        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
//        firewallAcl.setProtocol(ProtocolTypeEnum.TCP);
//
//        firewallAcl.setSource(source);
//
//        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(dest));
//        PortRangeType portRange = new PortRangeType();
//        portRange.setStart(port);
//        portRange.setEnd(port);
//        firewallAcl.setPortRange(objectFactory.createPortRange(portRange));
//        FirewallAclType result = null;
//        JAXBElement<CreateFirewallAclType> request = objectFactory.createCreateFirewallAcl(firewallAcl);
//        try {
//            result = firewallRuleService.createFirewallRuleForEnvironment(envId, request);
//        } catch (Throwable t) {
//            log.error("Failed to create firewall rule", t);
//            throw t;
//        }
//
//        if (result != null) {
//            log.info("Firewall rule was created successfully: " + result.getHref());
//        }
//
//        return result;
//    }
//
//    private FirewallAclEndpointType firewallAclEndpointType(VMDeployment deployment) {
//
//        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
//        IpAddressReferenceType v = objectFactory.createIpAddressReferenceType();
//        String ip = InfrastructureDeployer.getIP(deployment);
//        String network = getNetwork(deployment);
//        String href = "/cloudapi/ecloud/ipaddresses/networks/" + network + "/" + ip;
//        v.setHref(href);
//        v.setName(ip);
//        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
//        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(v));
//        return fwaclet;
//    }
//
//
//    private FirewallAclEndpointType firewallAclEndpointTypeAny() {
//        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
//        fwaclet.setType(FirewallAclEndpointTypeEnum.ANY);
//        return fwaclet;
//    }
//
//    private String getEnv(VMDeployment deployment) {
//        EnvironmentType href = cacheService.getByHrefOrName(EnvironmentType.class, VerizonInfrastructureDeployer.getEnvironment(deployment));
//        return TmrkUtils.getIdFromHref(href.getHref());
//    }
//
//    private String getNetwork(VMDeployment deployment) {
//        String vmId = InfrastructureDeployer.getVmId(deployment);
//        VirtualMachineType selectedVm = virtualMachineService.getVirtualMachineById(vmId);
//
//        HardwareConfigurationType hwconfig = selectedVm.getHardwareConfiguration().getValue();
//        NicsType nics = hwconfig.getNics().getValue();
//        NetworkReferenceType network = nics.getNic().get(0).getNetwork();
//        String networkId = network.getHref().substring(network.getHref().lastIndexOf("/") + 1, network.getHref()
//                .length());
//
//        return networkId;
//    }
//
//    private FirewallAclEndpointType haProxyFirewallAclEndpointType(ProducedServiceDeployment deployment) {
//        GatewayVMDeployment gatewayVmDeployment = HAProxyDeployer.getGatewayVmDeployment(deployment);
//        return firewallAclEndpointType(gatewayVmDeployment);
//    }
//
}

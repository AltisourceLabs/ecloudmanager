package org.ecloudmanager.node.verizon;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.util.NodeUtil;
import org.ecloudmanager.node.util.SynchronousPoller;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VerizonNodeAPI implements NodeBaseAPI {
    private static final Set<Integer> ALLOWED_CPU_VALUES = Sets.newHashSet(1, 2, 4, 8);
    private static final long VZ_TASK_TIMEOUT_SEC = 1000;
    private static Map<Pair<String, String>, CloudCachedEntityService> caches = new HashMap<>();
    private ObjectFactory objectFactory = new ObjectFactory();

    private CloudCachedEntityService getCache(String accessKey, String privateKey) {
        Pair key = Pair.of(accessKey, privateKey);
        CloudCachedEntityService result = caches.get(key);
        if (result == null) {
            result = new CloudCachedEntityService(new CloudServicesRegistry(accessKey, privateKey));
            caches.put(key, result);
        }

        return result;
    }

    @Override
    public List<NodeParameter> getNodeParameters(Credentials credentials) throws Exception {
        return Arrays.stream(Parameter.values()).map(Parameter::getNodeParameter).collect(Collectors.toList());
    }

    @Override
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        Parameter p = Parameter.valueOf(parameter);
        String organizationId = getOrganizationId(accessKey, secretKey);
        CloudCachedEntityService cache = getCache(accessKey, secretKey);
        String env = parameters.get(Parameter.environment.name());
        EnvironmentType environment = null;
        if (env != null) {
            Optional<EnvironmentType> o = cache.getEnvironments(organizationId).getEnvironment().stream().filter(e -> e.getName().equals(env)).findAny();
            if (o.isPresent()) {
                environment = o.get();
            }
            //environment = cache.getByHrefOrName(EnvironmentType.class, env);
        }
        switch (p) {
            case environment:
                EnvironmentsType environments = cache.getEnvironments(organizationId);
                return environments.getEnvironment().stream().map(e -> new ParameterValue().value(e.getName())).collect(Collectors.toList());
            case catalog:
                CatalogType catalog = cache.getCatalog(organizationId);
                //EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);
                // TODO - use environment location link and get catalog items for this location
                List<CatalogLocationType> locations = catalog.getLocations().getValue().getLocation();
                return locations.stream()
                        .flatMap(location -> location.getCatalog().getValue().getCatalogEntry().stream())
                        .map(e -> new ParameterValue().value(e.getName()))
                        .collect(Collectors.toList());
            case subnet:
                if (environment == null) {
                    return Collections.emptyList();
                }
                NetworksType networks = cache.getNetworks(TmrkUtils.getIdFromHref(environment.getHref()));
                return networks.getNetwork().stream().map(e -> new ParameterValue().value(e.getName())).collect(Collectors.toList());
            case row:
                if (environment == null) {
                    return Collections.emptyList();
                }
                DeviceLayoutType rows = cache.getRows(TmrkUtils.getIdFromHref(environment.getHref()));
                return rows.getRows().getValue().getRow().stream()
                        .map(e -> new ParameterValue().value(e.getName()))
                        .collect(Collectors.toList());
            case group:
                String rowName = parameters.get(Parameter.row.name());
                if (environment == null || rowName == null) {
                    return Collections.emptyList();
                }

                DeviceLayoutType envRows = cache.getRows(TmrkUtils.getIdFromHref(environment.getHref()));
                LayoutRowType row = envRows.getRows().getValue().getRow().stream()
                        .filter(r -> r.getName().equals(rowName))
                        .findAny()
                        .get();
                return row.getGroups().getValue().getGroup().stream()
                        .map(e -> new ParameterValue().value(e.getName()))
                        .collect(Collectors.toList());

            case cpu:
                return ALLOWED_CPU_VALUES.stream().map(i -> new ParameterValue().value(Integer.toString(i))).collect(Collectors.toList());
            case storage:
            case name:
            case memory:
                return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private String getOrganizationId(String accessKey, String privateKey) {
//        return "1927849";

        OrganizationsType organizations = new CloudServicesRegistry(accessKey, privateKey).getOrganizationService().getOrganizations();
        // TODO - for now - get the first organization. Consider handling multiple organizations.
        OrganizationType organization = organizations.getOrganization().get(0);
        return TmrkUtils.getIdFromHref(organization.getHref());
    }

    @Override
    public CreateNodeResponse createNode(Credentials credentials, Map<String, String> parameters) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();

        CloudCachedEntityService cache = getCache(accessKey, secretKey);
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        String cpu = parameters.get(Parameter.cpu.name());
        String memory = parameters.get(Parameter.memory.name());
        String storage = parameters.get(Parameter.storage.name());
        String name = parameters.get(Parameter.name.name());
        String envStr = parameters.get(Parameter.environment.name());
        String netStr = parameters.get(Parameter.subnet.name());
        String grpStr = parameters.get(Parameter.group.name());
        ;
        String rowStr = parameters.get(Parameter.row.name());
        String catStr = parameters.get(Parameter.catalog.name());

        CreateVm createVm = new CreateVm();
        createVm.setVmName(name);
        createVm.setCatalog(catStr);
        createVm.setCpuCount(cpu);
        createVm.setEnvironment(envStr);
        createVm.setGroup(grpStr);
        createVm.setRow(rowStr);
        createVm.setMemory(memory);
        createVm.setNetwork(netStr);
        // For now - use the first available compute pool
        //createVm.setComputePool();

        // Create vm
        EnvironmentType env = cache.getByHrefOrName(EnvironmentType.class, envStr);
        String idFromHref = TmrkUtils.getIdFromHref(env.getHref());
        List<ComputePoolReferenceType> computePools = cache
                .getComputePools(idFromHref).getComputePool();
        String poolId = TmrkUtils.getIdFromHref(computePools.get(0).getHref());
        ImportVirtualMachineType vm = createTmrkVm(createVm, env, cache);
        VirtualMachineType createdVm;
        try {
            createdVm = registry.getVirtualMachineService().importVirtualMachineFromCatalog(poolId, vm);
        } catch (Exception e) {
            NodeUtil.logError(details, "Can't create node", e);
            return new CreateNodeResponse().details(details);
        }

        String vmId = TmrkUtils.getIdFromHref(createdVm.getHref());
        NodeUtil.logInfo(details, "VM created with id: " + vmId);

        // Allocate the disk space if needed
        updateHardwareConfiguration(vmId, Integer.parseInt(storage), Integer.parseInt(cpu), Integer.parseInt(memory), registry);
        NodeUtil.logInfo(details, "VM hardware configuaration updated");
        startupVm(vmId, registry);
        String ip = getIpAddress(vmId, 180, registry);
        NodeUtil.logInfo(details, "VM started with ip " + ip);
        assignIp(vmId, netStr, ip, cache, registry);
        NodeUtil.logInfo(details, "IP " + ip + " assigned to VM " + vmId);
        return new CreateNodeResponse().details(details.status(ExecutionDetails.StatusEnum.OK)).nodeId(vmId);
    }

    private void startupVm(String vmId, CloudServicesRegistry registry) {
        //log.info("Starting VM " + vmId);
        waitUntilMachineIsNotReady(vmId, 1000, registry);

        TaskType task = registry.getVirtualMachineService().actionPowerOnMachine(vmId);
        task.setName("power on virtual machine");
        waitUntilTaskNotFinished(task, registry);
    }

    private void assignIp(String vmId, String network, String ipAddress, CloudCachedEntityService cache, CloudServicesRegistry registry) {
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
        NetworksType networks = cache.getByHrefOrName(NetworksType.class, network);

        String networkHref = networks.getHref();
        dn.setHref(networkHref);
        DeviceIpsType ips = objectFactory.createDeviceIpsType();

        ips.getIpAddress().add(ipAddress);

        dn.setIpAddresses(objectFactory.createDeviceNetworkTypeIpAddresses(ips));
        dnt.getNetwork().add(dn);
        assignedIp.setNetworks(objectFactory.createAssignedIpAddressesTypeNetworks(dnt));
        TaskType task = registry.getVirtualMachineService().editVirtulMachineAssignedIp(vmId, assignedIp);
        task.setName("assign ip address " + ipAddress + " to " + vmId);
        waitUntilTaskNotFinished(task, registry);
    }

    private String getIpAddress(String vmId, int timeout, CloudServicesRegistry registry) {
        String address = null;
        long start = System.currentTimeMillis();
        while (address == null && System.currentTimeMillis() - start < timeout * 1000) {
            address = getIpAddress(vmId, registry);
        }

        if (address == null) {
            throw new RuntimeException("Cannot obtain IP address for VM " + vmId);
        } else {
//            log.info("Successfully obtained IP address " + address + " for VM " + vmId);
        }

        return address;
    }

    private String getIpAddress(String vmId, CloudServicesRegistry registry) {
//        log.info("Trying to obtain IP address for VM " + vmId);
        VirtualMachineType selectedVm = registry.getVirtualMachineService().getVirtualMachineById(vmId);

        HardwareConfigurationType hwconfig = selectedVm.getHardwareConfiguration().getValue();
        NicsType nics = hwconfig.getNics().getValue();
        NetworkReferenceType network = nics.getNic().get(0).getNetwork();
        String networkId = network.getHref().substring(network.getHref().lastIndexOf("/") + 1, network.getHref()
                .length());

        NetworkType networkType = registry.getNetworkService().getNetworkById(networkId);

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
//            log.info("IP address for VM " + vmId + " is not available.");
        }

        return selectedVmIpAddress;
    }

    private ImportVirtualMachineType createTmrkVm(CreateVm cmd, EnvironmentType env, CloudCachedEntityService cache) {
        ImportVirtualMachineType vm = new ImportVirtualMachineType();

        /*
         * set vm name
         */
        vm.setName(cmd.getVmName());
        /*
         * set vm processor count
         */
        vm.setProcessorCount(Integer.valueOf(cmd.getCpuCount()));

        /*
         * set vm memory
         */
        ResourceUnitType resourceUnitType = new ResourceUnitType();
        resourceUnitType.setUnit(objectFactory.createResourceUnitTypeUnit("MB"));
        resourceUnitType.setValue(new BigDecimal(cmd.getMemory()));
        vm.setMemory(resourceUnitType);

        /*
         * set vm layout
         */
        LayoutRequestType layout = createLayoutRequest(cmd.getRow(), cmd.getGroup(), env, cache);
        vm.setLayout(layout);

        /*
         * set vm catalog
         */
        ReferenceType catalogRef = new ReferenceType();
        CatalogEntryType catalog = cache.getByHrefOrName(CatalogEntryType.class, cmd.getCatalog());
        catalogRef.setHref(catalog.getHref());
        vm.setCatalogEntry(catalogRef);

        /*
         * set vm network configuration
         */
        ImportNetworkMappingsType importNetMap = new ImportNetworkMappingsType();

        CatalogEntryConfigurationType catEntryConfig =
                cache.getByHrefOrName(CatalogEntryConfigurationType.class, catalog.getConfiguration().getValue()
                        .getHref());
        CatalogNetworkMappingsType networkMappingsType = catEntryConfig.getNetworkMappings().getValue();
        List<CatalogNetworkMappingType> networkMappings = networkMappingsType.getNetworkMapping();
        ImportNetworkMappingType networkMapping;
        for (CatalogNetworkMappingType networkMap : networkMappings) {
            networkMapping = new ImportNetworkMappingType();
            networkMapping.setName(networkMap.getName().getValue());
            importNetMap.getNetworkMapping().add(networkMapping);
        }

        NetworksType networks = cache.getByHrefOrName(NetworksType.class, cmd.getNetwork());
        ReferenceType rtNet = null;
        List<ImportNetworkMappingType> inmt = importNetMap.getNetworkMapping();
        for (ImportNetworkMappingType impNetMap : inmt) {
            rtNet = new ReferenceType();
            rtNet.setHref(networks.getHref());
            rtNet.setName(networks.getName());
            impNetMap.setNetwork(rtNet);
        }

        vm.setNetworkMappings(importNetMap);

        return vm;
    }

    private LayoutRequestType createLayoutRequest(String row, String group, EnvironmentType env, CloudCachedEntityService cache) {
        DeviceLayoutType tmrkLayout = cache.getRows(TmrkUtils.getIdFromHref(env.getHref()));
        RowsType rows = tmrkLayout.getRows().getValue();
        LayoutRowType rowType = null;
        for (LayoutRowType rowT : rows.getRow()) {
            if (rowT.getName().equals(row)) {
                rowType = rowT;
                break;
            }
        }

        LayoutRequestType layout = new LayoutRequestType();
        ReferenceType rt = new ReferenceType();
        layout.setGroup(objectFactory.createLayoutReferenceTypeGroup(rt));

        GroupsType groups = rowType.getGroups().getValue();
        for (LayoutGroupType lgt : groups.getGroup()) {
            if (lgt.getName().equals(group)) {
                rt.setHref(lgt.getHref());
                rt.setType(lgt.getType());
                break;
            }
        }
        return layout;
    }

    public void updateHardwareConfiguration(String vmId, int storage, int cpu, int memory, CloudServicesRegistry registry) {
        //log.info("Updating hardware configuration of VM " + vmId);
        VirtualMachineService vmService = registry.getVirtualMachineService();
        if (storage > 512) {
//            log.error("Cannot set storage to more than 512GB, but value is: " + storage);
            return;
        }
        if (memory % 4 != 0) {
//            log.error("Memory should be multiple of 4 but was: " + memory);
            return;
        }
        if (!ALLOWED_CPU_VALUES.contains(cpu)) {
//            log.error("Invalid cpu count: " + cpu);
            return;
        }

        waitUntilMachineIsNotReady(vmId, 1000, registry);

        boolean needShutdown = false;
        boolean nothingToDo = true;

        HardwareConfigurationType hardwareConfiguration = vmService.getVirtualMachineHardwareConfiguration(vmId);
        VirtualDiskType disk1 = hardwareConfiguration.getDisks().getValue().getDisk().get(0);
        int currentStorage = disk1.getSize().getValue().intValue();
        if (currentStorage != storage) {
            if (currentStorage > storage) {
//                log.error("Cannot set storage to a value less than current (shrink drive). Current: " +
//                    currentStorage + ", new: " + storage);
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
            VirtualMachineType vm = vmService.getVirtualMachineById(vmId);
            boolean poweredOn = vm.getPoweredOn().getValue();
            if (poweredOn) {
                shutdownVm(vmId, registry);
                needStartup = true;
            }
        }

        TaskType task = vmService.editVirtualMachineHardwareConfiguration(vmId, objectFactory
                .createHardwareConfiguration(hardwareConfiguration));
        task.setName("edit virtual machine hardware configuration");
        waitUntilTaskNotFinished(task, registry);

        if (needStartup) {
            startupVm(vmId, registry);
        }
    }

    private void shutdownVm(String vmId, CloudServicesRegistry registry) {
        //    log.info("Shutting down VM " + vmId);
        VirtualMachineService vmService = registry.getVirtualMachineService();
        VirtualMachineType vm = waitUntilMachineIsNotReady(vmId, 1000, registry);
        if (vm == null) {
//            log.warn("VM " + vmId + " not ready - skip shutdown");
            return;
        }

        if (!vm.getPoweredOn().getValue()) {
//            log.info("VM " + vmId + " is not powered on - skip shutdown");
            return;
        }

        TaskType task = vmService.actionShutdownMachine(vmId);
        task.setName("shutdown virtual machine");
        waitUntilTaskNotFinished(task, registry);
        waitUntilMachineIsPoweredOff(vmId, registry);
    }

    private void waitUntilMachineIsPoweredOff(String vmId, CloudServicesRegistry registry) {
        Callable<Boolean> poll = () -> {
            VirtualMachineType virtualMachineType = registry.getVirtualMachineService().getVirtualMachineById(vmId);
            return !virtualMachineType.getPoweredOn().getValue();
        };

        new SynchronousPoller().poll(
                poll, x -> x,
                1, VZ_TASK_TIMEOUT_SEC,
                "waiting for node " + vmId + " to become powered off"
        );
    }

    private void waitUntilTaskNotFinished(TaskType task, CloudServicesRegistry registry) {
        String taskHref = task.getHref();

        Callable<TaskStatus> poll = () -> {
            TaskType obtainedTask = registry.getTaskService().getTaskById(TmrkUtils.getIdFromHref(taskHref));
            return obtainedTask.getStatus().getValue();
        };
        Predicate<TaskStatus> check =
                (status) -> !(TaskStatus.QUEUED.equals(status) || TaskStatus.RUNNING.equals(status));
        new SynchronousPoller().poll(
                poll, check,
                1, VZ_TASK_TIMEOUT_SEC,
                "waiting for task '" + task.getName() + "' to complete."
        );
    }

    private VirtualMachineType waitUntilMachineIsNotReady(String vmId, int timeout, CloudServicesRegistry registry) {
        VirtualMachineService vmService = registry.getVirtualMachineService();
        List<VirtualMachineStatus> badStatuses = Arrays.asList(
                VirtualMachineStatus.COPY_IN_PROGRESS,
                VirtualMachineStatus.TASK_IN_PROGRESS,
                VirtualMachineStatus.NOT_DEPLOYED,
                VirtualMachineStatus.ORPHANED
        );

        Callable<VirtualMachineType> poll = () -> vmService.getVirtualMachineById(vmId);
        Predicate<VirtualMachineType> check = (virtualMachineType) -> {
            VirtualMachineStatus status = virtualMachineType.getStatus().getValue();
            return !badStatuses.contains(status);
        };

        VirtualMachineType result = null;

        try {
            result = new SynchronousPoller().poll(
                    poll, check,
                    1, VZ_TASK_TIMEOUT_SEC,
                    "waiting for virtual machine " + vmId + " to become ready"
            );
        } catch (Exception e) {
//            log.error("Virtual machine " + vmId + " is not ready", e);
        }

        return result;
    }

    @Override
    public NodeInfo getNode(Credentials credentials, String nodeId) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        CloudCachedEntityService cache = getCache(accessKey, secretKey);
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        VirtualMachineType vm = registry.getVirtualMachineService().getVirtualMachineById(nodeId);
        NodeInfo.StatusEnum status;
        switch (vm.getStatus().getValue()) {
            case DEPLOYED:
                status = NodeInfo.StatusEnum.RUNNING;
                break;
            case NOT_DEPLOYED:
            case ORPHANED:
            case TASK_IN_PROGRESS:
            case COPY_IN_PROGRESS:
            default:
                status = NodeInfo.StatusEnum.PENDING;
        }
        ;
        NodeInfo info = new NodeInfo().status(status).ip(getIpAddress(nodeId, registry)).id(nodeId);
        return info;
    }

    @Override
    public ExecutionDetails updateNode(Credentials credentials, String nodeId, Node node) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String region = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        NodeUtil.logInfo(details, "TODO");
        // TODO
        return details.status(ExecutionDetails.StatusEnum.OK);

    }

    @Override
    public ExecutionDetails deleteNode(Credentials credentials, String nodeId) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        shutdownVM(nodeId, registry);
        NodeUtil.logInfo(details, "Shutdown vm " + nodeId);
        deleteVm(nodeId, registry);
        NodeUtil.logInfo(details, "Delete vm " + nodeId);
        return details;
    }

    private void shutdownVM(String nodeId, CloudServicesRegistry registry) {
        VirtualMachineType vm = waitUntilMachineIsNotReady(nodeId, 1000, registry);
        if (vm == null) {
            //    log.warn("VM " + vmId + " not ready - skip shutdown");
            return;
        }
        if (!vm.getPoweredOn().getValue()) {
            //        log.info("VM " + vmId + " is not powered on - skip shutdown");
            return;
        }

        TaskType task = registry.getVirtualMachineService().actionShutdownMachine(nodeId);
        task.setName("shutdown virtual machine");
        waitUntilTaskNotFinished(task, registry);
        waitUntilMachineIsPoweredOff(nodeId, registry);
    }

    private void deleteVm(String vmId, CloudServicesRegistry registry) {
        //log.info("Deleting VM " + vmId);

        VirtualMachineType vm = waitUntilMachineIsNotReady(vmId, 1000, registry);
        if (vm == null) {
//            log.info("VM " + vmId + " not ready - skip deletion");
            return;
        }
        TaskType task = registry.getVirtualMachineService().removeVirtualMachine(vmId);
        task.setName("remove virtual machine");
        waitUntilTaskNotFinished(task, registry);
    }

    @Override
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) throws Exception {
        return null;
    }

    @Override
    public ExecutionDetails updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) throws Exception {
        ExecutionDetails details = new ExecutionDetails();
        return details;

    }

    private enum Parameter {
        name("Node name", true, null, false, false),
        environment("Verizon environment", true, null, true, true),
        catalog("Verizon catalog", true, null, true, true, environment),
        storage("Storage size", true, "20", false, false),
        cpu("CPU count", false, null, false, false),
        memory("memory", false, null, false, false),
        subnet("Subnet", true, null, true, true, environment),
        row("Verizon Row", true, null, true, true, environment),
        group("Verizon group", true, null, true, true, row),
        tags("tags", true, null, false, true);

        private NodeParameter nodeParameter;

        Parameter(String description, boolean required, String defaultValue, boolean canSuggest, boolean strictSuggest, Parameter... args) {
            List<String> argsList = Arrays.stream(args).map(Enum::name).collect(Collectors.toList());
            nodeParameter = new NodeParameter().name(name()).description(description)
                    .required(required).defaultValue(defaultValue)
                    .canSuggest(canSuggest).strictSuggest(strictSuggest).args(argsList);
        }

        public NodeParameter getNodeParameter() {
            return nodeParameter;
        }

    }

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

}

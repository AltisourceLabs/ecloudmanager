package org.ecloudmanager.node.verizon;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;
import org.ecloudmanager.node.util.SynchronousPoller;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ecloudmanager.tmrk.cloudapi.model.VirtualMachineStatus.NOT_DEPLOYED;

public class VerizonNodeAPI implements NodeBaseAPI {
    private static final Set<Integer> ALLOWED_CPU_VALUES = Sets.newHashSet(1, 2, 4, 8);
    private static final long VZ_TASK_TIMEOUT_SEC = 1000;
    static Logger log = LoggerFactory.getLogger(VerizonNodeAPI.class);
    private static Map<Pair<String, String>, CloudCachedEntityService> caches = new HashMap<>();
    private static APIInfo API_INFO = new APIInfo().id("VERIZON").description("Verizon Terremark");
    private ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public APIInfo getAPIInfo() {
        return API_INFO;
    }

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
    public List<ParameterValue> getNodeParameterValues(Credentials credentials, String parameter, Map<String, String> parameters) {
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
    public NodeInfo createNode(Credentials credentials, Map<String, String> parameters) {
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
        String envId = TmrkUtils.getIdFromHref(env.getHref());
        List<ComputePoolReferenceType> computePools = cache
                .getComputePools(envId).getComputePool();
        String poolId = TmrkUtils.getIdFromHref(computePools.get(0).getHref());
        ImportVirtualMachineType vm = createTmrkVm(createVm, env, cache, registry);
        VirtualMachineType createdVm;
        createdVm = registry.getVirtualMachineService().importVirtualMachineFromCatalog(poolId, vm);

        String vmId = TmrkUtils.getIdFromHref(createdVm.getHref());
        log.info("VM created with id: " + vmId);

//        // Allocate the disk space if needed
//        updateHardwareConfiguration(vmId, Integer.parseInt(storage), Integer.parseInt(cpu), Integer.parseInt(memory), registry);
//        log.info("VM hardware configuaration updated");
        startupVm(vmId, registry, false);

        return getNode(credentials, envId + ":" + vmId);
    }

    private void startupVm(String vmId, CloudServicesRegistry registry, boolean wait) {
        //log.info("Starting VM " + vmId);
        waitUntilMachineIsNotReady(vmId, 1000, registry);

        TaskType task = registry.getVirtualMachineService().actionPowerOnMachine(vmId);
        task.setName("power on virtual machine");
        if (wait) {
            waitUntilTaskNotFinished(task, registry);
        }
    }

    private void assignIp(String vmId, String networkHref, String ipAddress, CloudServicesRegistry registry) {
        AssignedIpAddressesType assignedIp = objectFactory.createAssignedIpAddressesType();
        ArrayOfActionType aat = objectFactory.createArrayOfActionType();
        ActionType at = objectFactory.createActionType();
        at.setHref("/cloudapi/ecloud/virtualMachines/" + vmId + "/assignedIps");
        at.setName("edit");
        aat.getAction().add(at);
        assignedIp.setActions(objectFactory.createResourceTypeActions(aat));
        DeviceNetworkType dn = objectFactory.createDeviceNetworkType();
        DeviceNetworksType dnt = objectFactory.createDeviceNetworksType();
        String network = TmrkUtils.getIdFromHref(networkHref);
        dn.setName(network);
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

    private ImportVirtualMachineType createTmrkVm(CreateVm cmd, EnvironmentType env, CloudCachedEntityService cache, CloudServicesRegistry registry) {
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
//        CatalogEntryType catalog = cache.getByHrefOrName(CatalogEntryType.class, cmd.getCatalog());
        catalogRef.setHref(cache.getHref(CatalogEntryType.class, cmd.getCatalog()));
        CatalogEntryConfigurationType catEntryConfig = registry.getCatalogService().getCatalogEntryConfigurationByCatalogId(TmrkUtils.getIdFromHref(catalogRef.getHref()));
        vm.setCatalogEntry(catalogRef);

        /*
         * set vm network configuration
         */
        ImportNetworkMappingsType importNetMap = new ImportNetworkMappingsType();

//        CatalogEntryConfigurationType catEntryConfig =
//                cache.getByHrefOrName(CatalogEntryConfigurationType.class, catalog.getConfiguration().getValue()
//                        .getHref());
        CatalogNetworkMappingsType networkMappingsType = catEntryConfig.getNetworkMappings().getValue();
        List<CatalogNetworkMappingType> networkMappings = networkMappingsType.getNetworkMapping();
        ImportNetworkMappingType networkMapping;
        for (CatalogNetworkMappingType networkMap : networkMappings) {
            networkMapping = new ImportNetworkMappingType();
            networkMapping.setName(networkMap.getName().getValue());
            importNetMap.getNetworkMapping().add(networkMapping);
        }
        String networksHRef = cache.getHref(NetworksType.class, cmd.getNetwork());

        ReferenceType rtNet = null;
        List<ImportNetworkMappingType> inmt = importNetMap.getNetworkMapping();
        for (ImportNetworkMappingType impNetMap : inmt) {
            rtNet = new ReferenceType();
            rtNet.setHref(networksHRef);
            rtNet.setName(cmd.getNetwork());
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

    private void updateHardwareConfiguration(String vmId, Integer storage, Integer cpu, Integer memory, CloudServicesRegistry registry) {
        //log.info("Updating hardware configuration of VM " + vmId);

        VirtualMachineService vmService = registry.getVirtualMachineService();
        HardwareConfigurationType hardwareConfiguration = vmService.getVirtualMachineHardwareConfiguration(vmId);
        boolean needShutdown = false;
        boolean nothingToDo = true;
        if (storage != null) {
            if (storage > 512) {
                log.info("Cannot set storage to more than 512GB, but value is: " + storage);
            } else {
                VirtualDiskType disk1 = hardwareConfiguration.getDisks().getValue().getDisk().get(0);
                int currentStorage = disk1.getSize().getValue().intValue();
                if (currentStorage != storage) {
                    if (currentStorage > storage) {
                        log.info("Cannot set storage to a value less than current (shrink drive). Current: " +
                                currentStorage + ", new: " + storage);

                    } else {
                        nothingToDo = false;
                        disk1.getSize().setValue(BigDecimal.valueOf(storage));
                    }
                }
            }
            if (memory != null)
                if (memory % 4 != 0) {
                    log.info("Memory should be multiple of 4 but was: " + memory);
                } else {
                    if (hardwareConfiguration.getMemory().getValue().getValue().intValue() != memory) {
                        needShutdown = true;
                        nothingToDo = false;
                        hardwareConfiguration.getMemory().getValue().setValue(BigDecimal.valueOf(memory));
                    }
            }
        }
        if (cpu != null) {
            if (!ALLOWED_CPU_VALUES.contains(cpu)) {
                log.info("Invalid cpu count: " + cpu);
            } else {
                if (!Objects.equals(hardwareConfiguration.getProcessorCount(), cpu)) {
                    needShutdown = true;
                    nothingToDo = false;
                    hardwareConfiguration.setProcessorCount(cpu);
                }
            }
        }
        if (nothingToDo) {
            return;
        }

        waitUntilMachineIsNotReady(vmId, 1000, registry);
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
            startupVm(vmId, registry, false);
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
                NOT_DEPLOYED,
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
    public NodeInfo getNode(Credentials credentials, String nodeId) {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String envId = nodeId.split(":")[0];
        String vmId = nodeId.split(":")[1];
        CloudCachedEntityService cache = getCache(accessKey, secretKey);
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        VirtualMachineType vm = registry.getVirtualMachineService().getVirtualMachineById(vmId);
        NodeInfo.StatusEnum status;
        String ip = null;
        switch (vm.getStatus().getValue()) {
            case DEPLOYED:
                boolean poweredOn = vm.getPoweredOn().getValue();
                ip = getIpAddress(vmId, registry);
                status = ip == null || !poweredOn ? NodeInfo.StatusEnum.PENDING : NodeInfo.StatusEnum.RUNNING;
                break;
            case NOT_DEPLOYED:
            case ORPHANED:
            case TASK_IN_PROGRESS:
            case COPY_IN_PROGRESS:
            default:
                status = NodeInfo.StatusEnum.PENDING;
        }
        ;
        NodeInfo info = new NodeInfo().status(status).ip(ip).id(nodeId);
        return info;
    }

    @Override
    public NodeInfo configureNode(Credentials credentials, String nodeId, Map<String, String> parameters) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String envId = nodeId.split(":")[0];
        String vmId = nodeId.split(":")[1];
        CloudCachedEntityService cache = getCache(accessKey, secretKey);
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        updateVmNameAndLayout(registry, cache, envId, vmId, parameters.get(Parameter.name.name()), parameters.get(Parameter.group.name()), parameters.get(Parameter.row.name()));
        String cpuStr = parameters.get(Parameter.cpu.name());
        String memoryStr = parameters.get(Parameter.memory.name());
        String storageStr = parameters.get(Parameter.storage.name());
        Integer cpu = cpuStr == null ? null : Integer.parseInt(cpuStr);
        Integer memory = memoryStr == null ? null : Integer.parseInt(memoryStr);
        Integer storage = storageStr == null ? null : Integer.parseInt(storageStr);
        updateHardwareConfiguration(vmId, storage, cpu, memory, registry);
        return getNode(credentials, nodeId);
    }

    private void updateVmNameAndLayout(CloudServicesRegistry registry, CloudCachedEntityService cache, String envStr, String vmId, String name, String group, String row) {
        VirtualMachineType vm = registry.getVirtualMachineService().getVirtualMachineById(vmId);

        if (name != null && !vm.getName().equals(name)) {
            log.info("Renaming VM " + vmId + " from " + vm.getName() + " to " + name);

            vm.setName(name);
            TaskType task = registry.getVirtualMachineService().editVirtualMachine(vmId, vm);
            task.setName("edit virtual machine");
            waitUntilTaskNotFinished(task, registry);
            log.info("VM renamed");
        }
        String currentGroup = vm.getLayout().getValue().getGroup().getValue().getName();
        String currentRow = vm.getLayout().getValue().getRow().getValue().getName();
        String toGroup = group == null ? currentGroup : group;
        String toRow = row == null ? currentRow : row;
        if (!currentGroup.equals(toGroup) || !currentRow.equals(toRow)) {
            EnvironmentType env = cache.getByHrefOrName(EnvironmentType.class, envStr);
            LayoutRequestType layoutRequest = createLayoutRequest(toRow, toGroup, env, cache);
            log.info("Moving VM " + vmId);
            log.info("From group: " + currentGroup + " row: " + currentRow);
            log.info("To group: " + toGroup + " row: " + toRow);
            registry.getVirtualMachineService().moveVirtualMachine(vmId, layoutRequest);
        }
    }

    @Override
    public void deleteNode(Credentials credentials, String nodeId) throws Exception {
        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String envId = nodeId.split(":")[0];
        String id = nodeId.split(":")[1];
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        shutdownVM(id, registry);
        log.info("Shutdown vm " + id);
        deleteVm(id, registry);
        log.info("Delete vm " + id);
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
    public FirewallInfo getNodeFirewallRules(Credentials credentials, String nodeId) {
        // TODO implement
        return null;
    }

    @Override
    public FirewallInfo updateNodeFirewallRules(Credentials credentials, String nodeId, FirewallUpdate firewallUpdate) {


        String accessKey = ((SecretKey) credentials).getName();
        String secretKey = ((SecretKey) credentials).getSecret();
        String envId = nodeId.split(":")[0];
        String vmId = nodeId.split(":")[1];
        CloudServicesRegistry registry = new CloudServicesRegistry(accessKey, secretKey);
        String ip = getNode(credentials, nodeId).getIp();
        if (Strings.isNullOrEmpty(ip)) {
            throw new IllegalStateException("No ip address on node " + nodeId);
        }
        log.info("VM " + vmId + " ip address: " + ip);
        VirtualMachineType vm = registry.getVirtualMachineService().getVirtualMachineById(vmId);
        JAXBElement<DeviceNetworksType> element = vm.getIpAddresses().getValue().getAssignedIpAddresses().getValue().getNetworks();
        boolean assigned = element != null && element.getValue().getNetwork().stream()
                .flatMap(dnt -> dnt.getIpAddresses().getValue().getIpAddress().stream()).anyMatch(ip::equals);
        if (!assigned) {
            assignIp(vmId, getNetworkHref(registry, vmId), ip, registry);
            log.info("Ip " + ip + " assigned to VM " + vmId);
        }

        FirewallAclEndpointType dest = firewallAclEndpointType(registry, vmId);
        firewallUpdate.getCreate().forEach(r -> {
            FirewallAclEndpointType source = null;
            switch (r.getType()) {
                case IP:
                    source = new FirewallAclEndpointType();
                    source.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
                    source.setExternalIpAddress(objectFactory.createFirewallAclEndpointTypeExternalIpAddress(r.getFrom()));
                    break;
                case NODE_ID:
                    source = firewallAclEndpointType(registry, r.getFrom());
                    break;
                case ANY:
                    source = new FirewallAclEndpointType();
                    source.setType(FirewallAclEndpointTypeEnum.ANY);
                    break;
            }

            createFirewallRule(registry, envId, source, dest, r.getPort());
            log.info("Created firewall rule " + source);
        });
        return getNodeFirewallRules(credentials, nodeId);


    }

    private String getNetworkHref(CloudServicesRegistry registry, String vmId) {

        VirtualMachineType selectedVm = registry.getVirtualMachineService().getVirtualMachineById(vmId);

        HardwareConfigurationType hwconfig = selectedVm.getHardwareConfiguration().getValue();
        NicsType nics = hwconfig.getNics().getValue();
        return nics.getNic().get(0).getNetwork().getHref();

    }

    private FirewallAclEndpointType firewallAclEndpointType(CloudServicesRegistry registry, String vmId) {
        String ip = getIpAddress(vmId, registry);
        String networkHref = getNetworkHref(registry, vmId);
        String network = TmrkUtils.getIdFromHref(networkHref);
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        IpAddressReferenceType v = objectFactory.createIpAddressReferenceType();
        String href = "/cloudapi/ecloud/ipaddresses/networks/" + network + "/" + ip;
        v.setHref(href);
        v.setName(ip);
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(v));
        return fwaclet;
    }

    private FirewallAclType createFirewallRule(CloudServicesRegistry registry, String envId, FirewallAclEndpointType source, FirewallAclEndpointType dest, long port) {
        //log.info("Create firewall rule from " + source + " to " + dest + ", port " + port);

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
            result = registry.getFirewallRuleService().createFirewallRuleForEnvironment(envId, request);
        } catch (Throwable t) {
//            log.error("Failed to create firewall rule", t);
            throw t;
        }

        if (result != null) {
//            log.info("Firewall rule was created successfully: " + result.getHref());
        }

        return result;
    }

    private enum Parameter {
        name("Node name", true, true, true, null, false, false),
        environment("Verizon environment", true, false, true, null, true, true),
        catalog("Verizon catalog", true, false, true, null, true, true, environment),
        storage("Storage size", true, true, true, "20", false, false),
        cpu("CPU count", true, true, true, null, false, false),
        memory("memory", true, true, true, null, false, false),
        subnet("Subnet", true, false, true, null, true, true, environment),
        row("Verizon Row", true, true, true, null, true, true, environment),
        group("Verizon group", true, true, true, null, true, true, row),
        tags("tags", false, true, false, null, false, true);

        private NodeParameter nodeParameter;

        Parameter(String description, boolean create, boolean configure, boolean required, String defaultValue, boolean canSuggest, boolean strictSuggest, Parameter... args) {
            List<String> argsList = Arrays.stream(args).map(Enum::name).collect(Collectors.toList());
            nodeParameter = new NodeParameter().name(name()).description(description).create(create).configure(configure)
                    .required(required).defaultValue(defaultValue)
                    .canSuggest(canSuggest).strictSuggest(strictSuggest).args(argsList);
        }

        public NodeParameter getNodeParameter() {
            return nodeParameter;
        }

    }


}

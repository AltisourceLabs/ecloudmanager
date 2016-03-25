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

import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.service.verizon.VmService;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.environment.ComputePoolService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.LayoutService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Wizard controller for VM import action from Catalog
 *
 * @author irosu
 */
@Controller
public class VmWizardController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = -4374674392481729704L;

    private ImportVirtualMachineType virtualMachine = new ImportVirtualMachineType();

    private ImportNetworkMappingsType importNetMap = new ImportNetworkMappingsType();

    @Inject
    private OrganizationService organizationService;

    @Inject
    private NetworkService networkService;

    @Inject
    private VmService vmDeploymentService;

    @Inject
    private ComputePoolService computePoolService;

    @Inject
    private LayoutService layoutService;

    @Inject
    CloudCachedEntityService cacheService;

    private String vmName = null;

    private List<OrganizationType> organizations = null;

    private OrganizationType org = null;

    private List<CatalogEntryType> catalogEntriesByOrg = new ArrayList<>();

    private CatalogEntryType catalogEntryByOrg = null;

    private CatalogEntryConfigurationType catEntryConfig = null;

    private List<EnvironmentType> environments = null;

    private EnvironmentType env = null;

    private List<NetworkReferenceType> networksList = null;

    private NetworkReferenceType selectedNetwork = null;

    private List<ComputePoolReferenceType> computePools = null;

    /* Use computePool to obtain the Layout */
    private ComputePoolReferenceType computePool = null;

//    private DeviceLayoutType deviceLayoutTypeByComputePoolId = null;

//    private DeviceLayoutType deviceLayoutTypeByEnvironmentId = null;

    private List<LayoutRowType> rowLayouts = null;

    private LayoutRowType rowLayout = null;

    private List<LayoutGroupType> groupLayouts = null;

    private LayoutGroupType groupLayout = null;

    private int processorCount;

    private String vmMemory = null;

    private List<VirtualMachineReferenceType> virtualMachinesForselectedComputePool;

    private boolean skip;

    private MindmapNode root;

    private MindmapNode selectedNode;

    @PostConstruct
    public void init() {
        organizations = organizationService.getOrganizations().getOrganization();
    }

    public void handleOrganizationChange() {
        String href = org.getHref();
        String organizationId = href.substring(href.lastIndexOf("/") + 1, href.length());
        CatalogType catalogType = cacheService.getCatalog(organizationId);//catalogService.getCatalogByOrganizationId
        // (organizationId);
        CatalogLocationsType catalogLocations = catalogType.getLocations().getValue();
        List<CatalogLocationType> catalogs = catalogLocations.getLocation();
        for (CatalogLocationType clt : catalogs) {
            catalogEntriesByOrg.addAll(clt.getCatalog().getValue().getCatalogEntry());
        }
        environments = cacheService.getEnvironments(organizationId).getEnvironment();
    }

    public void handleEnvironmentChange() {
        String href = env.getHref();
        String envId = href.substring(href.lastIndexOf("/") + 1, href.length());
        computePools =
            computePoolService.getComputePoolsByEnvironment(envId).getComputePool();
        NetworksType networks = networkService.getNetworks(envId);
        networksList = networks.getNetwork();
        DeviceLayoutType layout = layoutService.getDeviceLayoutByEnvironmentId(envId);
        RowsType rows = layout.getRows().getValue();
        rowLayouts = rows.getRow();

        root = new DefaultMindmapNode("layout", env, "FFCC00", false);
        MindmapNode rowNode;
        for (LayoutRowType row : rowLayouts) {
            rowNode = new DefaultMindmapNode("Row: " + row.getName(), row, "6e9ebf", true);
            root.addNode(rowNode);
        }
    }

//    public void handleComputePoolChange() {
//        virtualMachinesForselectedComputePool = computePool.getVirtualMachines().getValue().getVirtualMachine();
//        deviceLayoutTypeByComputePoolId = layoutService.getDeviceLayoutByComputePoolId(computePool.getHref()
// .substring(computePool.getHref().lastIndexOf("/")+1, computePool.getHref().length()));
//        RowsType rowsType = deviceLayoutTypeByComputePoolId.getRows().getValue();
//        rowLayouts = rowsType.getRow();
//    }

    public void handleGrouplayout() {
        GroupsType groupsType = rowLayout.getGroups().getValue();
        groupLayouts = groupsType.getGroup();
    }

    public void handleNetworkMappingsByCatalogEntryId() {
        catEntryConfig = cacheService.getCatalogConfiguration(TmrkUtils.getIdFromHref(catalogEntryByOrg.getHref()));
        CatalogNetworkMappingsType networkMappingsType = catEntryConfig.getNetworkMappings().getValue();
        List<CatalogNetworkMappingType> networkMappings = networkMappingsType.getNetworkMapping();
        ImportNetworkMappingType networkMapping;
        for (CatalogNetworkMappingType networkMap : networkMappings) {
            networkMapping = new ImportNetworkMappingType();
            networkMapping.setName(networkMap.getName().getValue());
            importNetMap.getNetworkMapping().add(networkMapping);
        }
    }

    public void save() {
        String computePoolId = computePool.getHref();
        virtualMachine.setName(vmName);
        virtualMachine.setProcessorCount(processorCount);
        ResourceUnitType resourceUnitType = new ResourceUnitType();
        ObjectFactory objectFactory = new ObjectFactory();
        resourceUnitType.setUnit(objectFactory.createResourceUnitTypeUnit("MB"));
        resourceUnitType.setValue(new BigDecimal(vmMemory));
        virtualMachine.setMemory(resourceUnitType);

        LayoutRequestType layout = new LayoutRequestType();

        /* Needed when NEW GROUP is used! - page 543 from Terremark doc*/
//        ReferenceType rt = new ReferenceType();
//        rt.setName(rowLayout.getName());
//        rt.setHref(rowLayout.getHref());
//        rt.setAccessible(objectFactory.createReferenceTypeAccessible(true));
//        rt.setType(rowLayout.getType());
//        layout.setRow(objectFactory.createReference(rt));

        ReferenceType rt = new ReferenceType();
        rt.setHref(groupLayout.getHref());
        rt.setType(groupLayout.getType());
        layout.setGroup(objectFactory.createLayoutReferenceTypeGroup(rt));

        virtualMachine.setLayout(layout);

        rt = new ReferenceType();
        rt.setAccessible(objectFactory.createReferenceTypeAccessible(true));
        rt.setName(catalogEntryByOrg.getName());
        rt.setHref(catalogEntryByOrg.getHref());
        rt.setType(catalogEntryByOrg.getType());

        virtualMachine.setCatalogEntry(rt);

        List<ImportNetworkMappingType> inmt = importNetMap.getNetworkMapping();
        for (ImportNetworkMappingType impNetMap : inmt) {
            rt = new ReferenceType();
            rt.setHref(selectedNetwork.getHref());
            rt.setName(selectedNetwork.getName());
            impNetMap.setNetwork(rt);
        }

        virtualMachine.setNetworkMappings(importNetMap);

        try {
            vmDeploymentService.deployVm(TmrkUtils.getIdFromHref(computePoolId), virtualMachine);
//            VirtualMachineType createdVm = virtualMachineService.importVirtualMachineFromCatalog(
//                    TmrkUtils.getIdFromHref(computePoolId),    virtualMachine);
//            List<TaskType> vmTasks = createdVm.getTasks().getValue().getTask();
//            String creationTaskIdHref = vmTasks.get(0).getHref();
//            String creationTaskId = TmrkUtils.getIdFromHref(creationTaskIdHref);
//
//            FacesMessage msg = new FacesMessage("Successful", "VM created :" + virtualMachine.getName());
//            addMessage(null, msg);
//            String href = createdVm.getHref();
//            String vmId = TmrkUtils.getIdFromHref(href);

            navigate(null, "vmdeployments.jsf?faces-redirect=true");
//            navigate(null, "vmdetails.jsf?faces-redirect=true&vmId=" + vmId + "&creationTaskId=" + creationTaskId);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failure", "VM failed :" +
                virtualMachine.getName());
            addMessage(null, msg);
        }
    }

    public String onFlowProcess(FlowEvent event) {
        if (skip) {
            skip = false;   //reset in case user goes back
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public List<OrganizationType> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationType> organizations) {
        this.organizations = organizations;
    }

    public OrganizationType getOrg() {
        return org;
    }

    public void setOrg(OrganizationType org) {
        this.org = org;
    }

    public CatalogEntryType getCatalogEntryByOrg() {
        return catalogEntryByOrg;
    }

    @Produces
    @Named
    public List<CatalogEntryType> getCatalogEntriesByOrg() {
        return catalogEntriesByOrg;
    }

    public void setCatalogEntryByOrg(CatalogEntryType catalogEntryByOrg) {
        this.catalogEntryByOrg = catalogEntryByOrg;
    }

    public void setCatalogEntriesByOrg(List<CatalogEntryType> catalogEntriesByOrg) {
        this.catalogEntriesByOrg = catalogEntriesByOrg;
    }

    public EnvironmentType getEnv() {
        return env;
    }

    public void setEnv(EnvironmentType env) {
        this.env = env;
    }

    public List<ComputePoolReferenceType> getComputePools() {
        return computePools;
    }

    public void setComputePools(List<ComputePoolReferenceType> computePools) {
        this.computePools = computePools;
    }

    public ComputePoolReferenceType getComputePool() {
        return computePool;
    }

    public void setComputePool(ComputePoolReferenceType computePool) {
        this.computePool = computePool;
    }

    public List<LayoutRowType> getRowLayouts() {
        return rowLayouts;
    }

    public void setRowLayouts(List<LayoutRowType> rowLayouts) {
        this.rowLayouts = rowLayouts;
    }

    public List<LayoutGroupType> getGroupLayouts() {
        return groupLayouts;
    }

    public void setGroupLayouts(List<LayoutGroupType> groupLayouts) {
        this.groupLayouts = groupLayouts;
    }

    public LayoutGroupType getGroupLayout() {
        return groupLayout;
    }

    public void setGroupLayout(LayoutGroupType groupLayout) {
        this.groupLayout = groupLayout;
    }

    public LayoutRowType getRowLayout() {
        return rowLayout;
    }

    public void setRowLayout(LayoutRowType rowLayout) {
        this.rowLayout = rowLayout;
    }

    public int getProcessorCount() {
        return processorCount;
    }

    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    public String getVmMemory() {
        return vmMemory;
    }

    public void setVmMemory(String vmMemory) {
        this.vmMemory = vmMemory;
    }

    @Produces
    @Named
    public List<VirtualMachineReferenceType> getVirtualMachinesForselectedComputePool() {
        return virtualMachinesForselectedComputePool;
    }

    public void setVirtualMachinesForselectedComputePool(List<VirtualMachineReferenceType> selectedComputePoolVms) {
        this.virtualMachinesForselectedComputePool = selectedComputePoolVms;
    }

    public List<EnvironmentType> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentType> environments) {
        this.environments = environments;
    }

    public List<NetworkReferenceType> getNetworksList() {
        return networksList;
    }

    public void setNetworksList(List<NetworkReferenceType> networksList) {
        this.networksList = networksList;
    }

    public NetworkReferenceType getSelectedNetwork() {
        return selectedNetwork;
    }

    public void setSelectedNetwork(NetworkReferenceType selectedNetwork) {
        this.selectedNetwork = selectedNetwork;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public MindmapNode getRoot() {
        return root;
    }

    public MindmapNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(MindmapNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void onNodeSelect(SelectEvent event) {
        MindmapNode selectedNode = (MindmapNode) event.getObject();
        //populate if not already loaded
        if (selectedNode.getChildren().isEmpty()) {
            if (selectedNode.getData() instanceof LayoutRowType) {
                LayoutRowType row = (LayoutRowType) selectedNode.getData();
                List<LayoutGroupType> groupsByRowList = row.getGroups().getValue().getGroup();

                if (groupsByRowList != null) {
                    for (LayoutGroupType group : groupsByRowList) {
                        selectedNode.addNode(new DefaultMindmapNode("Group: " + group.getName(), group, "66FF66",
                            true));
                    }
                }
            }
        }
    }

    public void onNodeDblselect(SelectEvent event) {
        this.selectedNode = (MindmapNode) event.getObject();
    }
}

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

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.service.deployment.CreateVm;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;
import org.jetbrains.annotations.NotNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class VmByNamesAdapterService {
    @Inject
    private Logger log;

    @Inject
    private VmService vmDeploymentService;

    @Inject
    private CloudCachedEntityService cacheService;

    private ObjectFactory objectFactory = new ObjectFactory();

    public String importVmFromCatalog(CreateVm command) {
        EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, command.getEnvironment());
        return vmDeploymentService.deployVm(getTmrkComputePoolId(env), createTmrkVm(command, env));
    }

    private ImportVirtualMachineType createTmrkVm(CreateVm cmd, EnvironmentType env) {
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
        LayoutRequestType layout = createLayoutRequest(cmd.getRow(), cmd.getGroup(), env);
        vm.setLayout(layout);

        /*
         * set vm catalog
         */
        ReferenceType catalogRef = new ReferenceType();
        CatalogEntryType catalog = cacheService.getByHrefOrName(CatalogEntryType.class, cmd.getCatalog());
        catalogRef.setHref(catalog.getHref());
        vm.setCatalogEntry(catalogRef);

        /*
         * set vm network configuration
         */
        ImportNetworkMappingsType importNetMap = new ImportNetworkMappingsType();

        CatalogEntryConfigurationType catEntryConfig =
            cacheService.getByHrefOrName(CatalogEntryConfigurationType.class, catalog.getConfiguration().getValue()
                .getHref());
        CatalogNetworkMappingsType networkMappingsType = catEntryConfig.getNetworkMappings().getValue();
        List<CatalogNetworkMappingType> networkMappings = networkMappingsType.getNetworkMapping();
        ImportNetworkMappingType networkMapping;
        for (CatalogNetworkMappingType networkMap : networkMappings) {
            networkMapping = new ImportNetworkMappingType();
            networkMapping.setName(networkMap.getName().getValue());
            importNetMap.getNetworkMapping().add(networkMapping);
        }

        NetworksType networks = cacheService.getByHrefOrName(NetworksType.class, cmd.getNetwork());
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

    @NotNull
    public LayoutRequestType createLayoutRequest(String row, String group, EnvironmentType env) {
        DeviceLayoutType tmrkLayout = cacheService.getRows(TmrkUtils.getIdFromHref(env.getHref()));
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

    private String getTmrkComputePoolId(EnvironmentType env) {
        String idFromHref = TmrkUtils.getIdFromHref(env.getHref());

        List<ComputePoolReferenceType> computePools = cacheService
            .getComputePools(idFromHref).getComputePool();

        return TmrkUtils.getIdFromHref(computePools.get(0).getHref());
    }

}

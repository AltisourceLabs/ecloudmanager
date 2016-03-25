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

import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VmFieldsCompletionService {
    @Inject
    private CloudCachedEntityService cacheService;

    @Inject
    private OrganizationService organizationService;

    public List<String> getEnvironments() {
        String organizationId = getOrganizationId();
        EnvironmentsType environments = cacheService.getEnvironments(organizationId);
        return environments.getEnvironment().stream().map(ResourceType::getName).collect(Collectors.toList());
    }

    public List<String> getCatalogs(String environmentName) {
        CatalogType catalog = cacheService.getCatalog(getOrganizationId());
        //EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);
        // TODO - use environment location link and get catalog items for this location
        List<CatalogLocationType> locations = catalog.getLocations().getValue().getLocation();
        return locations.stream()
            .flatMap(location -> location.getCatalog().getValue().getCatalogEntry().stream())
            .map(ResourceType::getName)
            .collect(Collectors.toList());
    }

    public List<String> getNetworks(String environmentName) {
        EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);
        NetworksType networks = cacheService.getNetworks(TmrkUtils.getIdFromHref(environment.getHref()));
        return networks.getNetwork().stream().map(ReferenceType::getName).collect(Collectors.toList());
    }

    public List<String> getRows(String environmentName) {
        EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);
        DeviceLayoutType rows = cacheService.getRows(TmrkUtils.getIdFromHref(environment.getHref()));
        return rows.getRows().getValue().getRow().stream()
            .map(ResourceType::getName)
            .collect(Collectors.toList());
    }

    public List<String> getGroups(String environmentName, String rowName) {
        EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);
        DeviceLayoutType rows = cacheService.getRows(TmrkUtils.getIdFromHref(environment.getHref()));
        LayoutRowType row = rows.getRows().getValue().getRow().stream()
            .filter(r -> r.getName().equals(rowName))
            .findAny()
            .get();
        return row.getGroups().getValue().getGroup().stream()
            .map(ResourceType::getName)
            .collect(Collectors.toList());
    }

    @Nullable
    private String getOrganizationId() {
//        return "1927849";

        OrganizationsType organizations = organizationService.getOrganizations();
        // TODO - for now - get the first organization. Consider handling multiple organizations.
        OrganizationType organization = organizations.getOrganization().get(0);
        return TmrkUtils.getIdFromHref(organization.getHref());
    }

}

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

package org.ecloudmanager.service.verizon.infrastructure;


import org.ecloudmanager.domain.verizon.VerizonConfiguration;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.service.template.VerizonConfigurationService;
import org.ecloudmanager.tmrk.cloudapi.CloudapiEndpointFactory;
import org.ecloudmanager.tmrk.cloudapi.service.EntityService;
import org.ecloudmanager.tmrk.cloudapi.service.device.VirtualMachineService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.*;
import org.ecloudmanager.tmrk.cloudapi.service.network.FirewallRuleService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.service.network.PublicIpsService;
import org.ecloudmanager.tmrk.cloudapi.service.network.TrustedNetworkGroupService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.CatalogService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.DeviceTagsService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.picketlink.Identity;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.Serializable;


/**
 * Terremark Cloudapi endpoint service registry CDI integration.
 *
 * @author irosu
 */
@Stateless
public class CloudServicesRegistry implements Serializable {
    @Inject
    private VerizonConfigurationService verizonConfigurationService;

    private CloudapiEndpointFactory factory;

    @PostConstruct
    void init() {

        factory = new CloudapiEndpointFactory(() -> {
            VerizonConfiguration verizonConfiguration = verizonConfigurationService.getCurrentConfiguration();
            if (verizonConfiguration.getAccessKey() != null && verizonConfiguration.getPrivateKey() != null) {
                return CloudapiEndpointFactory.createConfiguration(verizonConfiguration.getAccessKey(), verizonConfiguration.getPrivateKey());
            } else {
                return System.getProperties();
            }

        });
        factory.open();
    }

    @PreDestroy
    void release() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    @Produces
    @Service
    public EntityService getEntityService() {
        return factory.createEndpoint(EntityService.class);
    }

    @Produces
    @Service
    public TemplateService getTemplateService() {
        return factory.createEndpoint(TemplateService.class);
    }

    @Produces
    @Service
    public OrganizationService getOrganizationService() {
        return factory.createEndpoint(OrganizationService.class);
    }

    @Produces
    @Service
    public NetworkService getNetworkService() {
        return factory.createEndpoint(NetworkService.class);
    }

    @Produces
    @Service
    public VirtualMachineService getVirtualMachineService() {
        return factory.createEndpoint(VirtualMachineService.class);
    }

    @Produces
    @Service
    public EnvironmentService getEnvironmentService() {
        return factory.createEndpoint(EnvironmentService.class);
    }

    @Produces
    @Service
    public PublicIpsService getPublicIpsService() {
        return factory.createEndpoint(PublicIpsService.class);
    }

    @Produces
    @Service
    public CatalogService getCatalogService() {
        return factory.createEndpoint(CatalogService.class);
    }

    @Produces
    @Service
    public LocationService getLocationService() {
        return factory.createEndpoint(LocationService.class);
    }

    @Produces
    @Service
    public ComputePoolService getComputePoolService() {
        return factory.createEndpoint(ComputePoolService.class);
    }

    @Produces
    @Service
    public LayoutService getLayoutService() {
        return factory.createEndpoint(LayoutService.class);
    }

    @Produces
    @Service
    public LayoutGroupsService getLayoutGroupsService() {
        return factory.createEndpoint(LayoutGroupsService.class);
    }

    @Produces
    @Service
    public LayoutRowsService getLayoutRowsService() {
        return factory.createEndpoint(LayoutRowsService.class);
    }

    @Produces
    @Service
    public TaskService getTaskService() {
        return factory.createEndpoint(TaskService.class);
    }

    @Produces
    @Service
    public DeviceTagsService getDeviceTagsService() {
        return factory.createEndpoint(DeviceTagsService.class);
    }

    @Produces
    @Service
    public FirewallRuleService getFirewallRuleService() {
        return factory.createEndpoint(FirewallRuleService.class);
    }

    @Produces
    @Service
    public TrustedNetworkGroupService getTrustedNetworkGroupService() {
        return factory.createEndpoint(TrustedNetworkGroupService.class);
    }
}

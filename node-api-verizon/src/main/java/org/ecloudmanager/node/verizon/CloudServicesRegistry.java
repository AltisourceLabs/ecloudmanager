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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Terremark Cloudapi endpoint service registry CDI integration.
 *
 * @author irosu
 */

public class CloudServicesRegistry implements Serializable {
    private CloudapiEndpointFactory factory;
    private String accessKey;
    private String privateKey;


    public CloudServicesRegistry(String accessKey, String privateKey) {
        this.accessKey = accessKey;
        this.privateKey = privateKey;
        init();
    }

    private void init() {
        factory = new CloudapiEndpointFactory(() -> {
            return CloudapiEndpointFactory.createConfiguration(accessKey, privateKey);
        });
        factory.open();
    }


    void release() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }


    public EntityService getEntityService() {
        return factory.createEndpoint(EntityService.class);
    }

    public TemplateService getTemplateService() {
        return factory.createEndpoint(TemplateService.class);
    }

    public OrganizationService getOrganizationService() {
        return factory.createEndpoint(OrganizationService.class);
    }

    public NetworkService getNetworkService() {
        return factory.createEndpoint(NetworkService.class);
    }

    public VirtualMachineService getVirtualMachineService() {
        return factory.createEndpoint(VirtualMachineService.class);
    }

    public EnvironmentService getEnvironmentService() {
        return factory.createEndpoint(EnvironmentService.class);
    }

    public PublicIpsService getPublicIpsService() {
        return factory.createEndpoint(PublicIpsService.class);
    }

    public CatalogService getCatalogService() {
        return factory.createEndpoint(CatalogService.class);
    }

    public LocationService getLocationService() {
        return factory.createEndpoint(LocationService.class);
    }

    public ComputePoolService getComputePoolService() {
        return factory.createEndpoint(ComputePoolService.class);
    }

    public LayoutService getLayoutService() {
        return factory.createEndpoint(LayoutService.class);
    }

    public LayoutGroupsService getLayoutGroupsService() {
        return factory.createEndpoint(LayoutGroupsService.class);
    }

    public LayoutRowsService getLayoutRowsService() {
        return factory.createEndpoint(LayoutRowsService.class);
    }

    public TaskService getTaskService() {
        return factory.createEndpoint(TaskService.class);
    }

    public DeviceTagsService getDeviceTagsService() {
        return factory.createEndpoint(DeviceTagsService.class);
    }

    public FirewallRuleService getFirewallRuleService() {
        return factory.createEndpoint(FirewallRuleService.class);
    }

    public TrustedNetworkGroupService getTrustedNetworkGroupService() {
        return factory.createEndpoint(TrustedNetworkGroupService.class);
    }

    <U> U getDelegate(Class<U> serviceType) {
        for (Method m : this.getClass().getMethods()) {
            if (serviceType.equals(m.getReturnType())) {
                try {
                    return (U) m.invoke(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
//        if (serviceType.equals(EntityService.class)) {
//            return (U) getEntityService();
//        }
//        if (serviceType.equals(EnvironmentService.class)) {
//            return (U) getEnvironmentService();
//        }
//        if (serviceType.equals(CatalogService.class)) {
//            return (U) getCatalogService();
//        }
//        if (serviceType.equals(TrustedNetworkGroupService.class)) {
//            return (U) getCatalogService();
//        }

        return null;
    }
}

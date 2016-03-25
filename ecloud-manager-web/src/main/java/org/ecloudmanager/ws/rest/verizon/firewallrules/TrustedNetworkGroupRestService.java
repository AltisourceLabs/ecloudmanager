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

package org.ecloudmanager.ws.rest.verizon.firewallrules;

import org.ecloudmanager.service.verizon.TrustedGroupNetworkService;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

@Path("/trustednetworkgroup")
public class TrustedNetworkGroupRestService {

    @Inject
    private TrustedGroupNetworkService trustedNetworkGroupsService;

    @Inject
    private CloudCachedEntityService cacheService;

    @GET
    @Path("/all/{environmentName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TrustedNetworkGroup> getTrustedNetworkGroups(@PathParam("environmentName") String environmentName) {

        EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);

        String href = env.getHref();

        // Use TmrkUtils
        String envHrefId = getLastElement(href);

        TrustedNetworkGroupsType trustedNetworkGroupsType = trustedNetworkGroupsService.getTrustedNetworkGroups
            (envHrefId);

        List<TrustedNetworkGroup> list = mapToTrustedNetworkGroup(trustedNetworkGroupsType);
        return list;
    }

    @POST
    @Path("/create/{environmentName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<TrustedNetworkGroup> createTrustedNetworkGroups(@PathParam("environmentName") String environmentName,
                                                                TrustedNetworkGroup trustedNetworkGroup) {

        EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);

        String href = env.getHref();

        // Use TmrkUtils
        String envHrefId = getLastElement(href);

        ObjectFactory objectFactory = new ObjectFactory();

        // Hosts
        TrustedNetworkGroupHostsType trustedNetworkGroupHostsType = objectFactory.createTrustedNetworkGroupHostsType();
        List<String> list = trustedNetworkGroup.getIpAddresses();
        if (null != list) {
            for (String ipAddress : list) {
                trustedNetworkGroupHostsType.getIpAddress().add(ipAddress);
            }
        }
        JAXBElement<TrustedNetworkGroupHostsType> hosts = objectFactory.createCreateTrustedNetworkGroupTypeHosts
            (trustedNetworkGroupHostsType);

        // Networks
        TrustedNetworkGroupNetworksType trustedNetworkGroupNetworksType = objectFactory
            .createTrustedNetworkGroupNetworksType();
        ExternalNetworkType externalNetworkType = null;
        for (TrustedNetworkGroupNetworks trustedNetworkGroupNetworks : trustedNetworkGroup.getNetworks()) {
            String ipAddr = trustedNetworkGroupNetworks.getAddress();
            Long size = trustedNetworkGroupNetworks.getSize();
            externalNetworkType = objectFactory.createExternalNetworkType();
            externalNetworkType.setAddress(objectFactory.createExternalNetworkTypeAddress(ipAddr));
            externalNetworkType.setSize(size);
            trustedNetworkGroupNetworksType.getNetwork().add(externalNetworkType);
        }

        JAXBElement<TrustedNetworkGroupNetworksType> networks = objectFactory
            .createCreateTrustedNetworkGroupTypeNetworks(trustedNetworkGroupNetworksType);
        CreateTrustedNetworkGroupType createTrustedNetworkGroupType = objectFactory
            .createCreateTrustedNetworkGroupType();
        createTrustedNetworkGroupType.setName(trustedNetworkGroup.getName());
        createTrustedNetworkGroupType.setHosts(hosts);
        createTrustedNetworkGroupType.setNetworks(networks);
        JAXBElement<CreateTrustedNetworkGroupType> createTrustedNetworkGroupsType = objectFactory
            .createCreateTrustedNetworkGroup(createTrustedNetworkGroupType);
        TrustedNetworkGroupsType trustedNetworkGroupsType = trustedNetworkGroupsService.createTrustedNetworkGroups
            (envHrefId, createTrustedNetworkGroupsType);

        return mapToTrustedNetworkGroup(trustedNetworkGroupsType);
    }

    @DELETE
    @Path("/delete/{trustedGroupName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<TrustedNetworkGroup> deleteTrustedNetworkGroups(@PathParam("trustedGroupName") String name) {

        // Delete API understands HREF, not names. So, we can delete trusted network group only by HREF. 
        // Cache service should support TrustedNetworkGroup.
        return null;
    }

    private List<TrustedNetworkGroup> mapToTrustedNetworkGroup(TrustedNetworkGroupsType trustedNetworkGroupsType) {
        List<TrustedNetworkGroup> list = new ArrayList<TrustedNetworkGroup>();
        TrustedNetworkGroup trustedNetworkGroups = null;
        for (TrustedNetworkGroupType trustedNetworkGroupType : trustedNetworkGroupsType.getTrustedNetworkGroup()) {

            trustedNetworkGroups = new TrustedNetworkGroup();
            trustedNetworkGroups.setName(trustedNetworkGroupType.getName());
            trustedNetworkGroups.setType(trustedNetworkGroupType.getType());
            List<TrustedNetworkGroupNetworks> trustedNetworkGroupNetworks = mapToTrustedNetworkGroupNetworksType
                (trustedNetworkGroupType.getNetworks());
            trustedNetworkGroups.setNetworks(trustedNetworkGroupNetworks);
            List<String> ipAddresses = mapToTrustedNetworkGroupHosts(trustedNetworkGroupType.getHosts());
            trustedNetworkGroups.setIpAddresses(ipAddresses);
            List<Reference> internetServices = mapToInternetServices(trustedNetworkGroupType.getInternetServices());
            trustedNetworkGroups.setInternetServices(internetServices);

            list.add(trustedNetworkGroups);
        }
        return list;
    }

    private List<Reference> mapToInternetServices(JAXBElement<InternetServiceReferencesType> internetServices) {
        List<Reference> referencesList = new ArrayList<Reference>();
        List<ReferenceType> internetServicesList = internetServices.getValue().getInternetService();

        for (ReferenceType referenceType : internetServicesList) {
            Reference reference = mapToReference(referenceType);
            referencesList.add(reference);
        }
        return referencesList;
    }

    // TODO: Remove duplicated in FirewallRules.
    private Reference mapToReference(ReferenceType referenceType) {
        Reference reference = new Reference();
        if (null != referenceType.getAccessible()) {
            reference.setAccessible(referenceType.getAccessible().getValue());
        }
        reference.setHref(referenceType.getHref());
        reference.setName(referenceType.getName());
        reference.setType(referenceType.getType());
        return reference;
    }

    private List<String> mapToTrustedNetworkGroupHosts(JAXBElement<TrustedNetworkGroupHostsType>
                                                           trustedNetworkGroupHostsType) {
        List<String> ipAddressList = null;
        if (null != trustedNetworkGroupHostsType) {
            TrustedNetworkGroupHostsType hosts = trustedNetworkGroupHostsType.getValue();
            ipAddressList = hosts.getIpAddress();
        }
        return ipAddressList;

    }

    private List<TrustedNetworkGroupNetworks> mapToTrustedNetworkGroupNetworksType
        (JAXBElement<TrustedNetworkGroupNetworksType> networks) {
        List<TrustedNetworkGroupNetworks> trustedNetworkGroupNetworksList = new
            ArrayList<TrustedNetworkGroupNetworks>();
        TrustedNetworkGroupNetworks trustedNetworkGroupNetworks = new TrustedNetworkGroupNetworks();

        if (null != networks) {
            List<ExternalNetworkType> externalNetworks = networks.getValue().getNetwork();
            for (ExternalNetworkType externalNetworkType : externalNetworks) {
                trustedNetworkGroupNetworks = new TrustedNetworkGroupNetworks();
                trustedNetworkGroupNetworks.setAddress(externalNetworkType.getAddress().getValue());
                trustedNetworkGroupNetworks.setSize(externalNetworkType.getSize());
                trustedNetworkGroupNetworksList.add(trustedNetworkGroupNetworks);
            }
        }

        return trustedNetworkGroupNetworksList;
    }

    // TODO: Remove duplicated
    private String getLastElement(String href) {
        String[] ele = href.split("/");
        String hrefId = ele[ele.length - 1];
        return hrefId;
    }

}

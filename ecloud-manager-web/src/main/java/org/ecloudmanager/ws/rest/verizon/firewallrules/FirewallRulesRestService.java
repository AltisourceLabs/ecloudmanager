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

import org.ecloudmanager.service.verizon.FirewallAclService;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

@Path("/firewallrules")
public class FirewallRulesRestService {

    @Inject
    private FirewallAclService firewallAclService;

    @Inject
    private CloudCachedEntityService cacheService;

    @GET
    @Path("/all/{environmentName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FirewallAcls getFirewallRules(@PathParam("environmentName") String environmentName) {

        EnvironmentType env = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);

        String href = env.getHref();

        // Use TmrkUtils
        String envHrefId = getLastElement(href);

        FirewallAcls firewallAcls = new FirewallAcls();
        List<FirewallAclType> firewallAclTypes = firewallAclService.getFirewallServiceByEnvironmentId(envHrefId);

        System.out.println("################################################");
        System.out.println("firewall rules size: " + firewallAclTypes.size());
        System.out.println("################################################");

        List<FirewallAcl> firewallAclList = new ArrayList<FirewallAcl>();
        FirewallAcl firewallAcl = null;
        for (FirewallAclType firewallAclType : firewallAclTypes) {

            firewallAcl = new FirewallAcl();

            firewallAcl.setPermission(firewallAclType.getPermission().name());
            firewallAcl.setAclType(firewallAclType.getAclType().name());
            firewallAcl.setHref(firewallAclType.getHref());
            firewallAcl.setPortType(firewallAclType.getPortType().name());
            firewallAcl.setProtocol(firewallAclType.getProtocol().name());

            FirewallAclEndpoint source = mapToSourceOrDestination(firewallAclType.getSource().getValue());
            firewallAcl.setSource(source);

            if (null != firewallAclType.getPortRange()) {

                PortRange portRange = mapToPortRange(firewallAclType.getPortRange().getValue());
                firewallAcl.setPortRange(portRange);
            }

            if (null != firewallAclType.getPortRanges()) {
                List<PortRange> portRanges = mapToPortRanges(firewallAclType.getPortRanges().getValue());
                firewallAcl.setPortRanges(portRanges);
            }

            if (null != firewallAclType.getDestination()) {
                FirewallAclEndpoint destination = mapToSourceOrDestination(firewallAclType.getDestination().getValue());
                firewallAcl.setDestination(destination);
            }
            firewallAclList.add(firewallAcl);
        }
        firewallAcls.setFirewallAcls(firewallAclList);

        return firewallAcls;
    }

    @POST
    @Path("/create/{environmentName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FirewallAclType createFirewallRule(@PathParam("environmentName") String environmentName, FirewallAcl
        firewallAcl) {

        // 10.52.88.128/25
        EnvironmentType environment = cacheService.getByHrefOrName(EnvironmentType.class, environmentName);

        String environmentHref = environment.getHref();

        // Use TmrkUtils
        String envHrefId = getLastElement(environmentHref);

        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType createFirewallAclType = new CreateFirewallAclType();
        // permissions to Allow / Deny
        AclPermissionTypeEnum permission = AclPermissionTypeEnum.fromValue(firewallAcl.getPermission());
        createFirewallAclType.setPermission(permission);
        // protocol type, for example TCP, UDP etc
        createFirewallAclType.setProtocol(ProtocolTypeEnum.fromValue(firewallAcl.getProtocol()));

        // Port types
        AclPortTypeEnum aclPortTypeEnum = AclPortTypeEnum.fromValue(firewallAcl.getPortType());
        PortRangeType portRangeType = null;
        JAXBElement<PortRangeType> portRangeTypeJaxb = null;
        switch (aclPortTypeEnum) {
            case RANGES:
                // Ranges not supported by create firewall request.
                break;
            case RANGE:
            case SPECIFIC:
                portRangeType = mapToPortRangeType(firewallAcl.getPortRange());
                portRangeTypeJaxb = objectFactory.createPortRange(portRangeType);
                createFirewallAclType.setPortRange(portRangeTypeJaxb);
                break;
            case ANY:
                // no action required.
                break;
        }

        // firewall source
        FirewallAclEndpointType source = mapToFirewallAclEndpointType(environment, permission, firewallAcl.getSource());
        createFirewallAclType.setSource(source);

        // firewall destination
        FirewallAclEndpointType desination = mapToFirewallAclEndpointType(environment, permission, firewallAcl
            .getDestination());
        JAXBElement<FirewallAclEndpointType> desinationJaxb = objectFactory.createCreateFirewallAclTypeDestination
            (desination);
        createFirewallAclType.setDestination(desinationJaxb);

        FirewallAclType response = firewallAclService.createFirewallRule(envHrefId, createFirewallAclType);

        return response;
    }

    private String getNetworkHrefByName(EnvironmentType environmentType, String networkName) {
        String networkHref = null;
        JAXBElement<PhysicalDeviceReferencesType> physicalDevicesJaxb = environmentType.getPhysicalDevices();

        JAXBElement<NetworkReferencesType> networkReferencesTypeJaxb = environmentType.getNetworks();
        List<NetworkReferenceType> list = networkReferencesTypeJaxb.getValue().getNetwork();
        for (NetworkReferenceType networkReferenceType : list) {
            if (networkReferenceType.getName().equals(networkName)) {
                networkHref = networkReferenceType.getHref();
                break;
            }
        }
        return networkHref;
    }

    private FirewallAclEndpointType mapToFirewallAclEndpointType(EnvironmentType environmentType,
                                                                 AclPermissionTypeEnum permission,
                                                                 FirewallAclEndpoint firewallAclEndpoint) {
        ObjectFactory objectFactory = new ObjectFactory();

        FirewallAclEndpointType firewallAclEndpointType = objectFactory.createFirewallAclEndpointType();
        if (null != firewallAclEndpoint.getType()) {
            firewallAclEndpointType.setType(FirewallAclEndpointTypeEnum.fromValue(firewallAclEndpoint.getType()));
        }

        String networkHref = null;
        if (null != firewallAclEndpoint.getNetwork()) {
            NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
            networkHref = getNetworkHrefByName(environmentType, firewallAclEndpoint.getNetwork().getName());
            networkReferenceType.setHref(networkHref);
            JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory
                .createFirewallAclEndpointTypeNetwork(networkReferenceType);
            firewallAclEndpointType.setNetwork(networkReferenceTypeJaxb);
        }

        if (null != firewallAclEndpoint.getExternalIpAddress()) {
            JAXBElement<String> externalIpAddress = objectFactory.createFirewallAclEndpointTypeExternalIpAddress
                (firewallAclEndpoint.getExternalIpAddress());
            firewallAclEndpointType.setExternalIpAddress(externalIpAddress);
        }

        // For Firewall type Allow, source type should be either Network or
        // IpAddress.
        if (null != firewallAclEndpoint.getIpAddress()) {
            IpAddressReferenceType ipAddressReferenceType = objectFactory.createIpAddressReferenceType();

            // TODO: Need to find a generic solution to get ipaddresses href.
            // And
            // remove hardcoded string
            if (null != networkHref) {
                StringBuilder sb = new StringBuilder(networkHref);
                sb.insert(sb.indexOf("networks", 0), "ipaddresses/");
                sb.append("/");
                sb.append(firewallAclEndpoint.getIpAddress());
                System.out.println("href ip address: " + sb.toString());
                ipAddressReferenceType.setHref(sb.toString());
            }

            JAXBElement<IpAddressReferenceType> ipAddressReferenceTypeJaxb = objectFactory
                .createFirewallAclEndpointTypeIpAddress(ipAddressReferenceType);
            firewallAclEndpointType.setIpAddress(ipAddressReferenceTypeJaxb);
        }

        if (null != firewallAclEndpoint.getExternalNetwork()) {
            ExternalNetworkType externalNetworkType = objectFactory.createExternalNetworkType();
            JAXBElement<String> externalTypeAddress = objectFactory.createExternalNetworkTypeAddress
                (firewallAclEndpoint.getExternalNetwork().getAddress());
            externalNetworkType.setAddress(externalTypeAddress);
            externalNetworkType.setSize(firewallAclEndpoint.getExternalNetwork().getSize());
            JAXBElement<ExternalNetworkType> externalNetworkTypeJaxb = objectFactory.createExternalNetwork
                (externalNetworkType);
            firewallAclEndpointType.setExternalNetwork(externalNetworkTypeJaxb);
        }

        if (null != firewallAclEndpoint.getTrustedNetworkGroup()) {
            ReferenceType referenceType = objectFactory.createReferenceType();
            // TODO: Mapping
            JAXBElement<ReferenceType> trustedNetworkGroup = objectFactory
                .createFirewallAclEndpointTypeTrustedNetworkGroup(referenceType);
            firewallAclEndpointType.setTrustedNetworkGroup(trustedNetworkGroup);
        }
        return firewallAclEndpointType;
    }

    private PortRangeType mapToPortRangeType(PortRange portRange) {
        PortRangeType portRangeType = new PortRangeType();
        portRangeType.setEnd(portRange.getEnd());
        portRangeType.setStart(portRange.getStart());
        return portRangeType;
    }

    @DELETE
    @Path("/remove/{environmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FirewallAcl> removeFirewallRules(@PathParam("environmentId") String environmentId) {

        return null;
    }

    private List<PortRange> mapToPortRanges(PortRangesType value) {

        List<PortRangeType> portRangesList = value.getPortRange();
        List<PortRange> portRangeList = new ArrayList<PortRange>();

        for (PortRangeType portRangeType : portRangesList) {
            PortRange portRange = mapToPortRange(portRangeType);
            portRangeList.add(portRange);
        }

        return portRangeList;
    }

    private PortRange mapToPortRange(PortRangeType value) {
        PortRange portRange = new PortRange();
        portRange.setEnd(value.getEnd());
        portRange.setStart(value.getStart());
        return portRange;
    }

    private FirewallAclEndpoint mapToSourceOrDestination(FirewallAclEndpointType sourceType) {
        FirewallAclEndpoint source = new FirewallAclEndpoint();

        if (null != sourceType) {

            source.setType(sourceType.getType().name());

            if (null != sourceType.getExternalIpAddress()) {
                source.setExternalIpAddress(sourceType.getExternalIpAddress().getValue());
            }

            if (null != sourceType.getExternalNetwork()) {
                ExternalNetwork externalNetwork = mapToExternalNetwork(sourceType.getExternalNetwork().getValue());
                source.setExternalNetworkType(externalNetwork);
            }

            if (null != sourceType.getIpAddress()) {
                // TODO:
                IpAddressReference ipAddressReference = mapToIpAddress(sourceType.getIpAddress().getValue());
            }

            if (null != sourceType.getNetwork()) {
                NetworkReference network = mapToNetwork(sourceType.getNetwork().getValue());
                source.setNetwork(network);
            }

            if (null != sourceType.getTrustedNetworkGroup()) {
                Reference trustedNetworkGroup = mapToReference(sourceType.getTrustedNetworkGroup().getValue());
                source.setTrustedNetworkGroup(trustedNetworkGroup);
            }
        }
        return source;
    }

    private List<NetworkReference> mapToNetworks(NetworkReferencesType networkReferencesType) {

        List<NetworkReferenceType> networkReferenceTypeList = networkReferencesType.getNetwork();

        List<NetworkReference> list = new ArrayList<>();

        for (NetworkReferenceType networkReferenceType : networkReferenceTypeList) {

            NetworkReference networkReference = mapToNetwork(networkReferenceType);
            list.add(networkReference);
        }

        return list;
    }

    private NetworkReference mapToNetwork(NetworkReferenceType value) {
        NetworkReference networkReference = new NetworkReference();
        if (null != value.getAccessible()) {
            networkReference.setAccessible(value.getAccessible().getValue());
        }
        networkReference.setName(value.getName());
        networkReference.setHref(value.getHref());
        networkReference.setType(value.getType());
        if (null != value.getDescription()) {
            networkReference.setDescription(value.getDescription().getValue());
        }

        if (null != value.getLinks()) {
            ArrayOfLinkType arrayOfLInks = value.getLinks().getValue();
            List<NwLinkType> links = mapToLinks(arrayOfLInks);
            networkReference.setLinks(links);
        }

        networkReference.setNetworkType(value.getNetworkType().name());
        networkReference.setNetworkOverlay(value.isNetworkOverlay());

        return networkReference;
    }

    private List<NwLinkType> mapToLinks(ArrayOfLinkType arrayOfLInks) {

        List<LinkType> linkTypes = arrayOfLInks.getLink();
        NwLinkType nwLinkType = null;
        List<NwLinkType> list = new ArrayList<NwLinkType>();

        for (LinkType linkType : linkTypes) {
            nwLinkType = new NwLinkType();
            nwLinkType.setHref(linkType.getHref());
            nwLinkType.setName(linkType.getName());
            nwLinkType.setRel(linkType.getRel());
            nwLinkType.setType(linkType.getType());

            list.add(nwLinkType);
        }

        return list;
    }

    private IpAddressReference mapToIpAddress(IpAddressReferenceType value) {
        IpAddressReference ipAddressReference = new IpAddressReference();

        if (null != value.getAccessible()) {
            ipAddressReference.setAccessible(value.getAccessible().getValue());
        }
        ipAddressReference.setName(value.getName());
        ipAddressReference.setHref(value.getHref());
        ipAddressReference.setType(value.getType());

        Reference reference = mapToReference(value.getNetwork().getValue());
        ipAddressReference.setNetwork(reference);

        Reference host = mapToReference(value.getHost().getValue());
        ipAddressReference.setHost(host);
        return ipAddressReference;
    }

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

    private ExternalNetwork mapToExternalNetwork(ExternalNetworkType value) {
        ExternalNetwork externalNetwork = new ExternalNetwork();
        externalNetwork.setAddress(value.getAddress().getValue());
        externalNetwork.setSize(externalNetwork.getSize());
        return externalNetwork;
    }

    private String getLastElement(String href) {
        String[] ele = href.split("/");
        String hrefId = ele[ele.length - 1];
        return hrefId;
    }

}

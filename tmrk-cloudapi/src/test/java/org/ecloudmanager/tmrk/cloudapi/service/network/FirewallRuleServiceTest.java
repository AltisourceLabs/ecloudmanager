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

package org.ecloudmanager.tmrk.cloudapi.service.network;

import org.ecloudmanager.tmrk.cloudapi.CloudapiEndpointTestSupport;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBElement;

public class FirewallRuleServiceTest extends CloudapiEndpointTestSupport<FirewallRuleService> {

    @Test
    @Ignore
    public void shouldGetFirewallRulesByEnvironmentId() {

        FirewallAclsType result = endpoint.getFirewallRulesByEnvironmentId("4778");

        Assert.assertNotNull(result);

        print(result);
    }

    @Ignore
    @Test
    public void allowNetworkToAnyDestination() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.NETWORK);

        NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
        networkReferenceType.setHref("/cloudapi/ecloud/networks/10024816");
        JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(networkReferenceType);
        fwaclet.setNetwork(networkReferenceTypeJaxb);
        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.ANY);
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }

    @Ignore
    @Test
    public void allowIpAddressToAnyDestination() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);

        IpAddressReferenceType v = objectFactory.createIpAddressReferenceType();
        v.setHref("/cloudapi/ecloud/ipaddresses/networks/10024815/10.52.78.213");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(v));

        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.ANY);
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }

    @Ignore
    @Test
    public void allowExternalIpforSourceTypeIsInvalid() {
        // The request source type for 'Allow' is not 'Network' or 'IpAddress'
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source type should be Network or IpAddress
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.EXTERNAL_IP);

        IpAddressReferenceType v = objectFactory.createIpAddressReferenceType();
        v.setHref("/cloudapi/ecloud/ipaddresses/networks/10024815/10.52.78.213");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(v));

        NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
        networkReferenceType.setHref("/cloudapi/ecloud/networks/10024816");
        JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(networkReferenceType);
        fwaclet.setNetwork(networkReferenceTypeJaxb);
        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.ANY);
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }

    @Ignore
    @Test
    public void allowIpAddressToIpAddress() {
        // The request source type for 'Allow' is not 'Network' or 'IpAddress'

        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);

        IpAddressReferenceType sourceIpAddress = objectFactory.createIpAddressReferenceType();
        sourceIpAddress.setHref("/cloudapi/ecloud/ipaddresses/networks/10024815/10.52.78.219");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(sourceIpAddress));

        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
        IpAddressReferenceType destIpAddress = objectFactory.createIpAddressReferenceType();
        destIpAddress.setHref("/cloudapi/ecloud/ipaddresses/networks/10024816/10.52.88.238");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(destIpAddress));
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }
    
    @Ignore
    @Test
    public void allowNetworkToNetwork() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.NETWORK);

        NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
        networkReferenceType.setHref("/cloudapi/ecloud/networks/10024816");
        JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(networkReferenceType);
        fwaclet.setNetwork(networkReferenceTypeJaxb);
        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.NETWORK);
        
        NetworkReferenceType destNetworkReferenceType = objectFactory.createNetworkReferenceType();
        destNetworkReferenceType.setHref("/cloudapi/ecloud/networks/10024815");
        JAXBElement<NetworkReferenceType> destNetworkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(destNetworkReferenceType);
        fwaclet.setNetwork(destNetworkReferenceTypeJaxb);
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        
        
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }
    
    @Ignore
    @Test
    public void allowNetworkToIpAddress() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.NETWORK);

        NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
        networkReferenceType.setHref("/cloudapi/ecloud/networks/10024815");
        JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(networkReferenceType);
        fwaclet.setNetwork(networkReferenceTypeJaxb);
        firewallAcl.setSource(fwaclet);

        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
        
        IpAddressReferenceType destIpAddress = objectFactory.createIpAddressReferenceType();
        destIpAddress.setHref("/cloudapi/ecloud/ipaddresses/networks/10024816/10.52.88.238");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(destIpAddress));
        
        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        
        
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }
    
    @Ignore
    @Test
    public void allowIpAddressToNetwork() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateFirewallAclType firewallAcl = objectFactory.createCreateFirewallAclType();

        firewallAcl.setPermission(AclPermissionTypeEnum.ALLOW);
        firewallAcl.setProtocol(ProtocolTypeEnum.ANY);

        // source
        FirewallAclEndpointType fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.IP_ADDRESS);
        IpAddressReferenceType destIpAddress = objectFactory.createIpAddressReferenceType();
        destIpAddress.setHref("/cloudapi/ecloud/ipaddresses/networks/10024816/10.52.88.238");
        fwaclet.setIpAddress(objectFactory.createFirewallAclEndpointTypeIpAddress(destIpAddress));
        firewallAcl.setSource(fwaclet);
        
        // destination
        fwaclet = new FirewallAclEndpointType();
        fwaclet.setType(FirewallAclEndpointTypeEnum.NETWORK);

        NetworkReferenceType networkReferenceType = objectFactory.createNetworkReferenceType();
        networkReferenceType.setHref("/cloudapi/ecloud/networks/10024815");
        JAXBElement<NetworkReferenceType> networkReferenceTypeJaxb = objectFactory.createFirewallAclEndpointTypeNetwork(networkReferenceType);
        fwaclet.setNetwork(networkReferenceTypeJaxb);

        firewallAcl.setDestination(objectFactory.createFirewallAclTypeDestination(fwaclet));
        
        PortRangeType portRange = new PortRangeType();
        portRange.setStart(0L);
        portRange.setEnd(1L);

        FirewallAclType result = endpoint.createFirewallRuleForEnvironment("4778", objectFactory.createCreateFirewallAcl(firewallAcl));

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeFirewallAcl(TmrkUtils.getIdFromHref(href));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }

    @Ignore
    @Test
    public void shouldDeleteFirewallAcl() {
        TaskType removeResult = endpoint.removeFirewallAcl("10069943");

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }
}

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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBElement;

/**
 * Tests TrustedNetworkGroup REST API.
 *  
 * @author rajesh.karavadi
 */
public class TrustedNetworkGroupServiceTest extends CloudapiEndpointTestSupport<TrustedNetworkGroupService> {

    @Test
    @Ignore
    public void shouldGetTrustedNetworkGroupByEnvironmentId() {

        TrustedNetworkGroupsType result = endpoint.getTrustedNetworkGroups("4778");
        print(result);
        Assert.assertNotNull(result);
    }

    @Ignore
    @Test
    public void createAndDeleteTrustedNetworkGroupByEnvironmentId() {
        ObjectFactory objectFactory = new ObjectFactory();

        CreateTrustedNetworkGroupType createTrustedNetworkGroupType = objectFactory.createCreateTrustedNetworkGroupType();
        createTrustedNetworkGroupType.setName("test-trustedgroup03");
        TrustedNetworkGroupHostsType trustedNetworkGroupHosts = objectFactory.createTrustedNetworkGroupHostsType();
        trustedNetworkGroupHosts.getIpAddress().add("192.168.56.1");
        JAXBElement<TrustedNetworkGroupHostsType> trustedNetworkGroupHostsJaxb = objectFactory.createCreateTrustedNetworkGroupTypeHosts(trustedNetworkGroupHosts);
        createTrustedNetworkGroupType.setHosts(trustedNetworkGroupHostsJaxb);

        JAXBElement<CreateTrustedNetworkGroupType> createTrustedNetworkGroupsType = objectFactory.createCreateTrustedNetworkGroup(createTrustedNetworkGroupType);
        print(createTrustedNetworkGroupsType);

        TrustedNetworkGroupsType result = endpoint.createTrustedNetworkGroups("4778", createTrustedNetworkGroupsType);

        Assert.assertNotNull(result);

        String[] ele = result.getHref().split("/");

        String trustedNetworkGroupId = ele[ele.length - 1];
        TaskType taskType = endpoint.deleteTrustedNetworkGroups(trustedNetworkGroupId);

        print(taskType);

    }

    @Ignore
    @Test
    public void deleteTrustedNetworkGroup() {
        TaskType result = endpoint.deleteTrustedNetworkGroups("10003094");
        print(result);
    }
}

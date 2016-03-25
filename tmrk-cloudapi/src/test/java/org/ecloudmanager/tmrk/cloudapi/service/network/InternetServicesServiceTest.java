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

/**
 * @author irosu
 */
public class InternetServicesServiceTest extends CloudapiEndpointTestSupport<InternetServicesService> {

    @Test
    @Ignore
    public void shouldGetInternetServiceById() {
        InternetServiceType result = endpoint.getInternetServiceById("10038198");

        Assert.assertNotNull(result);
        print(result);
    }

    @Ignore
    @Test
    public void shouldCreateAndRemoveInternetServiceForPublicIp() {
        CreateInternetServiceType internetService = new CreateInternetServiceType();
        ObjectFactory objectFactory = new ObjectFactory();
        internetService.setName("onlyForTesting");
        internetService.setProtocol(ProtocolTypeEnum.HTTP);
        internetService.setPort(objectFactory.createInternetServiceTypePort(2020L));
        internetService.setEnabled(false);
        InternetServicePersistenceType ispt = new InternetServicePersistenceType();
        ispt.setType(PersistenceTypeEnum.NONE);
        internetService.setPersistence(objectFactory.createInternetServicePersistence(ispt));
        internetService.setLoadBalancingMethod(objectFactory.createInternetServiceTypeLoadBalancingMethod
            (LoadBalancingMethod.ROUND_ROBIN));

        InternetServiceType result = endpoint.createInternetServiceForPublicIp("10261320", internetService);

        Assert.assertNotNull(result);
        print(result);

        String href = result.getHref();
        TaskType removeResult = endpoint.removeInternetServiceFromEnvironment(href.substring(href.lastIndexOf("/"),
            href.length() - 1));

        Assert.assertNotNull(removeResult);
        print(removeResult);
    }

}

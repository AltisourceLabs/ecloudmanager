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

package org.ecloudmanager.tmrk.cloudapi;

import junit.framework.Assert;
import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CloudapiEndpointFactoryTest {

    private CloudapiEndpointFactory factory = CloudapiTestUtils.getFactory();

    @Before
    public void setup() {
        factory.open();
    }

    @After
    public void teardown() {
        factory.close();
    }

    @Test
    public void shouldCreateEndpointForInterface() {
        // given organization endpoint interface
        Class<OrganizationService> clazz = OrganizationService.class;
        // when create endpoint
        OrganizationService endpoint = factory.createEndpoint(clazz);
        // then should be not null
        Assert.assertNotNull(endpoint);
    }

    @Test(expected = CloudapiException.class)
    public void shouldNotCreateEndpointForClass() {
        // given a class as endpoit definition
        class TestClass {}
        // when create endpoint
        factory.createEndpoint(TestClass.class);
        // then expected exception
    }


}

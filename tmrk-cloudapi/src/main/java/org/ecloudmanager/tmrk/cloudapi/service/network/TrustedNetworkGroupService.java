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

import org.ecloudmanager.tmrk.cloudapi.model.CreateTrustedNetworkGroupType;
import org.ecloudmanager.tmrk.cloudapi.model.TaskType;
import org.ecloudmanager.tmrk.cloudapi.model.TrustedNetworkGroupsType;

import javax.ws.rs.*;
import javax.xml.bind.JAXBElement;

/**
 * Defines TrustedNetworkGroup API to make REST calls to Verizon terremark ecloud.
 */
@Path("/cloudapi/ecloud/TrustedNetworkGroups")
public interface TrustedNetworkGroupService {

    @GET
    @Path("/environments/{environmentId}")
    TrustedNetworkGroupsType getTrustedNetworkGroups(@PathParam("environmentId") String environmentId);
    
    @POST
    @Path("/environments/{environmentId}/action/createTrustedNetworkGroup")
    @Consumes
    TrustedNetworkGroupsType createTrustedNetworkGroups(@PathParam("environmentId") String environmentId, JAXBElement<CreateTrustedNetworkGroupType> createTrustedNetworkGroupsType);
    
    @DELETE
    @Path("/{trustedNetworkGroupId}")
    @Consumes
    TaskType deleteTrustedNetworkGroups(@PathParam("trustedNetworkGroupId") String trustedNetworkGroupId);

}

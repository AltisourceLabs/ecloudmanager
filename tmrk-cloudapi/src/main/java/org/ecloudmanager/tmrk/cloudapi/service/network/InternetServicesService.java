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

import org.ecloudmanager.tmrk.cloudapi.model.CreateInternetServiceType;
import org.ecloudmanager.tmrk.cloudapi.model.InternetServiceType;
import org.ecloudmanager.tmrk.cloudapi.model.TaskType;

import javax.ws.rs.*;

/**
 * 
 * @author irosu
 *
 */
@Path("/cloudapi/ecloud/InternetServices")
public interface InternetServicesService {

    /**
     * More information can be found in documentation for <i>eCloud_API_Update_2015_04_29.pdf, chapter: 7.26.2 Action Internet Services Create
     * @param publicIpId
     * @param internetService
     * @return the InternetServiceType created
     */
    @POST
    @Consumes
    @Path("/publicIps/{publicIpId}/action/createInternetService")
    InternetServiceType createInternetServiceForPublicIp(@PathParam("publicIpId") String publicIpId, CreateInternetServiceType internetService);

    @GET
    @Path("/{internetServiceId}")
    InternetServiceType getInternetServiceById(@PathParam("internetServiceId") String internetServiceId);

    /**
     * The Action Internet Services Remove call removes an Internet service from an environment.
     * <br>If successful, the call returns the task that removed the Internet service.
     *
     * @return the TaskType for Internet Service deletion process
     */
    @DELETE
    @Path("/{internetServiceId}")
    TaskType removeInternetServiceFromEnvironment(@PathParam("internetServiceId") String internetServiceId);
}

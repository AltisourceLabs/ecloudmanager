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

package org.ecloudmanager.tmrk.cloudapi.service.device;

import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.model.*;

import javax.ws.rs.*;
import javax.xml.bind.JAXBElement;

/**
 * HTTP requests handler for endpoint "/templates"
 * 
 * @author irosu
 *
 */
@Path("/cloudapi/ecloud/virtualMachines")
public interface VirtualMachineService {

    @GET
    @Path("/{virtualMachineId}")
    VirtualMachineType getVirtualMachineById(@PathParam("virtualMachineId") String virtualMachineId);

//    @GET
//    @Path("/networks/{networkId}")
//    VirtualMachinesType getVirtualMachinesByNetworkId(@PathParam("networkId") String networkId);

//    @GET
//    @Consumes
//    @Path("/computePools/{computePoolId}")
//    VirtualMachinesType getVirtualMachinesByComputePoolId(@PathParam("computePoolId") String computePoolId) throws CloudapiException;
    /**
     * The Action Virtual Machines Create from Template call creates a new virtual machine from a template in the specified compute pool.
     * <p>For VirtualMachineType:
     * <p>- The name may not be that of another virtual machine, must begin with a letter, may contain only letters, numbers, or hyphens, must not exceed fifteen characters.
     * <p>- <ProcessorCount> is required and refers to the number of processors in the virtual machine. Permitted values are: 1, 2, 4, or 8.
     * <p>- <Memory> is required. Permitted values are in megabytes, <Unit>MB</Unit>, and must be evenly divisible by
     * <p>- When creating an instance-based virtual machine, the processor count and memory capacity must match one of the configurations in the price matrix.
     * <p><i>- Terremark documentation: eCloud_API_Update_2015-04-29.pdf : 7.9.16 Action Virtual Machines Create from Template
     * @param computePoolId
     * @return VirtualMachineType the VirtualMachineType with information regarding the created virtual machine
     */
//    @POST
//    @Path("/computePools/{computePoolId}/action/createVirtualMachine")
//    VirtualMachineType createVirtualMachineFromTemplate(@PathParam("computePoolId") String computePoolId, CreateVirtualMachineType virtualMachine);

//    @POST
//    @Consumes
//    VirtualMachineType createBlankVirtualMachine(@PathParam("computePoolId") String computePoolId, CreateBlankVirtualMachineType blankVirtualMachine);

    /**
     * VM Catalog provides a means to upload complete virtual machines to their location and manage those catalog items.
     * <p> These uploaded virtual machines may be imported into a compute pool as new servers.
     * <p><i> Refer to 'eCloud_API_Update_2015-04-29.pdf' chapter '6.21VM Catalog' for more information.
     *
     * @return
     * @throws CloudapiException
     */
    @POST
//    @Produces("application/xml")
    @Consumes
    @Path("/computePools/{computePoolId}/action/importVirtualMachine")
    VirtualMachineType importVirtualMachineFromCatalog(@PathParam("computePoolId") String computePoolId,ImportVirtualMachineType virtualMachine) throws CloudapiException;
    /**
     * The Action Virtual Machines Remove call removes a specified virtual machine from the compute pool.
     * <p>If successful, the call returns the task that removed the virtual machine.
     * <p><i>Terremark documentation: eCloud_API_Update_2015-04-29.pdf : 7.9.40 Action Virtual Machines Remove
     *
     * @param virtualMachineId
     * @return TaskType the TaskType that removed the Virtual Machine
     */


    @POST
    @Consumes
    @Path("/{virtualMachineId}/action/powerOn")
    TaskType actionPowerOnMachine(@PathParam("virtualMachineId") String virtualMachineId);

    @POST
    @Consumes
    @Path("/{virtualMachineId}/action/powerOff")
    TaskType actionPowerOffMachine(@PathParam("virtualMachineId") String virtualMachineId);

    @POST
    @Consumes
    @Path("/{virtualMachineId}/action/shutdown")
    TaskType actionShutdownMachine(@PathParam("virtualMachineId") String virtualMachineId);

    /**
     *  No more than fifteen disks may be defined.
     *  <br>Within a disk, 'Index' is required and identifies specific disks.
     *  It is an integer with the first disk set to zero and incremented for each additional disk.
     *  <br>'Size' is required. 'Unit' is required and will be "GB" for disks.
     *  <br>'Value' is required, must be greater than zero for new disks and equal to or greater than the current size for existing disks, and must not be greater than 512.
     *  <br>'Name' is optional and ignored if present. When adding a disk, use the next available index number.
     * @param virtualMachineId
     * @param hw
     * @return
     * @throws CloudapiException
     */
    @PUT
    @Consumes
    @Path("/{virtualMachineId}/hardwareConfiguration")
    TaskType editVirtualMachineHardwareConfiguration(@PathParam("virtualMachineId") String virtualMachineId, JAXBElement<HardwareConfigurationType> hw);

    /**
     *  No more than fifteen disks may be defined.
     *  <br>Within a disk, 'Index' is required and identifies specific disks.
     *  It is an integer with the first disk set to zero and incremented for each additional disk.
     *  <br>'Size' is required. 'Unit' is required and will be "GB" for disks.
     *  <br>'Value' is required, must be greater than zero for new disks and equal to or greater than the current size for existing disks, and must not be greater than 512.
     *  <br>'Name' is optional and ignored if present. When adding a disk, use the next available index number.
     * @param virtualMachineId
     * @return
     * @throws CloudapiException
     */
    @GET
    @Consumes
    @Path("/{virtualMachineId}/hardwareConfiguration")
    HardwareConfigurationType getVirtualMachineHardwareConfiguration(@PathParam("virtualMachineId") String virtualMachineId);

    @DELETE
    @Path("/{virtualMachineId}")
    TaskType removeVirtualMachine(@PathParam("virtualMachineId") String virtualMachineId);

    @POST
    @Consumes
    @Path("/{virtualMachineId}/action/move")
    void moveVirtualMachine(@PathParam("virtualMachineId") String virtualMachineId, LayoutRequestType layoutRequestType);

    @PUT
    @Consumes
    @Path("/{virtualMachineId}")
    TaskType editVirtualMachine(@PathParam("virtualMachineId") String virtualMachineId, VirtualMachineType virtualMachineType);

    @PUT
    @Consumes
    @Path("/{virtualMachineId}/assignedIps")
    TaskType editVirtulMachineAssignedIp(@PathParam("virtualMachineId") String virtualMachineId, AssignedIpAddressesType assignedIpAddressesType);
}

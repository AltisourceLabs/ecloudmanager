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

package org.ecloudmanager.ws.rest.verizon.infrastructure;

import org.ecloudmanager.service.verizon.VmHardwareService;
import org.ecloudmanager.service.verizon.infrastructure.CloudCachedEntityService;
import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.ws.rest.verizon.firewallrules.Reference;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/vm")
public class VmHardwareRestService {

    @Inject
    private CloudCachedEntityService cacheService;

    private EnvironmentType env;

    @Inject
    private VmHardwareService vmHardwareService;

    @GET
    @Path("/hardware/{virtualMachineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public VmHardware getVmHardware(
        @PathParam("virtualMachineId") String virtualMachineId) {
        VmHardware vmHardware = null;
        // try {

        HardwareConfigurationType hardwareConfigurationType = vmHardwareService
            .getVmHardware(virtualMachineId);

        vmHardware = mapToVmHardware(hardwareConfigurationType);

        // } catch (CloudapiException e) {
        // throw new BadRequestException(e);
        // }
        return vmHardware;
    }

    @POST
    @Path("/discs/add/{virtualMachineId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addDiscsToVm(
        @PathParam("virtualMachineId") String virtualMachineId,
        VmHardware vmHardware) {
        Response response = null;
        HardwareConfigurationType hardwareConfigurationType = null;
        try {
            List<VmDisk> discsList = vmHardware.getDisks();

            hardwareConfigurationType = vmHardwareService
                .getVmHardware(virtualMachineId);

            DisksType disksType = hardwareConfigurationType.getDisks()
                .getValue();

            addDiscs(discsList, disksType);

            TaskType taskType = vmHardwareService.editVmConfiguration(
                virtualMachineId, hardwareConfigurationType);

            response = mapResponse(taskType);

            System.out.println("Added discs: " + discsList
                + ", to virtualMachineId: " + virtualMachineId);
        } catch (Exception e) {
            // TODO: handle exceptions. for example: Adding disks more than 15
            // is invalid.
            // throw new BadRequestException(e);

            if (null == response) {
                response = new Response();
            }
            response.setErrorMessage(e.getMessage());
        }
        return response;
    }

    @POST
    @Path("/nics/add/{virtualMachineId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addNicsToVm(
        @PathParam("virtualMachineId") String virtualMachineId,
        VmHardware vmHardware) {
        Response response = null;
        try {

            List<VmNic> nicsToAdd = vmHardware.getNics();
            HardwareConfigurationType hardwareConfigurationType = vmHardwareService
                .getVmHardware(virtualMachineId);

            NicsType existingNics = hardwareConfigurationType.getNics()
                .getValue();
            addNic(existingNics, nicsToAdd);

            TaskType taskType = vmHardwareService.editVmConfiguration(
                virtualMachineId, hardwareConfigurationType);

            response = mapResponse(taskType);

            System.out.println("Added nics: " + nicsToAdd
                + ", to virtualMachineId: " + virtualMachineId);
        } catch (CloudapiException e) {
            // throw new BadRequestException(e);
            if (null == response) {
                response = new Response();
            }
            response.setErrorMessage(e.getMessage());
        } finally {
            if (null == response) {
                response = new Response();
                response.setStatus("failed");
            }
        }

        return response;
    }

    private void addNic(NicsType nicsExisting, List<VmNic> nicsToAdd) {
        for (VmNic virtualNic : nicsToAdd) {
            VirtualNicType nic = new VirtualNicType();

            String name = virtualNic.getNetwork().getName();
            NetworkReferenceType networkReference = getNetworkReference(name);
            if (null != networkReference) {
                nic.setNetwork(networkReference);
                nicsExisting.getNic().add(nic);
            }
        }
    }

    private NetworkReferenceType getNetworkReference(String networkName) {
        // TODO: get from cache.
        String environment = "MIA-NON-PROD (npr)";
        env = cacheService.getByHrefOrName(EnvironmentType.class, environment);

        NetworkReferencesType networkReferencesType = env.getNetworks()
            .getValue();

        for (NetworkReferenceType networkReferenceType : networkReferencesType
            .getNetwork()) {
            String existingNetworkName = networkReferenceType.getName();
            if (existingNetworkName.equals(networkName)) {
                return networkReferenceType;
            }
        }

        System.out.println("returning null for: " + networkName);
        return null;
    }

    private void addDiscs(List<VmDisk> discsList, DisksType disksType) {
        if (null != discsList) {
            ObjectFactory objectFactory = new ObjectFactory();
            List<VirtualDiskType> existingDisks = disksType.getDisk();
            int existingDisksSize = existingDisks.size();
            VirtualDiskType lastExistingDisk = existingDisks
                .get(existingDisksSize - 1);
            int lastIndex = lastExistingDisk.getIndex().getValue();
            for (VmDisk vmDisk : discsList) {
                lastIndex++;
                VirtualDiskType disk = new VirtualDiskType();
                disk.setIndex(objectFactory
                    .createVirtualDiskBaseTypeIndex(lastIndex));
                ResourceUnitType rut = new ResourceUnitType();
                rut.setValue(vmDisk.getVmResourceUnit().getValue());
                rut.setUnit(objectFactory.createResourceUnitTypeUnit(vmDisk
                    .getVmResourceUnit().getUnit()));
                disk.setSize(rut);
                existingDisks.add(disk);
            }
        }
    }

    private VmHardware mapToVmHardware(
        HardwareConfigurationType hardwareConfigurationType) {

        VmHardware vmHardware = new VmHardware();

        vmHardware.setProcessorCount(hardwareConfigurationType
            .getProcessorCount().intValue());
        VmResourceUnit memory = mapToVmResourceUnit(hardwareConfigurationType
            .getMemory().getValue());
        vmHardware.setMemory(memory);

        List<VmDisk> disks = mapToDisks(hardwareConfigurationType.getDisks()
            .getValue());
        vmHardware.setDisks(disks);

        List<VmNic> nics = mapToNics(hardwareConfigurationType.getNics()
            .getValue());
        vmHardware.setNics(nics);

        return vmHardware;
    }

    private List<VmNic> mapToNics(NicsType value) {

        List<VirtualNicType> list = value.getNic();
        List<VmNic> vmNics = new ArrayList<>();

        VmNic vmNic = null;
        for (VirtualNicType virtualNicType : list) {
            vmNic = new VmNic();

            vmNic.setName(virtualNicType.getName().getValue());

            VmNetwork vmNetwork = mapToNetwork(virtualNicType.getNetwork());
            vmNic.setNetwork(vmNetwork);

            vmNic.setUnitNumber(virtualNicType.getUnitNumber().getValue());

            vmNics.add(vmNic);
        }

        return vmNics;
    }

    private VmNetwork mapToNetwork(NetworkReferenceType network) {
        VmNetwork vmNetworkType = new VmNetwork();
        if (null != network.getAccessible()) {
            vmNetworkType.setAccessible(network.getAccessible().getValue());
        }
        vmNetworkType.setHref(network.getHref());
        vmNetworkType.setName(network.getName());
        vmNetworkType.setNetworkType(network.getNetworkType().name());
        vmNetworkType.setNetworkOverlay(network.isNetworkOverlay());
        vmNetworkType.setType(network.getType());
        return vmNetworkType;
    }

    private List<VmDisk> mapToDisks(DisksType value) {

        List<VirtualDiskType> list = value.getDisk();
        List<VmDisk> vmDisks = new ArrayList<VmDisk>();

        VmDisk vmDisk = null;
        for (VirtualDiskType virtualDiskType : list) {
            vmDisk = new VmDisk();

            vmDisk.setIndex(virtualDiskType.getIndex().getValue());
            vmDisk.setName(virtualDiskType.getName().getValue());
            vmDisk.setVmResourceUnit(mapToVmResourceUnit(virtualDiskType
                .getSize()));

            vmDisks.add(vmDisk);
        }

        return vmDisks;
    }

    private VmResourceUnit mapToVmResourceUnit(ResourceUnitType resourceUnitType) {
        VmResourceUnit vmResourceUnit = new VmResourceUnit();
        vmResourceUnit.setUnit(resourceUnitType.getUnit().getValue());
        vmResourceUnit.setValue(resourceUnitType.getValue());
        return vmResourceUnit;
    }

    // TODO: duplicated in FirewallRulesService rest class.
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

    private Response mapResponse(TaskType taskType) {
        Response response = new Response();
        try {
            if (null != taskType) {
                response.setOperation(taskType.getOperation().getValue());
                response.setStatus(taskType.getStatus().getValue().name());
                response.setStartTime(taskType.getStartTime().getValue()
                    .toString());
                if (null != taskType.getCompletedTime()) {
                    response.setCompletedTime(String.valueOf(taskType
                        .getCompletedTime().getValue()));
                }

                ReferenceType referenceType = taskType.getInitiatedBy()
                    .getValue();
                Reference reference = mapToReference(referenceType);

                response.setInitiatedBy(reference);
            }
        } catch (Exception e) {
            // TODO: handle globally
            e.printStackTrace();
        }

        return response;
    }

}

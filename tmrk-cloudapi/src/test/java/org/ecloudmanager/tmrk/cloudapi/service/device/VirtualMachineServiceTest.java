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

import org.ecloudmanager.tmrk.cloudapi.CloudapiEndpointTestSupport;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

public class VirtualMachineServiceTest extends CloudapiEndpointTestSupport<VirtualMachineService> {

    @Ignore
    @Test
    public void shouldEditVirtualMachineHardwareConfiguration() {
        ObjectFactory objectFactory = new ObjectFactory();

        HardwareConfigurationType hwConfig = objectFactory.createHardwareConfigurationType();
        hwConfig.setProcessorCount(1);
        ResourceUnitType rut = new ResourceUnitType();
        rut.setValue(new BigDecimal(1024));
        rut.setUnit(objectFactory.createResourceUnitTypeUnit("MB"));
        hwConfig.setMemory(objectFactory.createHardwareConfigurationTypeMemory(rut));
        // disks update
        DisksType disks = new DisksType();
        VirtualDiskType disk = new VirtualDiskType();
        disk.setIndex(objectFactory.createVirtualDiskBaseTypeIndex(0));
        rut = new ResourceUnitType();
        rut.setValue(new BigDecimal(21));
        rut.setUnit(objectFactory.createResourceUnitTypeUnit("GB"));

        disk.setSize(rut);
        disks.getDisk().add(disk);

        disk = new VirtualDiskType();
        disk.setIndex(objectFactory.createVirtualDiskBaseTypeIndex(1));
        rut = new ResourceUnitType();
        rut.setValue(new BigDecimal(2));
        rut.setUnit(objectFactory.createResourceUnitTypeUnit("GB"));

        disk.setSize(rut);
        disks.getDisk().add(disk);

        hwConfig.setDisks(objectFactory.createHardwareConfigurationTypeDisks(disks));

        NicsType nics = new NicsType();
        VirtualNicType nic = new VirtualNicType();
        nic.setUnitNumber(objectFactory.createVirtualNicTypeUnitNumber(7));
        NetworkReferenceType networkReference = new NetworkReferenceType();
        networkReference.setHref("/cloudapi/ecloud/networks/10024815");
        nic.setNetwork(networkReference);
        nics.getNic().add(nic);

        hwConfig.setNics(objectFactory.createHardwareConfigurationTypeNics(nics));
        TaskType result = endpoint.editVirtualMachineHardwareConfiguration("10289325", objectFactory.createVirtualMachineTypeHardwareConfiguration(hwConfig));

        Assert.assertNotNull(result);

        print(result);
    }
}

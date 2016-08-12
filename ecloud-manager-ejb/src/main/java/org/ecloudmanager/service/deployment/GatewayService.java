/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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

package org.ecloudmanager.service.deployment;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.gateway.GatewayDeployment;
import org.ecloudmanager.deployment.vm.GatewayVMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.service.ServiceSupport;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class GatewayService extends ServiceSupport {
    @Inject
    private Logger log;

    public GatewayDeployment create(String name, VirtualMachineTemplate virtualMachineTemplate, String infrastructure) {
        log.info("Creating gateway deployment " + name + ", " + virtualMachineTemplate + ", " + infrastructure);
        //Infrastructure infra = Infrastructure.valueOf(infrastructure);
        GatewayDeployment gatewayDeployment = new GatewayDeployment();
        gatewayDeployment.setName(name);
        gatewayDeployment.setInfrastructure(infrastructure);

        GatewayVMDeployment vmDeployment = new GatewayVMDeployment(virtualMachineTemplate);
        vmDeployment.setName(gatewayDeployment.getName());
        gatewayDeployment.addChild(vmDeployment);
        vmDeployment.getRunlist().stream()
                .flatMap(recipe -> recipe.getEndpoints().stream())
                .forEach(vmDeployment::addChild);

        gatewayDeployment.getPublicEndpoints().add(gatewayDeployment.getName() + ":" + GatewayVMDeployment.HAPROXY_STATS);

        gatewayDeployment.specifyConstraints();
        return gatewayDeployment;
    }

    public void save(GatewayDeployment gatewayDeployment) {
        log.info("Saving " + gatewayDeployment.getName());
        datastore.save(gatewayDeployment);
        fireEntityCreated(gatewayDeployment);
    }

    public void update(GatewayDeployment gatewayDeployment) {
        log.info("Updating " + gatewayDeployment.getName());
        datastore.save(gatewayDeployment);
        fireEntityUpdated(gatewayDeployment);
    }

    public void remove(GatewayDeployment gatewayDeployment) {
        log.info("Deleting " + gatewayDeployment.getName());
        datastore.delete(gatewayDeployment);
        fireEntityDeleted(gatewayDeployment);
    }
}
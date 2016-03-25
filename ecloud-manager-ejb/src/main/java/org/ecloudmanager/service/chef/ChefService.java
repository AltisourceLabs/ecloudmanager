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

package org.ecloudmanager.service.chef;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.vm.VMDeployer;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.domain.Client;
import org.jclouds.chef.domain.Environment;
import org.jclouds.chef.domain.Node;
import org.jclouds.json.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;

@Singleton
public class ChefService {
    @Inject
    private Logger log;
    @Inject
    private Json json;

    @Inject
    private ChefApiService chefApiService;

    @Inject
    private ChefGenerationService chefGenerationService;

    public void createEnvironment(ChefEnvironment chefEnvironment) {
        StringWriter writer = new StringWriter();
        chefGenerationService.generateChefEnv(writer, chefEnvironment);

        ChefApi chefApi = chefApiService.getChefApi(chefEnvironment);

        Environment environment = json.fromJson(writer.toString(), Environment.class);
        if (chefApi.getEnvironment(environment.getName()) == null) {
            log.info("Creating chef environment '" + environment.getName() + "'.");
            chefApi.createEnvironment(environment);
        } else {
            log.error("Chef environment '" + environment.getName() + "' already exists. Skip environment creation.");
        }
    }

    public void deleteEnvironment(ChefEnvironment chefEnvironment) {
        ChefApi chefApi = chefApiService.getChefApi(chefEnvironment);

        String name = chefEnvironment.getTop().getName();
        log.info("Deleting chef environment '" + name + "'.");
        Environment environment = chefApi.deleteEnvironment(name);
        if (environment == null) {
            log.error("An environment '" + name + "' doesn't exist. Nothing to delete.");
        }
    }

    public void deleteNodeAndClient(VMDeployment vmDeployment) {
        String name = vmDeployment.getConfigValue(VMDeployer.VM_NAME);

        ChefApi chefApi = chefApiService.getChefApi(vmDeployment.getChefEnvironment());
        Node node = chefApi.deleteNode(name);
        if (node == null) {
            log.error("A chef node '" + name + "' doesn't exist. Nothing to delete.");
        }
        Client client = chefApi.deleteClient(name);
        if (client == null) {
            log.error("A chef client '" + name + "' doesn't exist. Nothing to delete.");
        }
    }

    public boolean needUpdateChefEnvironment(ChefEnvironment deployment) {
        StringWriter writer = new StringWriter();
        try {
            chefGenerationService.generateChefEnv(writer, deployment);

            ChefApi chefApi = chefApiService.getChefApi(deployment);

            Environment newEnvironment = json.fromJson(writer.toString(), Environment.class);

            Environment oldEnvironment = chefApi.getEnvironment(newEnvironment.getName());
            if (oldEnvironment == null) {
                return true;
            } else {
                return !oldEnvironment.equals(newEnvironment);
            }
        } catch (Exception e) {
            log.warn("Cannot compare existing chef environment with the new one. Environment delete/create actions " +
                "will be scheduled.", e);
            // Something went wrong, will try to recreate env
            return true;
        }
    }
}

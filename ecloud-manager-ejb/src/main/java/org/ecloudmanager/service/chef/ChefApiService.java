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
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.ChefApiMetadata;
import org.jclouds.chef.config.ChefProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class ChefApiService {
    @Inject
    private Logger log;

    private ChefApi createChefApi(String endpoint, String clientName, String clientCredentials, String
        validatorClientName, String validatorPem) {
        log.info("Instantiating chef client API for " + endpoint);
        Properties chefConfig = new Properties();
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, validatorClientName);
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL, validatorPem);
        chefConfig.put(Constants.PROPERTY_RELAX_HOSTNAME, "true");
        chefConfig.put(Constants.PROPERTY_TRUST_ALL_CERTS, "true");

        ContextBuilder builder = ContextBuilder.newBuilder(new ChefApiMetadata())
            .credentials(clientName, clientCredentials)
            .endpoint(endpoint)
            .overrides(chefConfig);

        return builder.buildApi(ChefApi.class);
    }

    public ChefApi getChefApi(ChefEnvironment env) {
        DeploymentObject config = ChefEnvironmentDeployer.getServerConfig(env);
        return createChefApi(
            config.getConfigValue(ChefEnvironmentDeployer.CHEF_SERVER_ADDRESS),
            config.getConfigValue(ChefEnvironmentDeployer.CHEF_CLIENT_NAME),
            config.getConfigValue(ChefEnvironmentDeployer.CHEF_CLIENT_SECRET),
            config.getConfigValue(ChefEnvironmentDeployer.CHEF_VALIDATION_CLIENT_NAME),
            config.getConfigValue(ChefEnvironmentDeployer.CHEF_VALIDATION_CLIENT_SECRET)
        );
    }
}

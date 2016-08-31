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

package org.ecloudmanager.deployment.vm.provisioning;

import org.ecloudmanager.actions.ChefActions;
import org.ecloudmanager.actions.ProvisioningActions;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.node.AsyncNodeAPI;
import org.ecloudmanager.node.model.SecretKey;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.execution.Action;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.inject.spi.CDI;
import java.util.List;

public class ChefProvisioningDeployer implements Deployer<VMDeployment> {
    private static final String CHEF_NODE_CONFIG_NAME = "chefNodeAttributes";
    private final String apiName;
    private final AsyncNodeAPI nodeAPI;
    private ProvisioningActions provisioningActions = CDI.current().select(ProvisioningActions.class).get();
    private ChefActions chefActions = CDI.current().select(ChefActions.class).get();
    private NodeAPIConfigurationService nodeAPIProvider = CDI.current().select(NodeAPIConfigurationService.class).get();
    private SecretKey credentials;

    public ChefProvisioningDeployer(String apiName) {
        this.apiName = apiName;
        nodeAPI = nodeAPIProvider.getAPI(apiName);
        credentials = nodeAPIProvider.getCredentials(apiName);
    }

    private static String constraintDescription(Recipe r, ChefAttribute a) {
        return "Recipe: " + r.getName() + " Chef Attribute: " + a.getName();
    }

    public static String getVersionConstraintFieldName(Recipe recipe) {
        return recipe.getCookbookName();
    }

    public static DeploymentObject getNodeConfig(VMDeployment deployment) {
        return deployment.createIfMissingAndGetConfig(CHEF_NODE_CONFIG_NAME);
    }

    @Override
    public void specifyConstraints(VMDeployment vmDeployment) {
        ChefEnvironment env = ChefEnvironmentDeployer.createChefEnvironmentIfMissing(vmDeployment);
        DeploymentObject environmentConfig = ChefEnvironmentDeployer.getEnvironmentAttributesConfig(env);
        DeploymentObject versionsConfig = ChefEnvironmentDeployer.getVersionsConfig(env);
        DeploymentObject nodeConfig = getNodeConfig(vmDeployment);

        List<Recipe> runlist = vmDeployment.getRunlist();

        runlist.forEach(r -> r.getAttributes().stream().filter(ChefAttribute::isNodeAttribute)
                .forEach(a -> a.getConstraintNames()
                    .forEach(n -> {
                        if (a.isEnvironmentAttribute()) {
                            nodeConfig.addOptionalField(n, constraintDescription(r, a));
                        } else {
                            nodeConfig.addField(n, constraintDescription(r, a));
                        }
                    })));

        runlist.forEach(r -> {
                versionsConfig.addField(ConstraintField.builder().name(getVersionConstraintFieldName(r)).description
                    ("Cookbook version").defaultValue(r.getVersion()).build());
                r.getAttributes().stream()
                    .filter(ChefAttribute::isEnvironmentAttribute)
                    .forEach(a -> a.getConstraintNames().forEach(
                        n -> environmentConfig.addField(n, constraintDescription(r, a))
                    ));

            });


    }

    @Override
    public Action getCreateAction(VMDeployment vmDeployment) {
        return provisioningActions.getProvisionVmAction(vmDeployment, nodeAPI, credentials);
    }

    @Override
    public Action getDeleteAction(VMDeployment vmDeployment) {
        return chefActions.getDeleteChefNodeAndClientAction(vmDeployment);
    }

    @Override
    public Action getUpdateAction(@NotNull DeploymentAttempt lastAttempt, VMDeployment before, VMDeployment after) {
        if (
            chefActions.needUpdateChefEnvironment(after.getChefEnvironment()) ||
                provisioningActions.needUpdateProvisioning(before, after)
            ) {
            return provisioningActions.getProvisionVmUpdateAction(after, nodeAPI, credentials);
        } else {
            return null;
        }
    }
}

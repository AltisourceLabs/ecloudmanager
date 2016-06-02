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

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.codes.ValueCode;
import org.ecloudmanager.actions.ChefActions;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.domain.chef.ChefConfiguration;
import org.ecloudmanager.repository.ChefConfigurationRepository;
import org.ecloudmanager.service.execution.Action;

import javax.enterprise.inject.spi.CDI;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChefEnvironmentDeployer implements Deployer<ChefEnvironment> {
    public static final String CHEF_CONFIG_NAME = "chef";
    public static final String CHEF_CONFIG_SERVER = "server";
    public static final String CHEF_CONFIG_VERSIONS = "cookbookVersions";
    public static final String CHEF_CONFIG_ATTRIBUTES = "environmentAttributes";

    public static final String CHEF_CONFIGURATION = "chefConfiguration";

    private ChefActions chefActions = CDI.current().select(ChefActions.class).get();

    public static ChefConfiguration getChefConfiguration(ChefEnvironment chefEnvironment) {
        DeploymentObject serverConfig = getServerConfig(chefEnvironment);
        String configurationName = serverConfig.getConfigValue(CHEF_CONFIGURATION);
        ChefConfigurationRepository chefConfigurationRepository = CDI.current().select(ChefConfigurationRepository.class).get();
        return chefConfigurationRepository.find(configurationName);
    }

    @Override
    public void specifyConstraints(ChefEnvironment deployment) {
        DeploymentConstraint config = getServerConfig(deployment);
        config.addField(ConstraintField.builder()
                .name(CHEF_CONFIGURATION)
                .description("Chef server configuration")
                .suggestionsProvider(new ChefConfigurationSuggestionsProvider())
                .build()
        );
    }

    @Override
    public Action getCreateAction(ChefEnvironment deployable) {
        return chefActions.getCreateChefEnvironmentAction(deployable);
    }

    @Override
    public Action getDeleteAction(ChefEnvironment deployable) {
        return chefActions.getDeleteChefEnvironmentAction(deployable);
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, ChefEnvironment before, ChefEnvironment after) {
        if (chefActions.needUpdateChefEnvironment(after)) {
            return Action.actionSequence(
                after.getName() + ": update chef environment",
                chefActions.getDeleteChefEnvironmentAction(before),
                chefActions.getCreateChefEnvironmentAction(after)
            );
        } else {
            return null;
        }
    }


    public static List<String> getConstraintNames(String text) {
        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(text), "tmp");
        return Arrays.stream(mustache.getCodes()).filter(c -> c instanceof ValueCode)
            .map(Code::getName)
            .collect(Collectors.toList());
    }

    public static DeploymentObject getServerConfig(ChefEnvironment deployment) {
        return deployment.createIfMissingAndGetConfig(CHEF_CONFIG_SERVER);
    }

    public static DeploymentObject getVersionsConfig(ChefEnvironment deployment) {
        return deployment.createIfMissingAndGetConfig(CHEF_CONFIG_VERSIONS);
    }

    public static DeploymentObject getEnvironmentAttributesConfig(ChefEnvironment deployment) {
        return deployment.createIfMissingAndGetConfig(CHEF_CONFIG_ATTRIBUTES);
    }

    @Override
    public boolean isRecreateActionRequired(ChefEnvironment before, ChefEnvironment after) {
        Map<String, String> beforeServerConfigValues = getServerConfig(before).getConfigValues();
        Map<String, String> afterServerConfigValues = getServerConfig(after).getConfigValues();
        return !beforeServerConfigValues.equals(afterServerConfigValues);
    }

    public static ChefEnvironment getChefEnvironment(DeploymentObject d) {
        return (ChefEnvironment) d.getTop().getChildByName(CHEF_CONFIG_NAME);
    }

    public static ChefEnvironment createChefEnvironmentIfMissing(VMDeployment vmDeployment) {
        ChefEnvironment env = getChefEnvironment(vmDeployment);
        if (env != null) {
            return env;
        }
        env = new ChefEnvironment();
        env.setName(CHEF_CONFIG_NAME);
        vmDeployment.getTop().addChild(env);
        env.specifyConstraints();
        return env;
    }
}

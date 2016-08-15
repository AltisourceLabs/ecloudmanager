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

import com.github.mustachejava.Mustache;
import com.github.mustachejava.util.DecoratedCollection;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.*;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.service.provisioning.UnescapedMustacheFactory;

import javax.inject.Inject;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChefGenerationService {
    private static final String TEMPLATE_FILE = "chef-env-template.json.mustache";
    private static final String NODE_TEMPLATE_FILE = "chef-node-template.json.mustache";

    @Inject
    private UnescapedMustacheFactory mustacheFactory;

    public Writer generateChefEnv(Writer writer, ChefEnvironment chefEnvironment) {
        DeploymentObject top = chefEnvironment.getTop();
        Mustache step1 = mustacheFactory.compile(TEMPLATE_FILE);

        Map<String, String> defaultAttributesMap = new HashMap<>();
        Map<String, String> overrideAttributesMap = new HashMap<>();
        top.stream(VMDeployment.class)
            .flatMap(d -> d.getRunlist().stream())
            .flatMap(recipe -> recipe.getAttributes().stream())
            .filter(ChefAttribute::isEnvironmentAttribute)
            .forEach(a -> {
                if (a.isEnvironmentDefaultAttribute()) {
                    defaultAttributesMap.put(a.getName(), a.getValue());
                } else {
                    overrideAttributesMap.put(a.getName(), a.getValue());
                }
            });

        Map<String, String> recipes = top.stream(VMDeployment.class)
            .flatMap(d -> d.getRunlist().stream())
            .collect(Collectors.toMap(Recipe::getCookbookName,
                ChefProvisioningDeployer::getVersionConstraintFieldName, (a, b) -> a));
        //noinspection unused
        Object scope = new Object() {
            String name = top.getName();
            String description = top.getDescription();
            DecoratedCollection<Map.Entry<String, String>> cookbook_versions = new DecoratedCollection<>(recipes
                .entrySet());
            DecoratedCollection<Map.Entry<String, String>> override_attributes = new DecoratedCollection<>
                (overrideAttributesMap.entrySet());
            DecoratedCollection<Map.Entry<String, String>> default_attributes = new DecoratedCollection<>
                (defaultAttributesMap.entrySet());

        };

        Reader r = new StringReader(step1.execute(new StringWriter(), scope).toString());

        DeploymentObject environmentConfig = ChefEnvironmentDeployer.getEnvironmentAttributesConfig
            (chefEnvironment);
        DeploymentObject versionsConfig = ChefEnvironmentDeployer.getVersionsConfig(chefEnvironment);

        Mustache step2 = mustacheFactory.compile(r, "template");
        Map<String, String> scopeMap = new HashMap<>();

        scopeMap.putAll(environmentConfig.getConfigValues());
        scopeMap.putAll(versionsConfig.getConfigValues());

        return step2.execute(writer, scopeMap);
    }

    public Writer generateChefNodeEnv(Writer writer, VMDeployment vmDeployment) {
        String runlist = vmDeployment.getRunlist().stream().
            map(s -> "\"recipe[" + s.getRunlistItem() + "]\"").collect(Collectors.joining(","));
        return generateChefNodeEnv(writer, vmDeployment, runlist);
    }

    public Writer generateChefNodeEnv(Writer writer, VMDeployment vmDeployment, String runlist) {
        Mustache step1 = mustacheFactory.compile(NODE_TEMPLATE_FILE);

        DeploymentObject nodeConfig = ChefProvisioningDeployer.getNodeConfig(vmDeployment);

        Map<String, String> nodeAttributesMap = vmDeployment.getRunlist()
            .stream()
            .flatMap(r -> r.getAttributes().stream())
            .filter(a -> a.isNodeAttribute() && (!a.isEnvironmentAttribute()
                || a.getConstraintNames().stream().anyMatch(n -> nodeConfig.getConfigValue(n) != null)))
            .collect(Collectors.toMap(ChefAttribute::getName, ChefAttribute::getValue, (a, b) -> b));

        nodeAttributesMap.put("run_list", "[" + runlist + "]");

        //noinspection unused
        Object scope = new Object() {
            DecoratedCollection<Map.Entry<String, String>> node_attributes = new DecoratedCollection<>
                (nodeAttributesMap.entrySet());
        };

        Reader r = new StringReader(step1.execute(new StringWriter(), scope).toString());

        Mustache step2 = mustacheFactory.compile(r, "template");
        Map<String, String> scopeMap = new HashMap<>();
        DeploymentObject config = ChefEnvironmentDeployer.getEnvironmentAttributesConfig(
            ChefEnvironmentDeployer.getChefEnvironment(vmDeployment));

        scopeMap.putAll(config.getConfigValues());
        scopeMap.putAll(nodeConfig.getConfigValues());

        return step2.execute(writer, scopeMap);
    }

}

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

package org.ecloudmanager.service.template;

import com.google.common.collect.ImmutableList;
import com.rits.cloning.Cloner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class VirtualMachineTemplateService extends ServiceSupport {
    @Inject
    private Logger log;
    @Inject
    Cloner cloner;
    @Inject
    private RecipeService recipeService;
    @Inject
    private RecipeRepository recipeRepository;
    @Inject
    private VirtualMachineTemplateRepository virtualMachineTemplateRepository;

    public void saveVm(VirtualMachineTemplate vm) {
        log.info("Saving " + vm.getName());
        save(vm);
    }

    public void updateVm(VirtualMachineTemplate vm) {
        log.info("Updating " + vm.getName());
        update(vm);
    }

    public void removeVm(VirtualMachineTemplate vm) {
        log.info("Deleting " + vm.getName());
        delete(vm);
    }

    public String importVm(VirtualMachineTemplate src, VirtualMachineTemplate dst, ApplicationDeployment owner) {
        ObjectId id = dst.getId();
        String name = dst.getName();
        DeploymentObject parent = dst.getParent();
        cloner.copyPropertiesOfInheritedClass(src, dst);
        dst.setId(id);

        if (!StringUtils.isEmpty(name)) {
            dst.setName(name);
        }

        List<String> addedRecipes = new ArrayList<>();
        List<String> addedRenamedRecipes = new ArrayList<>();

        List<Recipe> newRunlist = new ArrayList<>();
        dst.getRunlist().stream().forEachOrdered(recipe -> {
            Recipe newRecipe = null;
            Recipe existingRecipe = recipeRepository.findByName(recipe.getName(), owner);
            if (existingRecipe != null) {
                if (EqualsBuilder.reflectionEquals(recipe, existingRecipe, ImmutableList.of("id", "owner"))) {
                    newRecipe = existingRecipe;
                }
            }

            if (newRecipe == null) {
                newRecipe = cloner.deepClone(recipe);
                newRecipe.setId(new ObjectId());
                newRecipe.setOwner(owner);
                recipeService.saveWithUniqueName(newRecipe);
                if (newRecipe.getName().equals(recipe.getName())) {
                    addedRecipes.add(newRecipe.getName());
                } else {
                    addedRenamedRecipes.add(newRecipe.getName());
                }
            }

            newRunlist.add(newRecipe);
        });

        dst.setRunlist(newRunlist);
        dst.setParent(parent);

        StringBuilder builder = new StringBuilder();
        if (addedRecipes.size() > 0) {
            builder.append("The following recipes were imported: ").append(StringUtils.join(addedRecipes, ","));
        }
        if (addedRenamedRecipes.size() > 0) {
            builder.append("The following recipes were imported with modified names: ").append(StringUtils.join(addedRenamedRecipes, ","));
        }

        return builder.toString();
    }

    public List<String> findRecipeUsages(Recipe recipe) {
        Deployable owner = recipe.getOwner();
        if (owner == null) {
            return virtualMachineTemplateRepository.getAll().stream()
                    .filter(vmt -> vmt.getRunlist().contains(recipe))
                    .map(vmt -> "VM Template: " + vmt)
                    .collect(Collectors.toList());
        } else {
            List<String> result = new ArrayList<>();
            result.addAll(owner.stream(VirtualMachineTemplate.class)
                    .filter(vmt -> vmt.getRunlist().contains(recipe))
                    .map(vmt -> owner.getName() + ":" + vmt.getParent().getPath(":") + ":" + vmt.getName())
                    .collect(Collectors.toList()));
            return result;
        }
    }
}
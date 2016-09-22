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

import com.rits.cloning.Cloner;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentConstraint;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;
import org.ecloudmanager.util.NameUtil;
import org.jetbrains.annotations.NotNull;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class ImportDeployableService {
    @Inject
    private VirtualMachineTemplateService virtualMachineTemplateService;
    @Inject
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private RecipeRepository recipeRepository;
    @Inject
    private Logger log;

    public void copyDeployment(ApplicationDeployment source, String name, boolean keepConstraints) {
        ApplicationDeployment newDeployment = cloneDeployable(source, null, name, keepConstraints);
        if (!keepConstraints) {
            newDeployment.specifyConstraints();
        }

        // Save the deployment before importing recipes to be able to create recipe owner references
        applicationDeploymentRepository.save(newDeployment);

        // Copy all the recipes from src to dst deployment. Create a dummy VM template with all the recipes and "import" it
        List<Recipe> srcDeploymentRecipes = recipeRepository.getAll(source);
        VirtualMachineTemplate dummySrc = new VirtualMachineTemplate();
        dummySrc.getRunlist().addAll(srcDeploymentRecipes);
        VirtualMachineTemplate dummyDst = new VirtualMachineTemplate();
        virtualMachineTemplateService.importVm(dummySrc, dummyDst, newDeployment);

        // Fix runlists in VM templates
        Map<String, Recipe> recipeMap = dummyDst.getRunlist().stream().collect(Collectors.toMap(Recipe::getName, r -> r));
        newDeployment.stream(VirtualMachineTemplate.class).forEach(vmt -> vmt.getRunlist().replaceAll(r -> recipeMap.get(r.getName())));

        applicationDeploymentRepository.save(newDeployment);
    }

    public Deployable copyDeploymentObject(Deployable source, DeploymentObject dstParentObject, String name, boolean keepConstraints) {
        Deployable newDeploymentObj = cloneDeployable(source, dstParentObject, name, keepConstraints);

        ApplicationDeployment dstDeployment =
                dstParentObject != null && dstParentObject.getTop() instanceof ApplicationDeployment ?
                        (ApplicationDeployment) dstParentObject.getTop() : null;

        source.stream(VirtualMachineTemplate.class).forEach(vmt -> {
            String path = source.relativePathTo(vmt);
            if (!StringUtils.isEmpty(vmt.getName())) {
                VirtualMachineTemplate dstVmt = (VirtualMachineTemplate) newDeploymentObj.getByPath(path);
                virtualMachineTemplateService.importVm(vmt, dstVmt, dstDeployment);
            } else {
                log.warn("Cannot import vm template with empty name. Skipping. Path: " + source.getPath("/") + path);
            }
        });

        if (dstParentObject != null) {
            dstParentObject.addChild(newDeploymentObj);
        }

        if (dstDeployment != null) {
            applicationDeploymentRepository.save(dstDeployment);
        }

        return newDeploymentObj;
    }

    @NotNull
    private <T extends Deployable> T cloneDeployable(T source, DeploymentObject dstParentObject, String name, boolean keepConstraints) {
        Cloner cloner = new Cloner();
        T newDeploymentObj = cloner.deepClone(source);
        newDeploymentObj.setId(dstParentObject == null ? new ObjectId() : null);
        newDeploymentObj.stream().filter(obj -> obj != newDeploymentObj).forEach(dObj -> dObj.setId(new ObjectId()));

        String uniqueName = generateUniqueName(name, newDeploymentObj, dstParentObject);
        newDeploymentObj.setName(uniqueName);

        newDeploymentObj.stream().forEach(DeploymentObject::fixChildren);
        if (!keepConstraints) {
            newDeploymentObj.stream().forEach(DeploymentConstraint::clear);
            newDeploymentObj.stream(ComponentGroupDeployment.class).forEach(componentGroupDeployment -> {
                DeploymentObject cfg = componentGroupDeployment.getChildByName(ComponentGroupDeployment.VM_CONFIG);
                if (cfg != null) {
                    cfg.children().clear(); // Children will be recreated with specifyConstraints
                }
            });
            //newDeploymentObj.specifyConstraints();
        }
        return newDeploymentObj;
    }

    private String generateUniqueName(String hint, DeploymentObject object, DeploymentObject parent) {
        if (parent == null) {
            return hint;
        }

        Set<String> usedNames = parent.children().stream().map(DeploymentObject::getName).collect(Collectors.toSet());
        if (StringUtils.isEmpty(hint)) {
            hint = object.getClass().getSimpleName();
        }

        return NameUtil.getUniqueName(hint, usedNames);
    }
}

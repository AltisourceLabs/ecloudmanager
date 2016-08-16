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

package org.ecloudmanager.web.faces;

import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Controller
public class VmTemplateController extends FacesSupport implements Serializable {

    private VirtualMachineTemplate value;
    private static final long serialVersionUID = -3780788515526080540L;

    @Inject
    private transient VirtualMachineTemplateRepository vmtemplateRepo;
    @Inject
    private transient VirtualMachineTemplateService vmtemplateService;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient ApplicationDeploymentEditorController applicationDeploymentEditorController;

    private Recipe recipeToAdd = null;

    public VirtualMachineTemplate getValue() {
        return value;
    }

    public void setValue(VirtualMachineTemplate value) {
        this.value = value;
    }


    @PostConstruct
    public void init() {
        this.value = new VirtualMachineTemplate();
    }

    public List<VirtualMachineTemplate> getTemplates() {
        return vmtemplateRepo.getAll();
    }

    public void deleteRecipe(Recipe recipe) {
        value.removeRecipe(recipe);
    }

    public List<Recipe> getRecipes(ApplicationDeployment owner) {
        return recipeRepository.getAll(owner);
    }

    public void setRecipeToAdd(Recipe recipeToAdd) {
        this.recipeToAdd = recipeToAdd;
    }

    public Object getRecipeToAdd() {
        return recipeToAdd;
    }

    public void addRecipe() {
        if (recipeToAdd != null) {
            value.addRecipe(recipeToAdd);
            recipeToAdd = null;
        }
    }

    public void startImportVmTemplate() {
        HashMap<String, Object> options = new HashMap<>();
        options.put("width", 640);
        options.put("modal", true);
        RequestContext.getCurrentInstance().openDialog("importVmTemplate", options, null);
    }

    public void onImportVmTemplateReturn(SelectEvent event) {
        VirtualMachineTemplate virtualMachineTemplate = (VirtualMachineTemplate) event.getObject();
        if (virtualMachineTemplate != null) {
            String message = vmtemplateService.importVm(virtualMachineTemplate, value, applicationDeploymentEditorController.getDeployment());
            if (!message.isEmpty()) {
                addMessage(new FacesMessage(message));
            }
        }
    }

    public void onRowReorder(ReorderEvent event) {
        List<Recipe> runlist = value.getRunlist();
        Recipe moving = runlist.remove(event.getFromIndex());
        runlist.add(event.getToIndex(), moving);
        value.setRunlist(runlist);
    }
}

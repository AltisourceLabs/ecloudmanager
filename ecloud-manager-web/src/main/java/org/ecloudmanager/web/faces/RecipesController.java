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

import com.rits.cloning.Cloner;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class RecipesController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -7592006943742318320L;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient VirtualMachineTemplateService virtualMachineTemplateService;
    @Inject
    private transient Cloner cloner;
    @Inject
    private transient RecipeController recipeController;
    @Inject
    private transient ApplicationDeploymentEditorController applicationDeploymentEditorController;

    private List<Recipe> recipes;

    @PostConstruct
    private void init() {
        refresh();
    }

    public void refresh() {
        ApplicationDeployment owner = applicationDeploymentEditorController == null || applicationDeploymentEditorController.getDeployment() == null ?
                null : applicationDeploymentEditorController.getDeployment();
        recipes = recipeRepository.getAll(owner);
    }


    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void save() {
        Recipe value = recipeController.getValue();
        if (value == null) {
            return;
        }
        if (value.isNew()) {
            recipeService.save(value);
        } else {
            recipeService.update(value);
        }

        refresh();
    }

    public void cancel() {
        Recipe value = recipeController.getValue();
        if (value.isNew()) {
            recipes.remove(value);
        } else {
            Recipe reloaded = recipeRepository.reload(value);
            int idx = recipes.indexOf(value);
            recipes.set(idx, reloaded);
        }
        recipeController.setValue(null);
    }

    public void onRowSelect(SelectEvent event) {
//        String id =  ((Recipe) event.getObject()).getId();
//        ConfigurableNavigationHandler configurableNavigationHandler =
//                (ConfigurableNavigationHandler)FacesContext.
//                        getCurrentInstance().getApplication().getNavigationHandler();
//
//        configurableNavigationHandler.performNavigation("recipes.jsf?selected=" + id + "&faces-redirect=true");

    }

    public void onRowUnselect(UnselectEvent event) {
//        FacesMessage msg = new FacesMessage("Car Unselected", ((Recipe) event.getObject()).getId());
//        FacesContext.getCurrentInstance().addMessage(null, msg);

    }


    public Recipe getNewRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(new ObjectId());
        updateOwner(recipe);
        recipeController.setValue(recipe);
        return recipeController.getValue();
    }

    private void updateOwner(Recipe recipe) {
        if (
                applicationDeploymentEditorController != null &&
                applicationDeploymentEditorController.getDeployment() != null
        ) {
            recipe.setOwner(applicationDeploymentEditorController.getDeployment());
        } else {
            recipe.setOwner(null);
        }
    }

    public void deleteSelected() {
        if (recipeController.getValue() != null) {
            List<String> usages = virtualMachineTemplateService.findRecipeUsages(recipeController.getValue());
            if (usages.isEmpty()) {
                recipeService.remove(recipeController.getValue());
                recipeController.setValue(null);
            } else {
                addMessage(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Cannot delete the selected recipe",
                        "Cannot delete the selected recipe. It is used by the following objects: <br>" + StringUtils.join(usages, "<br>"))
                );
            }
        }
        refresh();
    }


    public void handleClose(CloseEvent event) {
        event.getSource();
    }

    public void onImportRecipeReturn(SelectEvent event) {
        List<Recipe> newRecipes = (List<Recipe>) event.getObject();
        if (newRecipes != null) {
            newRecipes.stream().forEach(recipe -> {
                Recipe newRecipe = cloner.deepClone(recipe);
                newRecipe.setId(new ObjectId());
                updateOwner(newRecipe);
                recipeService.saveWithUniqueName(newRecipe);
                refresh();
            });
        }
    }
}

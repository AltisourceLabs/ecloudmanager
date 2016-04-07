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
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@SessionScoped
@ManagedBean
public class RecipesController extends FacesSupport implements Serializable {
    public final static String NEW_RECIPE_ID = "(new)";
    private static final long serialVersionUID = -7592006943742318320L;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient Cloner cloner;
    @ManagedProperty(value = "#{recipeController}")
    private transient RecipeController recipeController;
    private List<Recipe> recipes;

    public void setRecipeController(RecipeController recipeController) {
        this.recipeController = recipeController;
    }

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        recipes = recipeRepository.getAll();
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
    }

    public void cancel() {
        Recipe value = recipeController.getValue();
        if (value.isNew()) {
            recipes.remove(value);
        } else {
            Recipe reloaded = recipeRepository.get(value.getOldId());
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
        recipeController.setValue(new Recipe(NEW_RECIPE_ID));
        return recipeController.getValue();
    }

    public void deleteSelected() {
        if (recipeController.getValue() != null) {
            recipeService.remove(recipeController.getValue());
        }
        recipeController.setValue(null);
    }


    public void handleClose(CloseEvent event) {
        event.getSource();
    }
}

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
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class RecipesController extends FacesSupport implements Serializable {
    public final static String NEW_RECIPE_ID = "(new)";
    private static final String ENV_DEFAULT = "Default";
    private static final String ENV_OVERRIDE = "Override";
    private static final String ENV_NO = "No";
    private static final long serialVersionUID = -7592006943742318320L;
    private Recipe selected;
    private String editAttributeEnv;
    private ChefAttribute selectedAttribute;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient Cloner cloner;


    private List<Recipe> recipes;
    private boolean changed = false;

    public ChefAttribute getSelectedAttribute() {
        return selectedAttribute;
    }

    public void setSelectedAttribute(ChefAttribute selectedAttribute) {
        this.selectedAttribute = selectedAttribute;
    }

    public ChefAttribute getEditAttribute() {
        if (selectedAttribute != null) {
            return selectedAttribute;
        }
        return new ChefAttribute();
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

    public Recipe getSelected() {
        return selected;
    }

    public void setSelected(Recipe selected) {
        if (this.selected != selected) {
            this.selected = selected;
            changed = false;
            this.selectedAttribute = null;
        }
    }

    public void save() {
        changed = false;
        if (selected == null) {
            return;
        }
        if (selected.isNew()) {
            recipeService.save(selected);
        } else {
            recipeService.update(selected);
        }
    }

    public void cancel() {
        if (selected.isNew()) {
            recipes.remove(selected);
            selected = null;
        } else {
            Recipe reloaded = recipeRepository.get(selected.getOldId());
            int idx = recipes.indexOf(selected);
            recipes.set(idx, reloaded);
            selected = null;
        }
        changed = false;
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

    public boolean isChanged() {
        return changed;
    }

    public void changedEvent() {
        this.changed = true;
    }

    public Recipe getNewRecipe() {
        selected = new Recipe(NEW_RECIPE_ID);
        changed = true;
        return selected;
    }

    public ChefAttribute getNewChefAttribute() {
        selectedAttribute = new ChefAttribute();
        selectedAttribute.setName("(new)");
        changed = true;
        return selectedAttribute;
    }

    public void deleteSelected() {
        if (selected != null) {
            recipeService.remove(selected);
        }
        selected = null;
    }

    public void deleteSelectedAttribute() {
        if (selectedAttribute != null && selected != null) {
            selected.getAttributes().remove(selectedAttribute);
        }
        selectedAttribute = null;
    }

    public Recipe createCopy() {
        if (selected == null) {
            return null;
        }
        Recipe r = cloner.deepClone(selected);
        r.setId(r.getId() + "[1]"); //FIXME
        selected = r;
        changed = true;
        return r;
    }

    public String getEditAttributeEnv() {
        if (getEditAttribute().isEnvironmentAttribute()) {
            if (getEditAttribute().isEnvironmentDefaultAttribute()) {
                return ENV_DEFAULT;
            } else {
                return ENV_OVERRIDE;
            }
        } else {
            return ENV_NO;
        }
    }

    public void setEditAttributeEnv(String editAttributeEnv) {
        if (ENV_NO.equals(editAttributeEnv)) {
            selectedAttribute.setEnvironmentAttribute(false);
        } else if (ENV_DEFAULT.equals(editAttributeEnv)) {
            selectedAttribute.setEnvironmentAttribute(true);
            selectedAttribute.setEnvironmentDefaultAttribute(true);
        } else {
            selectedAttribute.setEnvironmentAttribute(true);
            selectedAttribute.setEnvironmentDefaultAttribute(false);

        }
    }

    public void handleClose(CloseEvent event) {
        event.getSource();
    }
}

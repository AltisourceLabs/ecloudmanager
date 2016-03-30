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
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class RecipeController extends FacesSupport implements Serializable {
    public final static String NEW_RECIPE_ID = "(new)";
    private static final String ENV_DEFAULT = "Default";
    private static final String ENV_OVERRIDE = "Override";
    private static final String ENV_NO = "No";
    private static final long serialVersionUID = -7592006943742318320L;
    UIComponent dialogEditChefAttribute;
    private Recipe value;
    private ChefAttribute selectedAttribute;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient Cloner cloner;
    @Inject
    private transient EndpointController endpointController;
    private EndpointTemplate selectedEndpoint;
    private List<Recipe> recipes;
    private boolean changed = false;

    public EndpointTemplate getSelectedEndpoint() {
        return selectedEndpoint;
    }

    public UIComponent getDialogEditChefAttribute() {
        return dialogEditChefAttribute;
    }

    public void setDialogEditChefAttribute(UIComponent dialogEditChefAttribute) {
        this.dialogEditChefAttribute = dialogEditChefAttribute;
    }

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

    public Recipe getValue() {
        return value;
    }

    public void setValue(Recipe value) {
        if (this.value != value) {
            this.value = value;
            changed = false;
            this.selectedAttribute = null;
            this.selectedEndpoint = null;
        }
    }


    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void changedEvent() {
        this.changed = true;
    }

    public void addNewChefAttribute(){
        ChefAttribute attribute = new ChefAttribute();
        attribute.setName("(new)");
        value.getAttributes().add(attribute);
        changed = true;
        startEdit(attribute);
    }

    public void deleteSelectedAttribute() {
        if (selectedAttribute != null && value != null) {
            value.getAttributes().remove(selectedAttribute);
        }
        selectedAttribute = null;
    }

    public Recipe createCopy() {
        if (value == null) {
            return null;
        }
        Recipe r = cloner.deepClone(value);
        r.setId(r.getId() + "[1]"); //FIXME
        value = r;
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

    public void startEdit(ChefAttribute entity) {
        selectedAttribute = entity;

//        showDialog(dialogEditChefAttribute.getClientId());
        showDialog("dlg_edit");
    }

    private void showDialog(String dialog) {
        Ajax.update("recipes:recipe:recipe:" + dialog);
        RequestContext ctx = RequestContext.getCurrentInstance();

        //ctx.update(dialog);
        ctx.execute("PF('" + dialog + "').show()");

    }

    public void hideDialog(String dialog) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + dialog + "').hide()");
    }

    public void handleClose(CloseEvent event) {
        hideDialog("dlg_edit");
    }

    public void addNewEndpoint() {
        EndpointTemplate endpoint = new EndpointTemplate("");
        value.addEndpoint(endpoint);
        changed = true;
        startEditEndpoint(endpoint);
    }

    public void startEditEndpoint(EndpointTemplate entity) {
        endpointController.setValue(entity);

//        showDialog(dialogEditChefAttribute.getClientId());
        showDialog("dlg_edit_endpoint");
    }

    public void deleteSelectedEndpoint() {
        if (selectedEndpoint != null && value != null) {
            value.getEndpoints().remove(selectedEndpoint);
        }
        selectedEndpoint = null;
    }
}

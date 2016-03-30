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
import org.ecloudmanager.deployment.core.EndpointAttributeTemplate;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class EndpointController extends FacesSupport implements Serializable {
    private EndpointTemplate value;

    private EndpointAttributeTemplate selectedAttribute;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient Cloner cloner;

    public EndpointTemplate getSelectedEndpoint() {
        return selectedEndpoint;
    }

    private EndpointTemplate selectedEndpoint;

    private List<Recipe> recipes;
    private boolean changed = false;

    public EndpointAttributeTemplate getSelectedAttribute() {
        return selectedAttribute;
    }

    public void setSelectedAttribute(EndpointAttributeTemplate selectedAttribute) {
        this.selectedAttribute = selectedAttribute;
    }

    public EndpointAttributeTemplate getEditAttribute() {
        if (selectedAttribute != null) {
            return selectedAttribute;
        }
        return new EndpointAttributeTemplate();
    }


    public EndpointTemplate getValue() {
        return value;
    }

    public void setValue(EndpointTemplate value) {
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

    public void changedEvent() {
        this.changed = true;
    }

    public void addNewEndpointAttribute(){
        EndpointAttributeTemplate attribute = new EndpointAttributeTemplate();
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



    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void startEdit(EndpointAttributeTemplate entity) {
        selectedAttribute = entity;

//        showDialog(dialogEditChefAttribute.getClientId());
        showDialog("dlg_edit");
    }

    private void showDialog(String dialog) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(dialog);
        ctx.execute("PF('" + dialog + "').show()");

    }

    public void hideDialog(String dialog) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + dialog + "').hide()");
    }

    public void handleClose(CloseEvent event) {
        hideDialog("dlg_edit");
    }

}

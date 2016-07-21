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
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SessionScoped
@ManagedBean
public class RecipeController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -7592006943742318320L;
    private static Map<String, Object> options = new HashMap<>();

    static {
        // options.put("resizable", false);
        // options.put("draggable", false);
        //options.put("contentWidth", "auto");
        options.put("width", 640);
        options.put("height", 340);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("modal", true);
    }

    private Recipe value;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient Cloner cloner;
    @ManagedProperty(value = "#{endpointController}")
    private EndpointController endpointController;
    @ManagedProperty(value = "#{chefAttributeController}")
    private ChefAttributeController chefAttributeController;
    private boolean changed = false;

    @SuppressWarnings("unused") // required for JSF DI
    public void setEndpointController(EndpointController endpointController) {
        this.endpointController = endpointController;
    }

    @SuppressWarnings("unused") // required for JSF DI
    public void setChefAttributeController(ChefAttributeController chefAttributeController) {
        this.chefAttributeController = chefAttributeController;
    }

    public Recipe getValue() {
        return value;
    }

    public void setValue(Recipe value) {
        if (this.value != value) {
            this.value = value;
        }
    }

    public void addNewChefAttribute(){
        startEditChefAttribute(new ChefAttribute());
    }

    public void deleteChefAttribute(ChefAttribute attribute) {
        value.getAttributes().remove(attribute);
    }

    public Recipe createCopy() {
        if (value == null) {
            return null;
        }
        Recipe r = cloner.deepClone(value);
        r.setId(r.getId() + "[1]"); //FIXME
        value = r;
        return r;
    }

    public void startEditChefAttribute(ChefAttribute entity) {
        chefAttributeController.setValue(entity);
        RequestContext.getCurrentInstance().openDialog("editChefAttribute", options, null);
    }

    public void addNewEndpoint() {
        startEditEndpoint(new Endpoint());
    }

    public void startEditEndpoint(Endpoint entity) {
        endpointController.setValue(entity);
        RequestContext.getCurrentInstance().openDialog("editRecipeEndpoint", options, null);
    }

    public void deleteEndpoint(Endpoint endpoint) {
        if (endpoint != null && value != null) {
            value.getEndpoints().remove(endpoint);
        }
    }

    public void onEditEndpointReturn(SelectEvent event) {
        Endpoint template = (Endpoint) event.getObject();
    }

    public void onEditNewEndpointReturn(SelectEvent event) {
        Endpoint template = (Endpoint) event.getObject();
        if (template != null) {
            value.addEndpoint(template);
        }
    }

    public void onEditNewChefAttributeReturn(SelectEvent event) {
        ChefAttribute attribute = (ChefAttribute) event.getObject();
        if (attribute != null) {
            value.addChefAttribute(attribute);
        }
    }

    public void onEditChefAttributeReturn(SelectEvent event) {
        ChefAttribute attribute = (ChefAttribute) event.getObject();
    }


}

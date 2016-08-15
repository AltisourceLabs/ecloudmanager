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
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.provisioning.ChefAttribute;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;
import org.jetbrains.annotations.NotNull;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@Controller
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
    private boolean changed = false;

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
        r.setId(new ObjectId());
        value = r;
        return r;
    }

    public void startImportRecipe() {
        RequestContext.getCurrentInstance().openDialog("importRecipe", options, null);
    }

    @NotNull
    private Map<String, List<String>> storeToSessionMapAndGetParamsMapWithId(Object entity) {
        Map<String, List<String>> params = new HashMap<>();
        String valueParamId = UUID.randomUUID().toString();
        params.put("valueParamId", Collections.singletonList(valueParamId));
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(valueParamId, entity);
        return params;
    }

    public void startEditChefAttribute(ChefAttribute entity) {
        Map<String, List<String>> params = storeToSessionMapAndGetParamsMapWithId(entity);
        RequestContext.getCurrentInstance().openDialog("editChefAttribute", options, params);
    }


    public void addNewEndpoint() {
        startEditEndpoint(new Endpoint());
    }

    public void startEditEndpoint(Endpoint entity) {
        Map<String, List<String>> params = storeToSessionMapAndGetParamsMapWithId(entity);
        RequestContext.getCurrentInstance().openDialog("editRecipeEndpoint", options, params);
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

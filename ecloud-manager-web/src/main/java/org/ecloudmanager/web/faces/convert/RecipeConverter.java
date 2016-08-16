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

package org.ecloudmanager.web.faces.convert;

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.template.RecipeRepository;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "recipeConverter", forClass = Recipe.class)
public class RecipeConverter implements Converter {
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;

    @Override
    public Recipe getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Recipe result;

            if (value.contains(":")) {
                String[] split = value.split(":");
                ObjectId recipeId = new ObjectId(split[0]);
                String deploymentId = split[1];
                ApplicationDeployment deployment = applicationDeploymentRepository.get(deploymentId);
                result = recipeRepository.get(recipeId, deployment);
            } else {
                result = recipeRepository.get(new ObjectId(value));
            }

            if (result == null) {
                throw new ConverterException(new FacesMessage("Recipe with id " + value +
                                                              " not found"));
            }
            return result;
        } catch (IllegalArgumentException ex) {
            throw new ConverterException(new FacesMessage("Invalid id format :" + value, ex.getMessage()));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Recipe) {
            Recipe recipe = Recipe.class.cast(value);
            StringBuilder result = new StringBuilder(recipe.getId().toString());
            if (recipe.getOwner() != null) {
                result.append(":").append(recipe.getOwner().getId());
            }
            return result.toString();
        }
        return "";
    }
}

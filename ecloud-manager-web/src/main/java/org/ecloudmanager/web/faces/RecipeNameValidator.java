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

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.repository.template.RecipeRepository;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

@FacesValidator("recipeNameValidator")
public class RecipeNameValidator implements Validator {
    @Inject
    private transient RecipeRepository recipeRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        ApplicationDeployment deployment = (ApplicationDeployment) component.getAttributes().get("owner");
        ObjectId recipeId = (ObjectId) component.getAttributes().get("recipeId");

        String newName = (String) value;

        if (StringUtils.isEmpty(newName)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name should not be empty.", "Please enter a valid name"));
        }

        Recipe recipe = recipeRepository.findByName(newName, deployment);
        boolean duplicateName = recipe != null && !recipe.getId().equals(recipeId);
        if (duplicateName) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Recipe " + newName + " already exist", "Please enter unique name"));
        }
    }
}

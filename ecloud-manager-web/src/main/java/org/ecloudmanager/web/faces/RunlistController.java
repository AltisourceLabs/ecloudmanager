/*
 *  MIT License
 *
 *  Copyright (c) 2016  Altisource
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package org.ecloudmanager.web.faces;

import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.domain.RunlistHolder;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class RunlistController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -4787577960524429839L;

    @Inject
    private transient RecipeRepository recipeRepository;

    private transient List<Recipe> recipes;
    private transient Recipe recipeToAdd = null;

    @PostConstruct
    public void init() {
        recipes = recipeRepository.getAll();
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void deleteRecipe(Recipe recipe, RunlistHolder runlistHolder) {
        runlistHolder.getRunlist().remove(recipe);
    }

    public void setRecipeToAdd(Recipe recipeToAdd) {
        this.recipeToAdd = recipeToAdd;
    }

    public Object getRecipeToAdd() {
        return recipeToAdd;
    }

    public void addRecipe(RunlistHolder runlistHolder) {
        if (recipeToAdd != null) {
            runlistHolder.addRecipe(recipeToAdd);
            recipeToAdd = null;
        }
    }

}

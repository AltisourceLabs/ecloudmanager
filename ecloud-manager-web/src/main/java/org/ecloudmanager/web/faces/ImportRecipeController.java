/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.template.RecipeService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class ImportRecipeController implements Serializable {
    @Inject
    private transient RecipeService recipeService;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient RecipesController recipesController;
    @Inject
    private transient Cloner cloner;

    private List<Recipe> selectedRecipes;

    public List<Recipe> getSelectedRecipes() {
        return selectedRecipes;
    }

    public void setSelectedRecipes(List<Recipe> selectedRecipes) {
        this.selectedRecipes = selectedRecipes;
    }

    public List<Recipe> getRecipes() {
        return recipeRepository.getAll();
    }

    public void save() {
        if (selectedRecipes != null) {
            selectedRecipes.stream().forEach(recipe -> {
                Recipe newRecipe = cloner.deepClone(recipe);
                newRecipe.setId(new ObjectId());
                recipesController.updateOwner(newRecipe);
                recipeService.saveWithUniqueName(newRecipe);
                recipesController.refresh();
            });
        }
    }

    public void cancel() {
        reset();
    }

    public void reset() {
        if (selectedRecipes != null) {
            selectedRecipes.clear();
        }
    }
}

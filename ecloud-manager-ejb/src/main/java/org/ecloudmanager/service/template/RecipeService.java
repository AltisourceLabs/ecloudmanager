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

package org.ecloudmanager.service.template;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.template.RecipeRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Stateless
public class RecipeService extends ServiceSupport {

    @Inject
    private Logger log;
    @Inject
    RecipeRepository recipeRepository;

    public void save(Recipe recipe) {
        log.info("Saving " + recipe.getId());
        recipeRepository.save(recipe);
        fireEvent(recipe);
    }

    public void update(Recipe recipe) {
        log.info("Updating " + recipe.getId());
        recipeRepository.save(recipe);
        fireEvent(recipe);
    }

    public void remove(Recipe recipe) {
        log.info("Deleting " + recipe.getId());
        recipeRepository.delete(recipe);
        fireEvent(recipe);
    }

    public void saveWithUniqueName(Recipe recipe) {
        log.info("Saving with unique name " + recipe.getId());
        recipe.setName(getUniqueRecipeName(recipe.getName(), recipe.getOwner()));
        save(recipe);
        fireEvent(recipe);
    }

    private String getUniqueRecipeName(String hint, ApplicationDeployment owner) {
        Set<String> usedNames = recipeRepository.getAll(owner).stream().map(Recipe::getName).collect(Collectors.toSet());
        if (!usedNames.contains(hint)) {
            return hint;
        }

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String candidate = hint + i;
            if (!usedNames.contains(candidate)) {
                return candidate;
            }
        }

        return hint + UUID.randomUUID().toString();
    }
}
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

package org.ecloudmanager.repository.template;

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.repository.MongoDBRepositorySupport;
import org.ecloudmanager.jeecore.repository.Repository;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;

import javax.inject.Inject;
import java.util.List;

@Repository
public class RecipeRepository extends MongoDBRepositorySupport<Recipe> {
    @Inject
    ApplicationDeploymentRepository applicationDeploymentRepository;

    public Recipe findByName(String name, ApplicationDeployment owner) {
        if (owner == null) {
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("name").equal(name).field("owner").doesNotExist().get();
        } else {
            return owner.getRecipe(name);
        }
    }

    public List<Recipe> getAll(ApplicationDeployment owner) {
        if (owner == null) {
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("owner").doesNotExist().asList();
        } else {
            return owner.getRecipes();
        }
    }

    @Override
    public void save(Recipe recipe) {
        ApplicationDeployment deployment = recipe.getOwner();
        if (deployment == null) {
            super.save(recipe);
        } else {
            if (!deployment.getRecipes().contains(recipe)) {
                deployment.getRecipes().add(recipe);
            }
            applicationDeploymentRepository.save(deployment);
        }
    }

    @Override
    public List<Recipe> getAll() {
        List<Recipe> result = super.getAll();
        applicationDeploymentRepository.getAll().stream().map(ApplicationDeployment::getRecipes).forEach(result::addAll);
        return result;
    }

    @Override
    public void delete(Recipe recipe) {
        ApplicationDeployment deployment = recipe.getOwner();
        if (deployment == null) {
            super.delete(recipe);
        } else {
            deployment.getRecipes().remove(recipe);
            applicationDeploymentRepository.save(deployment);
        }
    }

    public Recipe reload(Recipe recipe) {
        ApplicationDeployment deployment = recipe.getOwner();
        if (deployment == null) {
            return get(recipe.getId());
        } else {
            return applicationDeploymentRepository.reload(deployment).getRecipe(recipe.getId());
        }
    }

    public Recipe get(ObjectId id, ApplicationDeployment deployment) {
        if (deployment == null) {
            return get(id);
        } else {
            return deployment.getRecipe(id);
        }
    }
}

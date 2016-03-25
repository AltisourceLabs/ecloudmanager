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
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.service.ServiceSupport;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class RecipeService extends ServiceSupport {

    @Inject
    private Logger log;

    public void save(Recipe recipe) {
        log.info("Saving " + recipe.getId());
        datastore.save(recipe);
        fireEvent(recipe);
    }

    public void update(Recipe recipe) {
        log.info("Updating " + recipe.getId());
        if (!recipe.isNew() && !recipe.getOldId().equals(recipe.getId())) {
            log.info("Recipe " + recipe.getOldId() + " renamed to " + recipe.getId());
        }
        // FIXME update refereces?
        datastore.delete(Recipe.class, recipe.getOldId());
        datastore.save(recipe);
        fireEvent(recipe);
    }

    public void remove(Recipe recipe) {
        log.info("Deleting " + recipe.getId());
        datastore.delete(recipe);
        fireEvent(recipe);
    }

    public void remove(String id) {
        log.info("Deleting " + id);
        datastore.delete(Recipe.class, id);
        fireEvent(id);
    }

}
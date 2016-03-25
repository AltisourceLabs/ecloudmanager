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
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.repository.Repository;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@Repository
public class RecipeRepository {
    @Inject
    protected Datastore datastore;

    public Long getCount() {
        return datastore.getCount(Recipe.class);
    }

    public Recipe get(ObjectId id) {
        return datastore.get(Recipe.class, id);
    }

    public Recipe get(String id) {
        return datastore.get(Recipe.class, id);
    }

    public List<Recipe> getAll() {
        return datastore.find(Recipe.class).asList();
    }

    public void save(Recipe entity) {
        datastore.save(entity);
    }

    public void saveAll(Collection<Recipe> entities) {
        datastore.save(entities);
    }

    public void update(Recipe entity) {
        datastore.save(entity);
    }

    public void updateAll(Collection<Recipe> entities) {
        datastore.save(entities);
    }

    public void saveOrUpdate(Recipe entity) {
        datastore.save(entity);
    }

    public void saveOrUpdateAll(Collection<Recipe> entities) {
        datastore.save(entities);
    }

    public void delete(Recipe entity) {
        datastore.delete(entity);
    }

    public Recipe reload(Recipe r) {
        return datastore.get(r);
    }

}

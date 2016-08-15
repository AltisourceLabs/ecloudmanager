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

import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.jeecore.repository.MongoDBRepositorySupport;
import org.ecloudmanager.jeecore.repository.Repository;
import org.mongodb.morphia.query.Query;

import java.util.List;

@Repository
public class RecipeRepository extends MongoDBRepositorySupport<Recipe> {
    public List<Recipe> findByName(String name, Deployable owner) {
        if (owner == null) {
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("name").equal(name).field("owner").doesNotExist().asList();
        } else {
            Object ownerKey = datastore.getKey(owner);
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("name").equal(name).field("owner").equal(ownerKey).asList();
        }
    }

    public List<Recipe> getAll(Deployable owner) {
        if (owner == null) {
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("owner").doesNotExist().asList();
        } else {
            Object ownerKey = datastore.getKey(owner);
            return datastore.createQuery(getEntityType()).disableValidation()
                    .field("owner").equal(ownerKey).asList();
        }
    }

    public void deleteAll(Deployable owner) {
        Object ownerKey = datastore.getKey(owner);
        Query<Recipe> query = datastore.createQuery(getEntityType()).disableValidation()
                .field("owner").equal(ownerKey);
        datastore.delete(query);
    }

}

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

package org.ecloudmanager.repository.deployment;

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.jeecore.repository.Repository;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@Repository
public class ApplicationDeploymentRepository {
    @Inject
    protected Datastore datastore;

    public Long getCount() {
        return datastore.getCount(ApplicationDeployment.class);
    }

    public ApplicationDeployment get(ObjectId id) {
        return datastore.get(ApplicationDeployment.class, id);
    }

    public ApplicationDeployment get(String id) {
        return datastore.get(ApplicationDeployment.class, id);
    }

    public List<ApplicationDeployment> getAll() {
        return datastore.find(ApplicationDeployment.class).asList();
    }

    public void save(ApplicationDeployment entity) {
        datastore.save(entity);
    }

    public void saveAll(Collection<ApplicationDeployment> entities) {
        datastore.save(entities);
    }

    public void update(ApplicationDeployment entity) {
        datastore.save(entity);
    }

    public void updateAll(Collection<ApplicationDeployment> entities) {
        datastore.save(entities);
    }

    public void saveOrUpdate(ApplicationDeployment entity) {
        datastore.save(entity);
    }

    public void saveOrUpdateAll(Collection<ApplicationDeployment> entities) {
        datastore.save(entities);
    }

    public void delete(ApplicationDeployment entity) {
        datastore.delete(entity);
    }

    public ApplicationDeployment reload(ApplicationDeployment r) {
        return datastore.get(r);
    }

}


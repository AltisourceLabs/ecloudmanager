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

package org.ecloudmanager.jeecore.repository;

import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

public class MongoDBRepositorySupport<T> {

    @Inject
    protected Datastore datastore;
    @Inject
    private Logger log;
    @SuppressWarnings("unchecked")
    protected Class<T> getEntityType() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    public Long getCount() {
        return datastore.getCount(getEntityType());
    }

    public T get(ObjectId id) {
        return datastore.get(getEntityType(), id);
    }

    public T get(String id) {
        return datastore.get(getEntityType(), new ObjectId(id));
    }

    public List<T> getAll() {
        return datastore.find(getEntityType()).asList();
    }

    public List<T> getAllForUser(String user) {
        return datastore.find(getEntityType(), "owner", user).asList();
    }

    public T find(String property, Object value) {
        return datastore.find(getEntityType(), property, value).get();
    }

    public void save(T entity) {
        try {
            datastore.save(entity);
        } catch (Throwable t) {
            log.error("Failed to save " + entity, t);
        }
    }

    public void saveAll(Collection<T> entities) {
        datastore.save(entities);
    }

    public void update(T entity) {
        save(entity);
    }

    public void updateAll(Collection<T> entities) {
        saveAll(entities);
    }

    public void saveOrUpdate(T entity) {
        save(entity);
    }

    public void saveOrUpdateAll(Collection<T> entities) {
        saveAll(entities);
    }

    public void delete(T entity) {
        datastore.delete(entity);
    }


}

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

package org.ecloudmanager.jeecore.service;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.jeecore.domain.Persistable;
import org.mongodb.morphia.Datastore;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.util.Collection;

public abstract class ServiceSupport {

    @Inject
    private Logger log;

    @Inject
    protected Datastore datastore;

    @Inject
    protected Event<Object> source;

    @Resource
    protected SessionContext ctx;

    protected boolean isUserInRole(String role) {
        return ctx.isCallerInRole(role);
    }

    protected String getUserPrincipalName() {
        return ctx.getCallerPrincipal().getName();
    }

    protected void fireEvent(Object entity) {
        source.fire(entity);
    }

    protected void fireEntityCreated(Persistable<?> entity) {
        source.select(new AnnotationLiteral<EntityCreated>() {
            private static final long serialVersionUID = 1L;
        }).fire(entity);
    }

    protected void fireEntityUpdated(Persistable<?> entity) {
        source.select(new AnnotationLiteral<EntityUpdated>() {
            private static final long serialVersionUID = 1L;
        }).fire(entity);
    }

    protected void fireEntityDeleted(Persistable<?> entity) {
        source.select(new AnnotationLiteral<EntityDeleted>() {
            private static final long serialVersionUID = 1L;
        }).fire(entity);
    }


    protected <T extends Persistable<?>> void save(T entity) {
        if (entity.isNew()) {
            datastore.save(entity);
        } else {
            log.warn("Skipped to save() existing entity. Use update() instead");
        }
    }

    protected <T extends Persistable<?>> void saveAll(Collection<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    protected <T extends Persistable<?>> void update(T entity) {
        if (!entity.isNew()) {
            datastore.merge(entity);
        } else {
            log.warn("Skipped to update() non-existing entity. Use save() instead");
        }
    }

    protected <T extends Persistable<?>> void updateAll(Collection<T> entities) {
        for (T entity : entities) {
            update(entity);
        }
    }

    protected <T extends Persistable<?>> void saveOrUpdate(T entity) {
        if (entity.isNew()) {
            save(entity);
        } else {
            update(entity);
        }
    }

    protected <T extends Persistable<?>> void saveOrUpdateAll(Collection<T> entities) {
        for (T entity : entities) {
            saveOrUpdate(entity);
        }
    }

    protected <T extends Persistable<?>> void delete(T entity) {
        if (!entity.isNew()) {
            datastore.delete(entity);
        } else {
            log.warn("Skipped to delete() non-existing entity.");
        }
    }

    protected <T extends Persistable<?>> void deleteAll(Collection<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

}

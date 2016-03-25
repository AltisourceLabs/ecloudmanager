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

package org.ecloudmanager.jeecore.web.faces.convert;

import org.bson.types.ObjectId;
import org.ecloudmanager.jeecore.domain.Persistable;
import org.mongodb.morphia.Datastore;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;

public class MongoEntityConverter<E extends Persistable<ObjectId>> implements Converter {

    @Inject
    private Datastore datastore;

    @SuppressWarnings("unchecked")
    protected Class<E> getEntityType() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<E>) type.getActualTypeArguments()[0];
    }

    @Override
    public E getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            E result = datastore.get(getEntityType(), new ObjectId(value));
            if (result == null) {
                throw new ConverterException(new FacesMessage(getEntityType().getSimpleName() + " with id " + value +
                    " not found"));
            }
            return result;
        } catch (IllegalArgumentException ex) {
            throw new ConverterException(new FacesMessage("Invalid id format :" + value, ex.getMessage()));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Persistable) {
            return Persistable.class.cast(value).getId().toString();
        }
        return "";
    }

}

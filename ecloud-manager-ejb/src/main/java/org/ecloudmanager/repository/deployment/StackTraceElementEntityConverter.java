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

import com.mongodb.BasicDBObject;
import org.ecloudmanager.domain.StackTraceElementEntity;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.MappingException;

public class StackTraceElementEntityConverter extends TypeConverter implements SimpleValueConverter {
    public StackTraceElementEntityConverter() {
        super(StackTraceElementEntity.class);
    }

    @Override
    public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo)
        throws MappingException {
        StackTraceElementEntity entity = new StackTraceElementEntity();
        if (fromDBObject instanceof BasicDBObject) {
            BasicDBObject basicDBObject = (BasicDBObject) fromDBObject;
            entity.setClassName((String) basicDBObject.get("className"));
            entity.setFileName((String) basicDBObject.get("fileName"));
            entity.setMethodName((String) basicDBObject.get("methodName"));
            entity.setLineNumber((Integer) basicDBObject.get("lineNumber"));
        }

        return entity;
    }

    @Override
    public Object encode(final Object value, final MappedField optionalExtraInfo) {
        return value.toString();
    }
}

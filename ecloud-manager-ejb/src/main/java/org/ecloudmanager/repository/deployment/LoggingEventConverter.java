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
import com.mongodb.DBObject;
import org.ecloudmanager.node.model.LoggingEvent;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.MappingException;

public class LoggingEventConverter extends TypeConverter implements SimpleValueConverter {
    private static final String LEVEL = "level";
    private static final String MESSAGE = "message";
    private static final String LOGGER = "logger";
    private static final String THROWABLE = "throwable";
    private static final String TIMESTAMP = "timeStamp";

    public LoggingEventConverter() {
        super(LoggingEvent.class);
    }

    @Override
    public LoggingEvent decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo)
            throws MappingException {
        DBObject dbObject = (DBObject) fromDBObject;
        return new LoggingEvent().level((String) dbObject.get(LEVEL)).message((String) dbObject.get(MESSAGE)).logger((String) dbObject.get(LOGGER)).throwable((String) dbObject.get(THROWABLE)).timeStamp((Long) dbObject.get(TIMESTAMP));
    }

    @Override
    public Object encode(final Object value, final MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }
        LoggingEvent e = (LoggingEvent) value;
        return new BasicDBObject(LEVEL, e.getLevel())
                .append(MESSAGE, e.getMessage())
                .append(LOGGER, e.getLogger())
                .append(THROWABLE, e.getThrowable())
                .append(TIMESTAMP, e.getTimeStamp())
                ;
    }

}

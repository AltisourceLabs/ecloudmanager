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
package org.ecloudmanager.tmrk.cloudapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * Custom JAXB XML provider to deal with Terremark specific content type.
 * @author irosu
 */
@Produces("application/*")
@Consumes("application/*")
final class CloudapiJAXBMessageBodyProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>,
    CloudapiConstants {

    CloudapiJAXBMessageBodyProvider() {
    }

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType) {
        return mediaType.getSubtype().startsWith(TMRK_MEDIA_SUBTYPE_PREFIX);
    }

    @Override
    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        try {
            return JAXB.unmarshal(entityStream, type);
        } catch (DataBindingException jaxbException) {
            throw new ProcessingException("Error deserializing " + type.getName(),
                jaxbException);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public long getSize(Object t, Class<?> type,
                        Type genericType, Annotation[] annotations, MediaType mediaType) {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public void writeTo(Object t, Class<?> type,
                        Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException,
        WebApplicationException {


        try {
            JAXB.marshal(t, entityStream);
        } catch (DataBindingException jaxbException) {
            throw new ProcessingException("Error serializing " + type.getName(),
                jaxbException);
        }
    }


}
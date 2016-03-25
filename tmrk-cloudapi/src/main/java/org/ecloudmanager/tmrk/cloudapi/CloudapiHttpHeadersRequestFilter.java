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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.util.HttpHeaderNames;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * HTTP request filter to add HTTP headers required by Terremark API.
 *
 * @author irosu
 */
final class CloudapiHttpHeadersRequestFilter implements ClientRequestFilter, CloudapiConstants {
    private Logger log = LogManager.getLogger(CloudapiHttpHeadersRequestFilter.class);

    private final String API_VERSION;

    CloudapiHttpHeadersRequestFilter(final Properties configuration) {
        API_VERSION = configuration.getProperty(TMRK_API_VERSION_PROP, TMRK_API_VERSION);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        log.debug(requestContext.getUri().toASCIIString());
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add(HttpHeaderNames.DATE, new Date());
        headers.putSingle(HttpHeaderNames.ACCEPT, MediaType.APPLICATION_XML);
        headers.add(X_TMRK_VERSION, API_VERSION);

        String method = requestContext.getMethod();
        if (HttpMethod.PUT.equalsIgnoreCase(method)
            || HttpMethod.POST.equalsIgnoreCase(method)) {
            // only content-type should be set here due to Apache HttpClient implementation
            // which will compute and add automatically content-length
            headers.putSingle(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_XML);
        }
    }

}

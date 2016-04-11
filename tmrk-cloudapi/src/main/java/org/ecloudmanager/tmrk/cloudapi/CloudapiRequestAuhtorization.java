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

import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.HttpHeaderNames;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * 
 * @author irosu
 *
 */
final class CloudapiRequestAuhtorization implements HttpRequestInterceptor, CloudapiConstants {

    private final Supplier<Properties> configuration;

    CloudapiRequestAuhtorization(final Supplier<Properties> configuration) {
        this.configuration = configuration;
    }

    @Override
    public void process(HttpRequest request, HttpContext context)
            throws HttpException, IOException {
        request.addHeader(HttpHeaderNames.AUTHORIZATION, encodeAuthorizationKeys(request));
    }

    private String encodeAuthorizationKeys(HttpRequest request) {
        Properties properties = configuration.get();
        String apiAccessKey = properties.getProperty(TMRK_API_ACCESS_KEY_PROP);
        String apiPrivateKey = properties.getProperty(TMRK_API_PRIVATE_KEY_PROP);

        String authorization = "CloudApi" + " AccessKey=" + '"'
                + apiAccessKey + '"' + " SignatureType=" + '"'
                + SIGNATURE_TYPE + '"' + " Signature=" + '"'
                + signature((HttpUriRequest)request, apiPrivateKey) + '"';
        return authorization;
    }

    private String signature(HttpUriRequest request, String apiPrivateKey) {
        StringBuilder sb = new StringBuilder();
        String verb = request.getMethod().toUpperCase();
        String date = request.getFirstHeader(HttpHeaderNames.DATE).getValue();
        Header contentTypeHeader = request.getFirstHeader(HttpHeaderNames.CONTENT_TYPE);
        String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;
        Header contentLengthHeader = request.getFirstHeader(HttpHeaderNames.CONTENT_LENGTH);
        String contentLength = contentLengthHeader != null ? contentLengthHeader.getValue() : null;

        sb.append(verb).append("\n");
        sb.append(contentLength != null ? contentLength.trim() : "").append("\n");
        sb.append(contentType != null ? contentType.trim() : "").append("\n");
        sb.append(date).append("\n");
        HeaderIterator hit = request.headerIterator();
        Headers<Object> headers = new Headers<>();
        while(hit.hasNext()) {
            Header hdr = hit.nextHeader();
            headers.add(hdr.getName(), hdr.getValue());
        }
        sb.append(canonicalizedHeaders(headers));
        sb.append(canonicalizedResource(new ResteasyUriInfo(request.getURI())));

        String sigstr = sb.toString();
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(getBytes(apiPrivateKey), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Base64.encodeBytes(sha256_HMAC.doFinal(getBytes(sigstr)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String canonicalizedResource(UriInfo uriInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(uriInfo.getPath().toLowerCase()).append("\n");
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        for (String key : params.keySet()) {
            sb.append(key.toLowerCase()).append(":").append(params.get(key))
            .append("\n");
        }

        return sb.toString();
    }

    private String canonicalizedHeaders(MultivaluedMap<String, Object> headers) {
        StringBuilder sb = new StringBuilder();
        for (String key : headers.keySet()) {
            if (key.toLowerCase().startsWith(TMRK_HEADER_PREFIX) && !key.equalsIgnoreCase(X_TMRK_AUTHORIZATION)) {
                sb.append(key.toLowerCase()).append(":")
                .append(headers.getFirst(key).toString().trim()).append("\n");
            }
        }

        return sb.toString();
    }

    private byte[] getBytes(String str) throws UnsupportedEncodingException {
        return str.getBytes("UTF-8");
    }
}

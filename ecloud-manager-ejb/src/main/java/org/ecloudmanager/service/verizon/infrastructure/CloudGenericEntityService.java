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

package org.ecloudmanager.service.verizon.infrastructure;

import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.tmrk.cloudapi.model.ResourceType;
import org.ecloudmanager.tmrk.cloudapi.service.EntityService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Generic end point which uses 'href' for Terremark resources.
 *
 * @author irosu
 */
@Service
public class CloudGenericEntityService {

    @Inject
    private EntityService delegate;

    public <T extends ResourceType> T getByHref(Class<T> type, String href) {
        if (!href.startsWith("/cloudapi/")) {
            throw new IllegalArgumentException("Usnupported Terremark E-Cloud resource: " + href);
        }
        Response response = delegate.getByHref(href.substring(1));
        return response.readEntity(type);
    }

}

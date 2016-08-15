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

package org.ecloudmanager.web.faces;

import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.omnifaces.cdi.Param;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

@Controller
public class EndpointController extends FacesSupport implements Serializable {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private String valueParamId;

    private Endpoint value;

    @PostConstruct
    public void init() {
        value = (Endpoint) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(valueParamId);
    }

    public Endpoint getValue() {
        return value;
    }

    public void setValue(Endpoint value) {
        this.value = value;
    }

    public void handleClose(CloseEvent event) {

    }

    public void saveEditedEndpoint() {
        RequestContext.getCurrentInstance().closeDialog(value);
    }

    public void cancelEditing() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }
}

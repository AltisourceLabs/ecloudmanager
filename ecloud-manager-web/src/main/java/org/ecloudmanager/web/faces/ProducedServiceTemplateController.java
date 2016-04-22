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

import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;

import java.io.Serializable;

@Controller
public class ProducedServiceTemplateController extends FacesSupport implements Serializable {
    public static String DIALOG_EDIT = "dlg_edit_service";
    private ProducedServiceTemplate value;
    private boolean newChild = false;

    public ProducedServiceTemplate getValue() {
        return value;
    }

    public void setValue(ProducedServiceTemplate value) {
        this.value = value;
    }

    public void handleClose(CloseEvent event) {

    }

    public void startEditComponentGroup(ComponentGroupTemplate componentGroupTemplate) {
        ComponentGroupTemplateController controller = Beans.getInstance(ComponentGroupTemplateController.class);
        controller.setValue(componentGroupTemplate);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(ComponentGroupTemplateController.DIALOG_EDIT);
        ctx.execute("PF('" + ComponentGroupTemplateController.DIALOG_EDIT + "').show()");
    }

    public void deleteComponentGroup(ComponentGroupTemplate componentGroupTemplate) {

    }

    public void newComponentGroup() {
        newChild = true;
        startEditComponentGroup(new ComponentGroupTemplate());
    }

    public void save() {
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('dlg_edit_service').hide()");
//        RequestContext.getCurrentInstance().closeDialog(value);
    }

    public void cancel() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void saveComponentGroup(ComponentGroupTemplate cg) {
        if (newChild) {
            value.getComponentGroups().add(cg);
            newChild = false;
        }
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + ComponentGroupTemplateController.DIALOG_EDIT + "').hide()");
        ctx.update(DIALOG_EDIT);

        ctx.execute("PF('" + DIALOG_EDIT + "').show()");
    }

    public void cancelComponentGroupEditing(ComponentGroupTemplate cg) {
        newChild = false;
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + ComponentGroupTemplateController.DIALOG_EDIT + "').hide()");
        ctx.update(DIALOG_EDIT);
        ctx.execute("PF('" + DIALOG_EDIT + "').show()");

    }

}

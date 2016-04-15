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

import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.es.ExternalServiceTemplate;
import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
import org.ecloudmanager.deployment.vm.VMTemplateReference;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TemplateEntityController extends EntityEditorController<Template> {
    private static final long serialVersionUID = -4260684280722977470L;
    private static Map<Class, String> dialogs = new HashMap<>();

    static {
        dialogs.put(ProducedServiceTemplate.class, "dlg_edit_service");
        dialogs.put(ExternalServiceTemplate.class, "dlg_edit_ext_service");
        dialogs.put(VMTemplateReference.class, "dlg_edit_vm_ref");
    }

    private ApplicationTemplateController applicationTemplateController = Beans.getInstance
            (ApplicationTemplateController.class, false);

    protected TemplateEntityController() {
        super(ProducedServiceTemplate.class);
    }

    public void newProducedService() {
        setClassImpl(ProducedServiceTemplate.class);
        showDialog(ProducedServiceTemplate.class);
    }

    public void newExternalService() {
        setClassImpl(ExternalServiceTemplate.class);
        showDialog(ExternalServiceTemplate.class);
    }

    public void newVmRef() {
        setClassImpl(VMTemplateReference.class);
        showDialog(VMTemplateReference.class);
    }


    @Override
    public void delete(Template entity) {
        applicationTemplateController.deleteChild(entity);
    }

    @Override
    protected void doSave(Template old, Template entity) {
        applicationTemplateController.saveChild();
    }


    @Override
    protected void doAdd(Template entity) {
        applicationTemplateController.addChild();
    }

    public void startEdit(Template entity) {
        super.startEdit(entity);
        showDialog(entity.getClass());
    }

    private void showDialog(Class c) {
        String dialog = dialogs.get(c);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(dialog);
        ctx.execute("PF('" + dialog + "').show()");

    }

    public void hideDialogs(boolean showTemplateDialog) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        dialogs.values().forEach(d ->
            ctx.execute("PF('" + d + "').hide()")
        );
        if (showTemplateDialog) {
            ctx.execute("PF('dlg_edit').show()");
        }
    }

    public void handleClose(CloseEvent event) {
        super.handleClose(event);
        hideDialogs(true);
    }
}

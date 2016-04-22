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

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.es.ExternalServiceTemplate;
import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
import org.ecloudmanager.deployment.vm.VMTemplateReference;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.ApplicationTemplateRepository;
import org.ecloudmanager.service.template.ApplicationTemplateService;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ApplicationTemplateController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -4809521504415887873L;
    private static Map<Class, String> dialogs = new HashMap<>();

    static {
        dialogs.put(ProducedServiceTemplate.class, ProducedServiceTemplateController.DIALOG_EDIT);
        dialogs.put(ExternalServiceTemplate.class, ExternalServiceTemplateController.DIALOG_EDIT);
        dialogs.put(VMTemplateReference.class, VirtualMachineTemplateReferenceController.DIALOG_EDIT);
    }

    private boolean newChild = false;
    private boolean newTemplate = false;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "applicationTemplateConverter")
    private ApplicationTemplate template;
    @Inject
    private Logger log;
    @Inject
    private transient ApplicationTemplateRepository applicationTemplateRepository;
    @Inject
    private transient ApplicationTemplateService applicationTemplateService;

    public ApplicationTemplate getTemplate() {
        return template;
    }

    @PostConstruct
    public void init() {
        if (template == null) {
            newTemplate = true;
            template = new ApplicationTemplate();
        }
    }


    public void save() {
        if (newTemplate) {
            applicationTemplateService.saveApp(template);
            newTemplate = false;
        } else {
            applicationTemplateService.updateApp(template);
        }
    }

    public void startEditChild(Template child) {
        if (child instanceof ProducedServiceTemplate) {
            ProducedServiceTemplateController controller = Beans.getInstance(ProducedServiceTemplateController.class);
            controller.setValue((ProducedServiceTemplate) child);
        } else if (child instanceof VMTemplateReference) {
            VirtualMachineTemplateReferenceController controller = Beans.getInstance(VirtualMachineTemplateReferenceController.class);
            controller.setValue((VMTemplateReference) child);
        } else if (child instanceof ExternalServiceTemplate) {
            ExternalServiceTemplateController controller = Beans.getInstance(ExternalServiceTemplateController.class);
            controller.setValue((ExternalServiceTemplate) child);
        } else {
            throw new RuntimeException("Uncnown instance type: " + child.getClass().getName());
        }
        showDialog(child.getClass());
    }

    public void deleteChild(Template child) {
        template.removeChild(child);
    }

    public List<String> getAvailableEndpoints() {
        List<String> result = new ArrayList<>();
        template.getChildren().forEach(t -> t.getEndpoints().forEach(e -> result.add(t.getName() + ":" + e.getName())));
        return result;
    }

    public void newProducedService() {
        newChild = true;
        startEditChild(new ProducedServiceTemplate());
    }

    public void newExternalService() {
        newChild = true;
        startEditChild(new ExternalServiceTemplate());

    }

    public void newVmRef() {
        newChild = true;
        startEditChild(new VMTemplateReference());
    }

    public void cancel() {
    }

    private void showDialog(Class c) {
        String dialog = dialogs.get(c);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(dialog);
        ctx.execute("PF('" + dialog + "').show()");
    }

    private void hideDialog(Class c) {
        String dialog = dialogs.get(c);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + dialog + "').hide()");
        ctx.update("out");
    }

    public void saveChild(Template c) {
        if (newChild) {
            template.addChild(c);
            newChild = false;
        }
        hideDialog(c.getClass());
    }

    public void cancelChildEditing(Template c) {
        newChild = false;
        hideDialog(c.getClass());
    }
}

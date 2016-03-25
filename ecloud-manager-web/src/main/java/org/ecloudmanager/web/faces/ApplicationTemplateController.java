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

import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.ps.FirewallRule;
import org.ecloudmanager.deployment.ps.ProducedServiceTemplate;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupTemplate;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.ApplicationTemplateRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.template.ApplicationTemplateService;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Controller
public class ApplicationTemplateController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = 418539347548538918L;

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    private Part file;
    private List<VirtualMachineTemplate> virtualMachineTemplates;
    @Inject
    private transient VirtualMachineTemplateRepository virtualMachineTemplateRepository;
    @Inject
    private transient ApplicationTemplateRepository applicationRepository;
    @Inject
    private transient ApplicationTemplateService applicationTemplateService;

    @Inject
    private ApplicationEntityEditorController applicationEntityEditorController;

    public TemplateEntityController getTemplateEntityEditorController() {
        return templateEntityEditorController;
    }

    @Inject
    private TemplateEntityController templateEntityEditorController;

    private List<ApplicationTemplate> applicationTemplates;

    private FirewallRule.Protocol[] firewallRuleProtocols = FirewallRule.Protocol.values();

    public List<VirtualMachineTemplate> getVirtualMachineTemplates() {
        return virtualMachineTemplates;
    }

    @PostConstruct
    private void init() {
        refresh();
    }

    void refresh() {
        applicationTemplates = applicationRepository.getAll();
        virtualMachineTemplates = virtualMachineTemplateRepository.getAll();
    }


    public List<ApplicationTemplate> getAppTemplates() {
        return applicationTemplates;
    }


    private EntityEditorController<ComponentGroupTemplate> componentGroupTemplateEntityEditorController = new
        ListEntityEditorController<ComponentGroupTemplate>(ComponentGroupTemplate.class) {
        @Override
        protected List<ComponentGroupTemplate> getList() {
            if (templateEntityEditorController.getSelected() instanceof ProducedServiceTemplate) {
                return ((ProducedServiceTemplate) templateEntityEditorController.getSelected()).getComponentGroups();
            }
            return Collections.emptyList();
        }
    };

    public EntityEditorController<ComponentGroupTemplate> getComponentGroupTemplateEntityEditorController() {
        return componentGroupTemplateEntityEditorController;
    }

    public void deleteChild(Template t) {
        applicationEntityEditorController.getSelected().getChildren().remove(t);
    }

    public void addChild() {
        applicationEntityEditorController.getSelected().getChildren().add(templateEntityEditorController.getSelected());
        templateEntityEditorController.hideDialogs(true);

        templateEntityEditorController.init();
    }

    public void saveChild() {
        int i = applicationEntityEditorController.getSelected().getChildren().indexOf(templateEntityEditorController
            .getOld());
        applicationEntityEditorController.getSelected().getChildren().set(i, templateEntityEditorController
            .getSelected());
        templateEntityEditorController.hideDialogs(true);
        templateEntityEditorController.init();
    }


    private EntityEditorController<FirewallRule> firewallRuleEntityEditorController = new
        ListEntityEditorController<FirewallRule>(FirewallRule.class) {
        @Override
        protected List<FirewallRule> getList() {
            if (templateEntityEditorController.getSelected() instanceof ProducedServiceTemplate) {
                return ((ProducedServiceTemplate) templateEntityEditorController.getSelected()).getFirewallRules();
            }
            return Collections.emptyList();
        }
    };


    public ApplicationEntityEditorController getApplicationEntityEditorController() {
        return applicationEntityEditorController;
    }

    public EntityEditorController<FirewallRule> getFirewallRuleEntityEditorController() {
        return firewallRuleEntityEditorController;
    }

    public FirewallRule.Protocol[] getFirewallRuleProtocols() {
        return firewallRuleProtocols;
    }

    public void download(ApplicationTemplate app) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        ec.responseReset();
        ec.setResponseContentType("text/yaml");
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + app.getName() + ".yaml" + "\"");
        OutputStream output = ec.getResponseOutputStream();
        String yaml = applicationTemplateService.toYaml(app);
        output.write(yaml.getBytes());
        output.close();
        fc.responseComplete();
    }


}


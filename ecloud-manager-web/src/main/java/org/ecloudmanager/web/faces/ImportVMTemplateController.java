/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Altisource Labs
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

import org.ecloudmanager.deployment.core.ObjectRef;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ImportVMTemplateController extends FacesSupport implements Serializable {
    @Inject
    private transient VirtualMachineTemplateRepository virtualMachineTemplateRepository;
    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private transient VirtualMachineTemplateService vmtemplateService;
    @Inject
    private transient VmTemplateController vmTemplateController;
    @Inject
    private transient ApplicationDeploymentEditorController applicationDeploymentEditorController;

    private ObjectRef selectedTemplate;
    private List<ObjectRef> templateRefs = new ArrayList<>();

    @PostConstruct
    public void init() {
        templateRefs.clear();

        List<VirtualMachineTemplate> templates = virtualMachineTemplateRepository.getAll();
        templateRefs.addAll(templates.stream().map(t -> new ObjectRef(null, t)).collect(Collectors.toList()));

        List<ComponentGroupDeployment> componentGroupDeployments = applicationDeploymentRepository.getAll().stream()
                .flatMap(d -> d.stream(ComponentGroupDeployment.class))
                .collect(Collectors.toList());
        List<VMDeployment> vmDeployments = applicationDeploymentRepository.getAll().stream()
                .flatMap(d -> d.children(VMDeployment.class).stream())
                .collect(Collectors.toList());

        templateRefs.addAll(
                vmDeployments.stream()
                        .map(d -> new ObjectRef(d, d.getVirtualMachineTemplate())).collect(Collectors.toList())
            );
        templateRefs.addAll(
                componentGroupDeployments.stream()
                        .map(d -> new ObjectRef(d, d.getVirtualMachineTemplate())).collect(Collectors.toList())
            );
    }

    public List<ObjectRef> getTemplateRefs() {
        return templateRefs;
    }

    public void setTemplateRefs(List<ObjectRef> templateRefs) {
        this.templateRefs = templateRefs;
    }

    public ObjectRef getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(ObjectRef selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public void save() {
        if (selectedTemplate != null) {
            VirtualMachineTemplate virtualMachineTemplate = (VirtualMachineTemplate) selectedTemplate.getObject();
            if (virtualMachineTemplate != null) {
                String message = vmtemplateService.importVm(virtualMachineTemplate, vmTemplateController.getValue(), applicationDeploymentEditorController.getDeployment());
                if (!message.isEmpty()) {
                    addMessage(new FacesMessage(message));
                }
            }
        }
    }

    public void cancel() {
        selectedTemplate = null;
    }
}

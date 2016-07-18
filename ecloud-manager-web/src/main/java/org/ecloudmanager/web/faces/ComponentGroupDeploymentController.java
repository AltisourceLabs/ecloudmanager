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

import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Controller
public class ComponentGroupDeploymentController extends FacesSupport implements Serializable {
    public static String DIALOG_EDIT = "dlg_edit_cg";
    private ComponentGroupDeployment value;

    @Inject
    private transient VirtualMachineTemplateRepository virtualMachineTemplateRepository;
    private List<VirtualMachineTemplate> virtualMachineTemplates;

    @PostConstruct
    private void init() {
        refresh();
    }

    void refresh() {
        virtualMachineTemplates = virtualMachineTemplateRepository.getAll();
    }

    public List<VirtualMachineTemplate> getVirtualMachineTemplates() {
        return virtualMachineTemplates;
    }

    public ComponentGroupDeployment getValue() {
        return value;
    }

    public void setValue(ComponentGroupDeployment value) {
        this.value = value;
    }

    public List<String> generateHaproxyBackendConfig() {
        return value == null ? Collections.emptyList() : HAProxyDeployer.generateHAProxyBackendConfig(value.getName(), value.getHaProxyBackendConfig());
    }

}

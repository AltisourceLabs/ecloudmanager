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

import org.ecloudmanager.deployment.gateway.GatewayDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.GatewayRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.deployment.GatewayService;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GatewaysController extends FacesSupport implements Serializable {
    @Inject
    private transient GatewayService gatewayService;
    @Inject
    private transient ApplicationDeploymentService applicationDeploymentService;
    @Inject
    private transient GatewayRepository gatewayRepository;
    @Inject
    private transient VirtualMachineTemplateRepository virtualMachineTemplateRepository;
    @Inject
    private transient NodeAPIConfigurationService nodeAPIProvider;

    private List<GatewayDeployment> gatewayDeployments;

    private String newGatewayName;
    private VirtualMachineTemplate newGatewayVmTemplate;
    private MenuModel nodeAPIMenuModel;

    @PostConstruct
    private void init() {
        nodeAPIMenuModel = new DefaultMenuModel();
        nodeAPIProvider.getAPIs().forEach(a -> {
                    DefaultMenuItem item = new DefaultMenuItem(a);
                    item.setCommand("#{gatewaysController.goToDeployment('" + a + "')}");
                    item.setIcon("ui-icon-circle-triangle-e");
                    nodeAPIMenuModel.addElement(item);
                }
        );
        refresh();
    }

    void refresh() {
        gatewayDeployments = gatewayRepository.getAll();

    }

    public List<GatewayDeployment> getGatewayDeployments() {
        return gatewayDeployments;
    }

    public void delete(GatewayDeployment entity) {
        gatewayService.remove(entity);
        refresh();
    }

    public boolean isDeployed(GatewayDeployment gatewayDeployment) {
        return applicationDeploymentService.isDeployed(gatewayDeployment);
    }

    public List<VirtualMachineTemplate> getVmTemplates() {
        return virtualMachineTemplateRepository.getAll().stream()
                .filter(vmTemplate -> vmTemplate.getRunlist().isEmpty())
                .collect(Collectors.toList());
    }

    public String getNewGatewayName() {
        return newGatewayName;
    }

    public void setNewGatewayName(String newGatewayName) {
        this.newGatewayName = newGatewayName;
    }

    public VirtualMachineTemplate getNewGatewayVmTemplate() {
        return newGatewayVmTemplate;
    }

    public void setNewGatewayVmTemplate(VirtualMachineTemplate newGatewayVmTemplate) {
        this.newGatewayVmTemplate = newGatewayVmTemplate;
    }

    private void reset() {
        newGatewayName = null;
        newGatewayVmTemplate = null;
    }

    public void cancel() {
        reset();
    }

    public String goToDeployment(String infrastructure) {
        return "/editApp/editApplicationDeployment?faces-redirect=true" +
               "&gatewayName=" + newGatewayName +
               "&vmTemplate=" + newGatewayVmTemplate.getId().toString() +
               "&tabindex=2" +
               "&infrastructure=" + infrastructure;
    }

    public MenuModel getNodeAPIMenuModel() {
        return nodeAPIMenuModel;
    }

}


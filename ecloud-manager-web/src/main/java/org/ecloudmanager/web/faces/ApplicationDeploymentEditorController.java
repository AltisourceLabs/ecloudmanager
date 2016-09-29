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

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.es.ExternalServiceDeployment;
import org.ecloudmanager.deployment.gateway.GatewayDeployment;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.ecloudmanager.service.deployment.ApplicationDeploymentService;
import org.ecloudmanager.service.deployment.GatewayService;
import org.ecloudmanager.service.deployment.ImportDeployableService;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ApplicationDeploymentEditorController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -4809521504415887873L;
    private static Map<Class, String> dialogs = new HashMap<>();

    static {
        dialogs.put(ProducedServiceDeployment.class, ProducedServiceDeploymentController.DIALOG_EDIT);
        dialogs.put(ExternalServiceDeployment.class, ExternalServiceDeploymentController.DIALOG_EDIT);
        dialogs.put(VMDeployment.class, TopLevelVMDeploymentController.DIALOG_EDIT);
    }
    private boolean newChild = false;
    private String publicEndpointToAdd;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "topLevelDeployableConverter")
    private ApplicationDeployment deployment;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private Integer tabindex;

    // Parameters for a gateway deployment
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "vmTemplateConverter")
    private VirtualMachineTemplate vmTemplate;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private String gatewayName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param
    private String infrastructure;


    @Inject
    private transient ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private transient ApplicationDeploymentService applicationDeploymentService;
    @Inject
    private transient NodeAPIConfigurationService nodeAPIProvider;
    @Inject
    private transient ImportDeployableService importDeployableService;
    @Inject
    private transient ImportDeployableController importDeployableController;
    @Inject
    private transient GatewayService gatewayService;
    @Inject
    ApplicationDeploymentController applicationDeploymentController;

    public ApplicationDeployment getDeployment() {
        return deployment;
    }

    public List<String> getNodeApiIds() {
        return nodeAPIProvider.getAPIs();
    }

    public Integer getTabindex() {
        return tabindex;
    }

    public void setTabindex(Integer tabindex) {
        this.tabindex = tabindex;
    }

    @PostConstruct
    public void init() {
        if (gatewayName != null) {
            deployment = gatewayService.create(gatewayName, vmTemplate, infrastructure);
        }
    }

    public void save() {
        try {
            deployment.specifyConstraints();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Can't specify deployment constraints", e.getMessage()));
        }
        if (deployment.isNew()) {
            applicationDeploymentService.save(deployment);
        } else {
            applicationDeploymentService.update(deployment);
        }
        addMessage(new FacesMessage("Application deployment saved.", ""));
        applicationDeploymentController.save();
        applicationDeploymentController.resetTree();
    }

    public void startEditChild(Deployable child) {
        if (child instanceof ProducedServiceDeployment) {
            ProducedServiceDeploymentController controller = Beans.getInstance(ProducedServiceDeploymentController.class);
            controller.setValue((ProducedServiceDeployment) child);
        } else if (child instanceof VMDeployment) {
            TopLevelVMDeploymentController controller = Beans.getInstance(TopLevelVMDeploymentController.class);
            controller.setValue((VMDeployment) child);
        } else if (child instanceof ExternalServiceDeployment) {
            ExternalServiceDeploymentController controller = Beans.getInstance(ExternalServiceDeploymentController.class);
            controller.setValue((ExternalServiceDeployment) child);
        } else {
            throw new RuntimeException("Unknown instance type: " + child.getClass().getName());
        }
        showDialog(child.getClass());
    }

    public void deleteChild(Deployable child) {
        deployment.removeChild(child);
    }

    public List<String> getAvailableEndpoints() {
        List<String> result = new ArrayList<>();
        deployment.children(Deployable.class).forEach(t -> {
            result.addAll(t.getEndpointsIncludingTemplateName());
        });
        return result;
    }

    public void newProducedService() {
        newChild = true;
        ProducedServiceDeployment producedServiceDeployment = new ProducedServiceDeployment();
        producedServiceDeployment.setParent(deployment);
        producedServiceDeployment.children().add(new Endpoint());
        startEditChild(producedServiceDeployment);
    }

    public void newExternalService() {
        newChild = true;
        startEditChild(new ExternalServiceDeployment());

    }

    public void newVmRef() {
        newChild = true;
        VMDeployment vmDeployment = new VMDeployment();
        vmDeployment.setParent(deployment);
        startEditChild(vmDeployment);
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

    public void saveChild(Deployable c) {
        if (newChild) {
            deployment.addChild(c);
            newChild = false;
        } else {
            deployment.updateLinks();
        }
        hideDialog(c.getClass());
    }

    public void cancelChildEditing(Deployable c) {
        newChild = false;
        hideDialog(c.getClass());
    }

    public void deletePublicEndpoint(String ep) {
        deployment.getPublicEndpoints().remove(ep);
    }

    public String getPublicEndpointToAdd() {
        return publicEndpointToAdd;
    }

    public void setPublicEndpointToAdd(String publicEndpointToAdd) {
        this.publicEndpointToAdd = publicEndpointToAdd;
    }

    public void addPublicEndpoint() {
        if (publicEndpointToAdd != null) {
            deployment.getPublicEndpoints().add(publicEndpointToAdd);
            publicEndpointToAdd = null;
        }
    }

    public List<DeploymentObject> editableChildren() {
        return deployment.children().stream()
                .filter(c -> !(c instanceof ChefEnvironment))
                .collect(Collectors.toList());
    }

    public void startImportDeployable() {
        importDeployableController.openDialog(
                ImmutableSet.of(ProducedServiceDeployment.class, VMDeployment.class),
                false,
                result -> {
                    if (result != null) {
                        Deployable deployable = (Deployable) result.getObject();
                        if (deployable != null) {
                            String name = StringUtils.isEmpty(result.getName()) ? deployable.getName() : result.getName();
                            importDeployableService.copyDeploymentObject(deployable, deployment, name, result.getIncludeConstraints());
                        }
                    }
                    RequestContext.getCurrentInstance().update("out");
                }
        );
    }

    public boolean isGatewayDeployment() {
        return deployment instanceof GatewayDeployment;
    }
}

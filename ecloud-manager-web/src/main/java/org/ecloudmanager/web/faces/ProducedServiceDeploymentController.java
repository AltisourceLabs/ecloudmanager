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

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.ps.BackendWeight;
import org.ecloudmanager.deployment.ps.HAProxyDeployer;
import org.ecloudmanager.deployment.ps.ProducedServiceDeployment;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.service.deployment.ImportDeployableService;
import org.omnifaces.util.Beans;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Controller
public class ProducedServiceDeploymentController extends FacesSupport implements Serializable {
    public static String DIALOG_EDIT = "dlg_edit_service";
    private ProducedServiceDeployment value;
    private boolean newChild = false;

    @Inject
    private transient VmTemplateController vmTemplateController;
    @Inject
    private transient ImportDeployableService importDeployableService;

    public ProducedServiceDeployment getValue() {
        return value;
    }

    public void setValue(ProducedServiceDeployment value) {
        this.value = value;
    }

    public void handleClose(CloseEvent event) {

    }

    public void startEditComponentGroup(ComponentGroupDeployment componentGroupDeployment) {
        ComponentGroupDeploymentController controller = Beans.getInstance(ComponentGroupDeploymentController.class);
        controller.setValue(componentGroupDeployment);
        vmTemplateController.setValue(componentGroupDeployment.getVirtualMachineTemplate());
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(ComponentGroupDeploymentController.DIALOG_EDIT);
        ctx.execute("PF('" + ComponentGroupDeploymentController.DIALOG_EDIT + "').show()");
    }

    public void deleteComponentGroup(ComponentGroupDeployment componentGroupDeployment) {
        value.children().remove(componentGroupDeployment);
    }

    public void newComponentGroup() {
        newChild = true;
        ComponentGroupDeployment componentGroupDeployment = new ComponentGroupDeployment();
        componentGroupDeployment.setParent(value);
        startEditComponentGroup(componentGroupDeployment);
    }

    public void save() {
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('dlg_edit_service').hide()");
//        RequestContext.getCurrentInstance().closeDialog(value);
    }

    public void cancel() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void saveComponentGroup(ComponentGroupDeployment cg) {
        if (newChild) {
            value.addChild(cg);
            newChild = false;
        }
        // Create the first vm if it is missing
        if (cg.getVirtualMachineTemplate() != null && cg.children(VMDeployment.class).size() == 0) {
            cg.addVm();
        }
        // Propagate infrastructure and vm template to vm deployments
        cg.children(VMDeployment.class).forEach(vmDeployment -> {
            vmDeployment.setVirtualMachineTemplate(cg.getVirtualMachineTemplate());
        });
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + ComponentGroupDeploymentController.DIALOG_EDIT + "').hide()");
        ctx.update(DIALOG_EDIT);

        ctx.execute("PF('" + DIALOG_EDIT + "').show()");
    }

    public void cancelComponentGroupEditing(ComponentGroupDeployment cg) {
        newChild = false;
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + ComponentGroupDeploymentController.DIALOG_EDIT + "').hide()");
        ctx.update(DIALOG_EDIT);
        ctx.execute("PF('" + DIALOG_EDIT + "').show()");

    }

    public boolean getAbTestingEnabled() {
        return value.getHaProxyFrontendConfig().getBackendWeights().size() > 0;
    }

    public void setAbTestingEnabled(boolean abTestingEnabled) {
        List<BackendWeight> weights = value.getHaProxyFrontendConfig().getBackendWeights();
        if (!abTestingEnabled) {
            weights.clear();
        } else if (weights.isEmpty()) {
            List<ComponentGroupDeployment> componentGroups = value.getComponentGroups();
            componentGroups.forEach((componentGroup) -> {
                weights.add(new BackendWeight(componentGroup.getName(), 100/componentGroups.size()));
            });
            if (weights.size() > 0) {
                int sum = weights.stream().mapToInt(BackendWeight::getWeight).sum();
                weights.get(0).setWeight(weights.get(0).getWeight() + 100 - sum);
            }
        }
    }

    public List<String> generateHaproxyFrontendConfig() {
        return value == null ? Collections.emptyList() : HAProxyDeployer.generateHAProxyFrontendConfig(value.getName(), value.getHaProxyFrontendConfig());
    }

    public void startImportComponentGroup() {
        HashMap<String, Object> options = new HashMap<>();
        options.put("width", 640);
        options.put("modal", true);
        HashMap<String, List<String>> params = new HashMap<>();
        params.put("classes", Collections.singletonList(ComponentGroupDeployment.class.getName()));
        params.put("recursive", Collections.singletonList("true"));

        RequestContext.getCurrentInstance().openDialog("importDeployable", options, params);
    }

    public void onImportComponentGroupReturn(SelectEvent event) {
        ImportDeployableController.ImportDeployableDialogResult result = (ImportDeployableController.ImportDeployableDialogResult) event.getObject();
        if (result != null) {
            Deployable deployable = (Deployable) result.getObject();
            if (deployable != null) {
                String name = StringUtils.isEmpty(result.getName()) ? deployable.getName() : result.getName();
                importDeployableService.copyDeploymentObject(deployable, value, name, result.getIncludeConstraints());
            }
        }
    }
}

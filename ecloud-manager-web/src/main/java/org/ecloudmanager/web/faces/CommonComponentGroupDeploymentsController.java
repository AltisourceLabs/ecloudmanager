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
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.ComponentGroupDeploymentRepository;
import org.ecloudmanager.service.deployment.ImportDeployableService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Controller
public class CommonComponentGroupDeploymentsController extends FacesSupport implements Serializable {
    private List<ComponentGroupDeployment> componentGroupDeployments;

    @Inject
    private transient ComponentGroupDeploymentRepository componentGroupDeploymentRepository;
    @Inject
    private transient ImportDeployableService importDeployableService;
    @Inject
    private transient ComponentGroupDeploymentController componentGroupDeploymentController;
    @Inject
    private transient VmTemplateController vmTemplateController;

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        componentGroupDeployments = componentGroupDeploymentRepository.getAll();
    }

    public List<ComponentGroupDeployment> getComponentGroupDeployments() {
        return componentGroupDeployments;
    }

    public void startEdit(ComponentGroupDeployment componentGroupDeployment) {
        if (componentGroupDeployment == null) {
            componentGroupDeployment = new ComponentGroupDeployment();
        }
        componentGroupDeploymentController.setValue(componentGroupDeployment);
        vmTemplateController.setValue(componentGroupDeployment.getVirtualMachineTemplate());
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(ComponentGroupDeploymentController.DIALOG_EDIT);
        ctx.execute("PF('" + ComponentGroupDeploymentController.DIALOG_EDIT + "').show()");
    }

    public void delete(ComponentGroupDeployment entity) {
        componentGroupDeploymentRepository.delete(entity);
        refresh();
    }

    public void startImportCG() {
        HashMap<String, Object> options = new HashMap<>();
        options.put("width", 640);
        options.put("modal", true);

        HashMap<String, List<String>> params = new HashMap<>();
        params.put("classes", Collections.singletonList(ComponentGroupDeployment.class.getName()));
        params.put("recursive", Collections.singletonList("true"));

        RequestContext.getCurrentInstance().openDialog("/editApp/import/importDeployable", options, params);
    }

    public void onImportCGReturn(SelectEvent event) {
        ImportDeployableController.ImportDeployableDialogResult result = (ImportDeployableController.ImportDeployableDialogResult) event.getObject();
        if (result != null) {
            Deployable deployable = (Deployable) result.getObject();
            if (deployable != null) {
                String name = StringUtils.isEmpty(result.getName()) ? deployable.getName() : result.getName();
                Deployable newDeployable = importDeployableService.copyDeploymentObject(deployable, null, name, result.getIncludeConstraints());
                componentGroupDeploymentRepository.save((ComponentGroupDeployment) newDeployable);
                refresh();
            }
        }
    }

    public void save(ComponentGroupDeployment componentGroupDeployment) {
        componentGroupDeploymentRepository.save(componentGroupDeployment);
        refresh();
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + ComponentGroupDeploymentController.DIALOG_EDIT + "').hide()");
        ctx.update("out");
    }
}

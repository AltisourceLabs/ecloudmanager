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
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.VMDeploymentRepository;
import org.ecloudmanager.service.deployment.ImportDeployableService;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class CommonVMDeploymentsController extends FacesSupport implements Serializable {
    private List<VMDeployment> vmDeployments;

    @Inject
    private transient VMDeploymentRepository vmDeploymentRepository;
    @Inject
    private transient ImportDeployableService importDeployableService;
    @Inject
    private transient TopLevelVMDeploymentController topLevelVMDeploymentController;
    @Inject
    private transient ImportDeployableController importDeployableController;

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        vmDeployments = vmDeploymentRepository.getAll();
    }

    public List<VMDeployment> getVmDeployments() {
        return vmDeployments;
    }

    public void startEdit(VMDeployment vmDeployment) {
        if (vmDeployment == null) {
            vmDeployment = new VMDeployment();
            vmDeployment.children().add(new Endpoint());
        }
        topLevelVMDeploymentController.setValue(vmDeployment);
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(TopLevelVMDeploymentController.DIALOG_EDIT);
        ctx.execute("PF('" + TopLevelVMDeploymentController.DIALOG_EDIT + "').show()");
    }

    public void delete(VMDeployment entity) {
        vmDeploymentRepository.delete(entity);
        refresh();
    }

    public void startImportVM() {
        importDeployableController.openDialog(
                ImmutableSet.of(VMDeployment.class),
                false,
                result -> {
                    if (result != null) {
                        Deployable deployable = (Deployable) result.getObject();
                        if (deployable != null) {
                            String name = StringUtils.isEmpty(result.getName()) ? deployable.getName() : result.getName();
                            Deployable newDeployable = importDeployableService.copyDeploymentObject(deployable, null, name, result.getIncludeConstraints());
                            vmDeploymentRepository.save((VMDeployment) newDeployable);
                            refresh();
                        }
                        RequestContext.getCurrentInstance().update("out");
                    }
                }
        );
    }

    public void save(VMDeployment vmDeployment) {
        vmDeploymentRepository.save(vmDeployment);
        refresh();
        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.execute("PF('" + TopLevelVMDeploymentController.DIALOG_EDIT + "').hide()");
        ctx.update("out");
    }
}

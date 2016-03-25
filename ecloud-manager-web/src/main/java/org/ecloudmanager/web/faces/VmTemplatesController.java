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

import com.rits.cloning.Cloner;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.repository.template.VirtualMachineTemplateRepository;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class VmTemplatesController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = 8603619026458002471L;

    @Inject
    private transient VirtualMachineTemplateRepository vmtemplateRepo;
    @Inject
    private transient VirtualMachineTemplateService vmtemplateService;
    @Inject
    private transient RecipeRepository recipeRepository;
    @Inject
    private transient VmTemplateController vmTemplateController;

    @Inject
    private transient Cloner cloner;

    private List<VirtualMachineTemplate> vmtemplates;

    private VirtualMachineTemplate oldValue;

    private boolean edit = false;

    @PostConstruct
    private void init() {
        refresh();
    }

    private void refresh() {
        vmtemplates = vmtemplateRepo.getAll();
    }


    public List<VirtualMachineTemplate> getTemplates() {
        return vmtemplates;
    }

    public void startEdit(VirtualMachineTemplate template) {
        edit = true;
        oldValue = template;
        vmTemplateController.setValue(cloner.deepClone(template));
    }

    public void handleClose() {
        reset();
    }

    private void reset() {
        edit = false;
        vmTemplateController.init();
    }

    public boolean isEdit() {
        return edit;
    }

    public void save() {
        vmtemplateService.updateVm(vmTemplateController.getValue());
        refresh();
        reset();
    }

    public void add() {
        vmtemplateService.saveVm(vmTemplateController.getValue());
        refresh();
        reset();

    }

    public void cancel() {
        reset();
    }

    public void delete(VirtualMachineTemplate entity) {
        vmtemplateService.removeVm(entity);
        refresh();
    }

}

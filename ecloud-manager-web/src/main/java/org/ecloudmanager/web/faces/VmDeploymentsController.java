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

import org.ecloudmanager.domain.verizon.deployment.VirtualMachine;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.VirtualMachineRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class VmDeploymentsController extends FacesSupport implements Serializable {

    private static final long serialVersionUID = -7165590568370435112L;

    private List<VirtualMachine> createdVms;

    private VirtualMachine selectedVm;

    @Inject
    private transient VirtualMachineRepository vmRepository;

    @PostConstruct
    private void init() {
        createdVms = vmRepository.getAll();
    }

    public List<VirtualMachine> getCreatedVms() {
        return createdVms;
    }

    public void setCreatedVms(List<VirtualMachine> createdVms) {
        this.createdVms = createdVms;
    }

    public VirtualMachine getSelectedVm() {
        return selectedVm;
    }

    public void setSelectedVm(VirtualMachine selectedVm) {
        this.selectedVm = selectedVm;
    }

}

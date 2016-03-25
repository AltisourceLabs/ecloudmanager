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

package org.ecloudmanager.deployment.ps.cg;

import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.util.ClonerProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

public class ComponentGroupTemplate implements Template<ComponentGroupDeployment> {
    private String name;
    private String description;
    private HAProxyBackendConfig haProxyBackendConfig = new HAProxyBackendConfig();

    @Reference(ignoreMissing = true)
    private VirtualMachineTemplate virtualMachineTemplate;

    public ComponentGroupTemplate() {
    }

    @Override
    public String getName() {
        return name;
    }


    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public HAProxyBackendConfig getHaProxyBackendConfig() {
        return haProxyBackendConfig;
    }

    public void setHaProxyBackendConfig(HAProxyBackendConfig haProxyBackendConfig) {
        this.haProxyBackendConfig = haProxyBackendConfig;
    }

    public VirtualMachineTemplate getVirtualMachineTemplate() {
        return virtualMachineTemplate;
    }

    public void setVirtualMachineTemplate(VirtualMachineTemplate virtualMachineTemplate) {
        this.virtualMachineTemplate = virtualMachineTemplate;
    }

    public String toString() {
        if (virtualMachineTemplate == null) {
            return "(VM not defined)";
        }
        return "[" + virtualMachineTemplate.toString() + "]";
    }

    @NotNull
    @Override
    public ComponentGroupDeployment toDeployment() {
        ComponentGroupDeployment cg = new ComponentGroupDeployment();
        cg.setName(getName());
        cg.setHaProxyBackendConfig(new HAProxyBackendConfig(getHaProxyBackendConfig()));
        if (getVirtualMachineTemplate() != null) {
            cg.setVirtualMachineTemplate(new ClonerProducer().produceCloner().deepClone(getVirtualMachineTemplate()));
        }
        cg.addVm();
        return cg;
    }

    @Override
    public List<EndpointTemplate> getEndpoints() {
        return null;
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return null;
    }
}

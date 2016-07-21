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

package org.ecloudmanager.deployment.vm;

import org.ecloudmanager.deployment.core.App;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.core.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

public class VMTemplateReference implements Template<VMDeployment>, App {

    private String name;
    private String description;
    @Reference(ignoreMissing = true)
    private VirtualMachineTemplate virtualMachineTemplate;

    public VMTemplateReference() {
    }

    public VirtualMachineTemplate getVirtualMachineTemplate() {
        return virtualMachineTemplate;
    }

    public void setVirtualMachineTemplate(VirtualMachineTemplate virtualMachineTemplate) {
        this.virtualMachineTemplate = virtualMachineTemplate;
        if (name == null || name.isEmpty()) {
            name = virtualMachineTemplate.getName();
        }
        if (description == null || description.isEmpty()) {
            description = virtualMachineTemplate.getDescription();
        }
    }

    @NotNull
    @Override
    public VMDeployment toDeployment() {
        VMDeployment deployment = virtualMachineTemplate.toDeployment();
        deployment.setName(name);
        return deployment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

    @NotNull
    @Override
    public List<Endpoint> getEndpoints() {
        return virtualMachineTemplate.getEndpoints();
    }

    @NotNull
    @Override
    public List<String> getRequiredEndpoints() {
        return virtualMachineTemplate.getRequiredEndpoints();
    }

    public String toString() {
        return name + " VM[" + virtualMachineTemplate + "]";
    }
}

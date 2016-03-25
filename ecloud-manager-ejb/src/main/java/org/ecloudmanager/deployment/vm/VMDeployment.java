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

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.vm.infrastructure.Infrastructure;
import org.ecloudmanager.deployment.vm.infrastructure.InfrastructureDeployer;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VMDeployment extends Deployable {
    private static final long serialVersionUID = 4504079285011312598L;

    private VirtualMachineTemplate virtualMachineTemplate;
    private Infrastructure infrastructure;

    VMDeployment() {
        setId(new ObjectId());
    }

    void setVirtualMachineTemplate(VirtualMachineTemplate virtualMachineTemplate) {
        this.virtualMachineTemplate = virtualMachineTemplate;
    }

    @Transient
    private VMDeployer deployer;

    @Override
    public VMDeployer getDeployer() {
        if (deployer == null) {
            InfrastructureDeployer infrastructureDeployer = getInfrastructure().getDeployer();
            deployer = new VMDeployer(infrastructureDeployer);
        }
        return deployer;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    public VirtualMachineTemplate getVirtualMachineTemplate() {
        return virtualMachineTemplate;
    }

    public Stream<Deployable> getRequired() {
        return Stream.concat(super.getRequired(), Stream.of(getChefEnvironment()));
    }

    public ChefEnvironment getChefEnvironment() {
        return ChefEnvironmentDeployer.getChefEnvironment(this);
    }

    public List<Recipe> getRunlist() {
        List<Recipe> runlist = new ArrayList<>(getInfrastructure().getRunlistHolder().getRunlist());
        runlist.addAll(getVirtualMachineTemplate().getRunlist());
        return runlist;
    }
}

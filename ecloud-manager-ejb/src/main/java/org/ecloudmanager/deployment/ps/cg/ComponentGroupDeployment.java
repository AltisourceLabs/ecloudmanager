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

import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Config;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.VirtualMachineTemplate;
import org.ecloudmanager.util.ClonerProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class ComponentGroupDeployment extends Deployable {

    public static final String VM_CONFIG = "vmConfig";
    private static final long serialVersionUID = 4963949678744410890L;
    private HAProxyBackendConfig haProxyBackendConfig = new HAProxyBackendConfig();
    @Transient
    private Deployer deployer;

    public ComponentGroupDeployment() {
        setId(new ObjectId());
    }

    public HAProxyBackendConfig getHaProxyBackendConfig() {
        return haProxyBackendConfig;
    }

    void setHaProxyBackendConfig(HAProxyBackendConfig haProxyBackendConfig) {
        this.haProxyBackendConfig = haProxyBackendConfig;
    }

    public VirtualMachineTemplate getVirtualMachineTemplate() {
        List<VirtualMachineTemplate> virtualMachineTemplates = children(VirtualMachineTemplate.class);
        if (virtualMachineTemplates.size() == 0) {
            addChild(new VirtualMachineTemplate());
        }

        return children(VirtualMachineTemplate.class).get(0);
    }

    public void setVirtualMachineTemplate(VirtualMachineTemplate virtualMachineTemplate) {
        children().remove(getVirtualMachineTemplate());
        addChild(virtualMachineTemplate);
    }

    @NotNull
    @Override
    public Deployer getDeployer() {
        if (deployer == null) {
            deployer = new ComponentGroupDeployer();
        }
        return deployer;
    }

    public String getInfrastructure() {
        return ((ApplicationDeployment)getTop()).getInfrastructure();
    }

    public void scale() {
        int newVmCount = Integer.valueOf(getConfigValue(ComponentGroupDeployer.VM_COUNT));
        int oldVmCount = (int) stream(VMDeployment.class).count();
        if (newVmCount > oldVmCount) {
            for (int i = oldVmCount; i < newVmCount; i++) {
                addVm();
            }
        } else if (newVmCount < oldVmCount) {
            for (int i = newVmCount; i < oldVmCount; i++) {
                deleteVm();
            }
        }
    }

    public void addVm() {
        VMDeployment vmDeployment = getVirtualMachineTemplate().toDeployment();
        vmDeployment.setName(generateNextVmName());
        addChild(vmDeployment);
    }

    private void deleteVm() {
        Optional<VMDeployment> last = stream(VMDeployment.class).reduce((first, second) -> second);
        if (last.isPresent()) {
            children().remove(last.get());
        }
    }

    private String generateNextVmName() {
        Set<String> usedNames = stream(VMDeployment.class).map(DeploymentObject::getName).collect(Collectors.toSet());
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String suffix = String.format("%03d", i);
            String candidate = getVirtualMachineTemplate().getName() + suffix;
            if (!usedNames.contains(candidate)) {
                return candidate;
            }
        }
        // Unable to generate a unique name, return something meaningful
        return getVirtualMachineTemplate().getName();
    }

    public void specifyConstraints() {
        super.specifyConstraints();
        DeploymentObject cfg = getChildByName(VM_CONFIG);
        DeploymentObject vmConfig;
        if (cfg == null) {
            vmConfig = new Config(VM_CONFIG, "Configuration for VMs in ComponentGroup");
            addChild(vmConfig);
        } else {
            vmConfig = cfg;
        }
        children(VMDeployment.class).forEach(child -> {
            child.copyConfig(vmConfig);
            child.setExtendedConfig(vmConfig);
            // TODO uncomment?
            // child.clear();
            child.children(Config.class).forEach(c -> {
                @Nullable DeploymentObject parentConfig = vmConfig.getChildByName(c.getName());
                if (parentConfig == null) {
                    parentConfig = new ClonerProducer().produceCloner().deepClone(c);
                    vmConfig.addChild(parentConfig);
                }
                c.setExtendedConfig(parentConfig);
                c.clear();
            });
        });
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return getVirtualMachineTemplate().getRequiredEndpoints();
    }
}

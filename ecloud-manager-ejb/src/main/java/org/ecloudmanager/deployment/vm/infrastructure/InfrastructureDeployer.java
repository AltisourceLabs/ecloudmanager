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

package org.ecloudmanager.deployment.vm.infrastructure;

import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.vm.VMDeployment;

public abstract class InfrastructureDeployer implements Deployer<VMDeployment> {
    private static final String VM_ID = "vmId";
    private static final String VM_IP_ADDRESS = "vmIpAddress";
    private static final String SSH_CONFIGURATION = "sshConfiguration";

    public static String getSshConfiguration(VMDeployment deployment) {
        return deployment.getConfigValue(SSH_CONFIGURATION);

    }

    public static void addSshConfiguration(VMDeployment deployment, String configuration) {
        deployment.addField(
            ConstraintField.builder()
                .name(SSH_CONFIGURATION)
                .description("SSH access configuration")
                .defaultValue(configuration)
                .allowReference(false)
                .build()
        );

    }

    public static String getVmId(VMDeployment deployment) {
        return deployment.getConfigValue(VM_ID);
    }

    public static void addVMId(VMDeployment deployment, String id) {
        deployment.addField(
            ConstraintField.builder()
                .name(VM_ID)
                .description("Virtual machine ID")
                .defaultValue(id)
                .readOnly(true)
                .allowReference(false)
                .build()
        );
    }

    public static void removeVMId(VMDeployment deployment) {
        deployment.removeField(VM_ID);
    }

    public static String getIP(VMDeployment deployment) {
        return deployment.getConfigValue(VM_IP_ADDRESS);

    }

    public static void addIP(VMDeployment deployment, String ip) {
        deployment.addField(
            ConstraintField.builder()
                .name(VM_IP_ADDRESS)
                .description("Virtual machine IP address")
                .defaultValue(ip)
                .readOnly(true)
                .allowReference(false)
                .build()
        );
    }

    public static void removeIP(VMDeployment deployment) {
        deployment.removeField(VM_IP_ADDRESS);
    }


    protected abstract DeploymentObject getInfrastructureConfig(VMDeployment deployment);
}

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

import org.ecloudmanager.domain.RunlistHolder;
import org.ecloudmanager.service.template.AWSConfigurationService;
import org.ecloudmanager.service.template.VerizonConfigurationService;

import javax.enterprise.inject.spi.CDI;

public enum Infrastructure {
    VERIZON(new VerizonInfrastructureDeployer(), new VerizonInfrastructureHAProxyDeployer()) {
        @Override
        public RunlistHolder getRunlistHolder() {
            return CDI.current().select(VerizonConfigurationService.class).get().getCurrentConfiguration();
        }
    },
    AWS(new AWSInfrastructureDeployer(), new AWSInfrastructureHAProxyDeployer()) {
        @Override
        public RunlistHolder getRunlistHolder() {
            return CDI.current().select(AWSConfigurationService.class).get().getCurrentConfiguration();
        }
    };

    private InfrastructureDeployer deployer;
    private InfrastructureHAProxyDeployer haProxyDeployer;

    Infrastructure(InfrastructureDeployer deployer, InfrastructureHAProxyDeployer haProxyDeployer) {
        this.deployer = deployer;
        this.haProxyDeployer = haProxyDeployer;
    }

    @Override
    public String toString() {
        return "Infrastructure[" + name() + "]";
    }

    public InfrastructureDeployer getDeployer() {
        return deployer;
    }

    public InfrastructureHAProxyDeployer getHaProxyDeployer() {
        return haProxyDeployer;
    }

    public abstract RunlistHolder getRunlistHolder();
}

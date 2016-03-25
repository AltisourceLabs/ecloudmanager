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

package org.ecloudmanager.deployment.ps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"parent"})
public class ProducedServiceDeployment extends Deployable {
    @JsonProperty("public")
    private boolean isPublic;

    private HAProxyFrontendConfig haProxyFrontendConfig = new HAProxyFrontendConfig();

    private List<FirewallRule> firewallRules = new ArrayList<>();

    ProducedServiceDeployment() {
    }

    public boolean isPublicHostname() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public HAProxyFrontendConfig getHaProxyFrontendConfig() {
        return haProxyFrontendConfig;
    }

    public void setHaProxyFrontendConfig(HAProxyFrontendConfig haProxyFrontendConfig) {
        this.haProxyFrontendConfig = haProxyFrontendConfig;
    }

    public List<ComponentGroupDeployment> getComponentGroups() {
        return children().stream()
            .filter(ComponentGroupDeployment.class::isInstance)
            .map(ComponentGroupDeployment.class::cast)
            .collect(Collectors.toList());
    }

    public List<FirewallRule> getFirewallRules() {
        return firewallRules;
    }

    public void setFirewallRules(List<FirewallRule> firewallRules) {
        this.firewallRules = firewallRules;
    }

    @Override
    public Deployer getDeployer() {
        return new HAProxyDeployer();
    }

}

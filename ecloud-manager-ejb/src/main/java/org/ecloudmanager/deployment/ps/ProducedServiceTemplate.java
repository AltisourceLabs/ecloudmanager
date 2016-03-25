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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProducedServiceTemplate implements Serializable, Template<ProducedServiceDeployment> {

    private static final long serialVersionUID = -659754447964919685L;

    private String name;
    private String description;

    @JsonProperty("public")
    private boolean publicHostname;

    private HAProxyFrontendConfig haProxyFrontendConfig = new HAProxyFrontendConfig();

    private List<ComponentGroupTemplate> componentGroups = new ArrayList<>();

    private List<FirewallRule> firewallRules = new ArrayList<>();

    public ProducedServiceTemplate() {
    }

    public ProducedServiceTemplate(String name) {
        this.name = name;
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
    public List<EndpointTemplate> getEndpoints() {
        return null;
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return null;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicHostname() {
        return publicHostname;
    }

    public void setPublicHostname(boolean publicHostname) {
        this.publicHostname = publicHostname;
    }

    public HAProxyFrontendConfig getHaProxyFrontendConfig() {
        return haProxyFrontendConfig;
    }

    public void setHaProxyFrontendConfig(HAProxyFrontendConfig haProxyFrontendConfig) {
        this.haProxyFrontendConfig = haProxyFrontendConfig;
    }

    @NotNull
    public List<ComponentGroupTemplate> getComponentGroups() {
        return componentGroups;
    }

    public void setComponentGroups(@NotNull List<ComponentGroupTemplate> componentGroups) {
        this.componentGroups = componentGroups;
    }

    public String toString() {
        return "PS[" + name + "] : " + getComponentGroups().toString();
    }

    public List<FirewallRule> getFirewallRules() {
        return firewallRules;
    }

    public void setFirewallRules(List<FirewallRule> firewallRules) {
        this.firewallRules = firewallRules;
    }

    @NotNull
    @Override
    public ProducedServiceDeployment toDeployment() {
        ProducedServiceDeployment ps = new ProducedServiceDeployment();
        ps.setName(getName());
        ps.setIsPublic(isPublicHostname());
        ps.setHaProxyFrontendConfig(new HAProxyFrontendConfig(getHaProxyFrontendConfig()));
        getComponentGroups().forEach(cg -> ps.addChild(cg.toDeployment()));
        getFirewallRules().forEach(r -> ps.getFirewallRules().add(new FirewallRule(r)));
        return ps;
    }
}

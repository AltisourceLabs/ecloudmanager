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

import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProducedServiceTemplate implements Serializable, Template<ProducedServiceDeployment> {

    private static final long serialVersionUID = -659754447964919685L;

    private String name;
    private String description;

    private HAProxyFrontendConfig haProxyFrontendConfig = new HAProxyFrontendConfig();

    private List<ComponentGroupTemplate> componentGroups = new ArrayList<>();
    private EndpointTemplate endpoint = new EndpointTemplate();

    public ProducedServiceTemplate() {
    }

    public ProducedServiceTemplate(String name) {
        this.name = name;
    }

    public EndpointTemplate getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointTemplate endpoint) {
        this.endpoint = endpoint;
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
    public List<EndpointTemplate> getEndpoints() {
        return Collections.singletonList(endpoint);
    }

    @NotNull
    @Override
    public List<String> getRequiredEndpoints() {
        return componentGroups.stream().flatMap(t -> t.getRequiredEndpointsIncludingTemplateName().stream()).collect(Collectors.toList());
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

    @NotNull
    @Override
    public ProducedServiceDeployment toDeployment() {
        ProducedServiceDeployment ps = new ProducedServiceDeployment();
        ps.setName(getName());
        ps.setHaProxyFrontendConfig(new HAProxyFrontendConfig(getHaProxyFrontendConfig()));
        getComponentGroups().forEach(cg -> ps.addChild(cg.toDeployment()));
        return ps;
    }
}

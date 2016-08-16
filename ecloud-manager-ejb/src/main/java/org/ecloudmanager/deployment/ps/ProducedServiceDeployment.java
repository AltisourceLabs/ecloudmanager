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
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"parent"})
public class ProducedServiceDeployment extends Deployable {

    private HAProxyFrontendConfig haProxyFrontendConfig = new HAProxyFrontendConfig();

    public ProducedServiceDeployment() {
        setId(new ObjectId());
    }

    public Endpoint getEndpoint() {
        return children(Endpoint.class).get(0);
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return Collections.singletonList(getEndpoint());
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return children(ComponentGroupDeployment.class).stream().flatMap(t -> t.getRequiredEndpointsIncludingTemplateName().stream()).collect(Collectors.toList());
    }

    public HAProxyFrontendConfig getHaProxyFrontendConfig() {
        return haProxyFrontendConfig;
    }

    public void setHaProxyFrontendConfig(HAProxyFrontendConfig haProxyFrontendConfig) {
        this.haProxyFrontendConfig = haProxyFrontendConfig;
    }

    public List<ComponentGroupDeployment> getComponentGroups() {
        return children(ComponentGroupDeployment.class);
    }

    @NotNull
    @Override
    public Deployer getDeployer() {
        return new HAProxyDeployer();
    }

}

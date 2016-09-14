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

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.app.Link;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironmentDeployer;
import org.ecloudmanager.deployment.vm.provisioning.Recipe;
import org.ecloudmanager.service.NodeAPIConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.enterprise.inject.spi.CDI;
import java.util.*;
import java.util.stream.Stream;

@Entity
public class VMDeployment extends Deployable {
    private static final long serialVersionUID = 4504079285011312598L;

    @Transient
    private VMDeployer deployer;

    public VMDeployment() {
        setId(new ObjectId());
    }

    @NotNull
    @Override
    public VMDeployer getDeployer() {
        if (deployer == null || !deployer.getApiName().equals(getInfrastructure())) {
            deployer = new VMDeployer(getInfrastructure());
        }
        return deployer;
    }

    @NotNull
    public String getInfrastructure() {
        return ((ApplicationDeployment)getTop()).getInfrastructure();
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

    public Stream<Deployable> getRequired() {
        ApplicationDeployment ad = (ApplicationDeployment) getTop();
        List<Link> links = ad.getLinks();
        List<Deployable> required = new ArrayList<>();
        getVirtualMachineTemplate().getRequiredEndpointsIncludingTemplateName().forEach(r -> {
            Optional<Link> o = links.stream().filter(l -> l.getConsumer().equals(r)).findFirst();
            if (o.isPresent()) {
                String endpoint = o.get().getSupplier();
                String name = endpoint.split(":")[0];
                required.add((Deployable) ad.getChildByName(name));
            }
        });
        return Stream.concat(required.stream(), Stream.concat(super.getRequired(), Stream.of(getChefEnvironment())));
    }

    public ChefEnvironment getChefEnvironment() {
        return ChefEnvironmentDeployer.getChefEnvironment(this);
    }

    public List<Recipe> getRunlist() {
        List<Recipe> runlist = new ArrayList<>();
        runlist.addAll(CDI.current().select(NodeAPIConfigurationService.class).get().getRunlist(getInfrastructure()));
        runlist.addAll(getVirtualMachineTemplate().getRunlist());
        return runlist;
    }

    @Override
    protected Collection<String> getExcludeFieldNames() {
        return Arrays.asList("deployer", "parent", "children", "fields", "values");
    }

    public List<Pair<DeploymentObject, Endpoint>> getLinkedRequiredEndpoints() {
        List<Pair<DeploymentObject, Endpoint>> result = new ArrayList<>();
        ApplicationDeployment ad = (ApplicationDeployment) getTop();
        List<Link> links = ad.getLinks();
        getVirtualMachineTemplate().getRequiredEndpoints().forEach(r -> {
            String path;
            if (getParent() == getTop()) { //FIXME find better way to handle different paths in ComponentGroup
                path = getPath(":");
            } else {
                path = getParent().getPath(":");
            }
            String epPath = path + ":" + r;
            Optional<Link> o = links.stream().filter(l -> l.getConsumer().equals(epPath)).findFirst();
            if (o.isPresent()) {
                String endpoint = o.get().getSupplier();
                String[] splitted = endpoint.split(":");
                String name = splitted[0];
                Deployable d = (Deployable) ad.getChildByName(name);
                String endpointName = splitted[splitted.length - 1];
                Optional<Endpoint> e = d.getEndpoints().stream().filter(ep -> ep.getName().equals(endpointName)).findFirst();
                if (e.isPresent()) {
                    result.add(Pair.of(d, e.get()));
                }
            }
        });
        return result;
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return getVirtualMachineTemplate().getEndpoints();
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return getVirtualMachineTemplate().getRequiredEndpoints();
    }
}

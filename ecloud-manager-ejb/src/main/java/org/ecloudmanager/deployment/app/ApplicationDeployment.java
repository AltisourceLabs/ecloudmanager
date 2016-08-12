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

package org.ecloudmanager.deployment.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.Deployer;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"id", "version", "new"})
@Entity("deployments")
public class ApplicationDeployment extends Deployable {
    private static final long serialVersionUID = -8557535271917698832L;
    private List<Link> links = new ArrayList<>();
    private List<String> publicEndpoints = new ArrayList<>();
    private String infrastructure;

    public ApplicationDeployment() {
    }

    @NotNull
    @Override
    public Deployer<ApplicationDeployment> getDeployer() {
        return new ApplicationDeployer();
    }

    public String getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(String infrastructure) {
        this.infrastructure = infrastructure;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }


    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    private Link addLink(String consumer) {
        Optional<Link> existing = links.stream().filter(l -> (l.getConsumer().equals(consumer))).findAny();
        if (existing.isPresent()) {
            return existing.get();
        }
        Link newLink = new Link();
        newLink.setConsumer(consumer);
        links.add(newLink);
        return newLink;
    }

    private void deleteLink(String consumer) {
        List<Link> linksToRemove = links.stream().filter(l -> (l.getConsumer().equals(consumer))).collect(Collectors.toList());
        linksToRemove.forEach(l -> links.remove(l));
    }

    public void updateLinks() {
        List<String> endpoints = new ArrayList<>();
        children().forEach(c -> {
            if (c instanceof Deployable) {
                endpoints.addAll(((Deployable) c).getRequiredEndpointsIncludingTemplateName());
            }
        });
        List<Link> newLinks = endpoints.stream().map(this::addLink).collect(Collectors.toList());
        links.clear();
        links.addAll(newLinks);
    }

    @Override
    public void addChild(DeploymentObject child) {
        super.addChild(child);
        if (child instanceof Deployable) {
            List<String> endpoints = ((Deployable)child).getRequiredEndpointsIncludingTemplateName();
            endpoints.forEach(this::addLink);
        }
    }

    public boolean removeChild(DeploymentObject child) {
        if (children().remove(child) && child instanceof Deployable) {
            Deployable deployable = (Deployable) child;
            List<String> oldEndpoints = deployable.getRequiredEndpointsIncludingTemplateName();
            oldEndpoints.forEach(this::deleteLink);
            deployable.getEndpointsIncludingTemplateName().forEach(publicEndpoints::remove);
            return true;
        }
        return false;
    }

}

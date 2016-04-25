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
import com.google.common.collect.ImmutableList;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.jeecore.domain.MongoObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties({"id", "version", "new"})
public class ApplicationTemplate extends MongoObject implements Template<ApplicationDeployment>, Serializable {

    private static final long serialVersionUID = -452900719263209167L;

    private String name;
    private String description;
    private List<Link> links = new ArrayList<>();
    private List<Template> children = new ArrayList<>();
    private List<String> publicEndpoints = new ArrayList<>();

    public ApplicationTemplate() {
    }

    public ApplicationTemplate(String name) {
        this.name = name;
    }

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Template> getChildren() {
        return ImmutableList.copyOf(children);
    }

    public String getName() {
        return name;
    }

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

    public void addChild(Template template) {
        children.add(template);
        List<String> endpoints = template.getRequiredEndpointsIncludingTemplateName();
        endpoints.forEach(this::addLink);
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
        links.stream().filter(l -> (l.getConsumer().equals(consumer))).forEach(l -> links.remove(l));
    }

    public Template setChild(int index, Template child) {
        Template old = children.set(index, child);
        List<String> newEndpoints = child.getRequiredEndpointsIncludingTemplateName();
        List<String> oldEndpoints = old.getRequiredEndpointsIncludingTemplateName();
        List<String> toDelete = new ArrayList<>(oldEndpoints);
        toDelete.removeAll(newEndpoints);
        List<String> toAdd = new ArrayList<>(newEndpoints);
        toAdd.removeAll(oldEndpoints);
        toDelete.forEach(this::deleteLink);
        toAdd.forEach(this::addLink);
        return old;
    }

    public boolean removeChild(Template child) {
        if (children.remove(child)) {
            List<String> oldEndpoints = child.getRequiredEndpointsIncludingTemplateName();
            oldEndpoints.forEach(this::deleteLink);
            child.getEndpointsIncludingTemplateName().forEach(publicEndpoints::remove);
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public ApplicationDeployment toDeployment() {
        ApplicationDeployment ad = new ApplicationDeployment();
        ad.setName(getName() + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
        ad.setDescription(getName() + " description");
        children.forEach(s -> {
            Deployable deployable = s.toDeployment();
            s.getEndpoints().forEach(e -> {
                deployable.addChild(e.toDeployment());
            });
            ad.addChild(deployable);
        });
        ad.getLinks().addAll(links);
        ad.getPublicEndpoints().addAll(publicEndpoints);
        return ad;
    }

    public <T extends Template> List<T> getChildrenOfType(Class<T> type) {
        return getChildren().stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public List<EndpointTemplate> getEndpoints() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<String> getRequiredEndpoints() {
        return Collections.emptyList();
    }

    @PostLoad
    private void postLoad() {
        children.forEach(t -> {
            List<String> endpoints = t.getRequiredEndpointsIncludingTemplateName();
            endpoints.forEach(this::addLink);
        });
    }
}

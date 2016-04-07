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

package org.ecloudmanager.deployment.vm.provisioning;

import org.ecloudmanager.deployment.core.App;
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.jeecore.domain.DomainObject;
import org.ecloudmanager.jeecore.domain.Persistable;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.Transient;

import java.util.*;

public class Recipe extends DomainObject implements App, Persistable<String> {
    private static final long serialVersionUID = -3878655521051524483L;
    List<ChefAttribute> attributes = new ArrayList<>();
    @Id
    private String id;
    private String description;
    private String version = "= 0.1.0";
    @Transient
    private String oldId = null;
    private List<EndpointTemplate> endpoints = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(String id) {
        this.id = id;
    }

    public static void createRecipes() {

    }

    public String getOldId() {
        return oldId;
    }

    public List<ChefAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ChefAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addEndpoint(EndpointTemplate endpoint) {
        endpoints.add(endpoint);
    }

    public void addChefAttribute(ChefAttribute attribute) {
        attributes.add(attribute);
    }

    public void addEnvironmentOverrideAttribute(String id, String value) {
        attributes.add(new ChefAttribute(id, value));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return oldId == null;
    }

    public List<EndpointTemplate> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointTemplate> endpoints) {
        this.endpoints = endpoints;
    }

    public String getCookbookName() {
        int pos = id.indexOf("::");

        return pos == -1 ? id : id.substring(0, pos);
    }

    @Override
    public List<String> getRequiredEndpoints() {
        List<String> constraints = getConstraintNames();
        Set<String> result = new TreeSet<>();
        for (String c : constraints) {
            int index = c.indexOf(":");
            if (index > 0) {
                result.add(c.substring(0, index));
            }
        }
        return new ArrayList<>(result);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected Collection<String> getExcludeFieldNames() {
        return Arrays.asList("attributes", "endpoints", "oldId");
    }

    @PostLoad
    @PostPersist
    void saveOldId() {
        oldId = id;
    }

    public String toString() {
        return id;
    }

    public List<String> getConstraintNames() {
        Set<String> result = new HashSet<>();
        for (ChefAttribute a : attributes) {
            result.addAll(a.getConstraintNames());
        }
        return new ArrayList<>(result);
    }

}

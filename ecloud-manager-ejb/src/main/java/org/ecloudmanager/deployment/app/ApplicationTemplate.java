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
import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.ecloudmanager.jeecore.domain.MongoObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mongodb.morphia.annotations.Entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties({"id", "version", "new"})
public class ApplicationTemplate extends MongoObject implements Template<ApplicationDeployment>, Serializable {

    private static final long serialVersionUID = -452900719263209167L;

    private String name;
    private String description;
    private List<Link> links = new ArrayList<>();
    private List<Template> children = new ArrayList<>();

    public List<Template> getChildren() {
        return children;
    }

    public ApplicationTemplate() {
    }

    public ApplicationTemplate(String name) {
        this.name = name;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void addChild(Template template) {
        children.add(template);
    }

    @NotNull
    @Override
    public ApplicationDeployment toDeployment() {
        ApplicationDeployment ad = new ApplicationDeployment();
        ad.setName(getName() + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
        ad.setDescription(getName() + " description");
        children.forEach(s -> ad.addChild(s.toDeployment()));
        return ad;
    }

    public <T extends Template> List<T> getChildrenOfType(Class<T> type) {
        return getChildren().stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }

    @Override
    public List<EndpointTemplate> getEndpoints() {
        return null;
    }

    @Override
    public List<String> getRequiredEndpoints() {
        return null;
    }


}

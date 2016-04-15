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

package org.ecloudmanager.deployment.es;

import org.ecloudmanager.deployment.core.EndpointTemplate;
import org.ecloudmanager.deployment.core.Template;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ExternalServiceTemplate implements Template<ExternalServiceDeployment> {
    private String name;

    private String description;

    public ExternalServiceTemplate() {
    }

    public ExternalServiceTemplate(String name) {
        this.name = name;
    }

    public ExternalServiceTemplate(ExternalServiceTemplate es) {
        name = es.getName();
        description = es.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String address) {
        this.description = address;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name + " : " + description;
    }

    @NotNull
    @Override
    public ExternalServiceDeployment toDeployment() {
        ExternalServiceDeployment es = new ExternalServiceDeployment();
        es.setName(getName());
        es.setDescription(getDescription());
        return es;
    }
}

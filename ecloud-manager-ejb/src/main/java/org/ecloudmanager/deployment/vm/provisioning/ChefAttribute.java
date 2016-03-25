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

import java.util.List;

public class ChefAttribute {
    private String name;
    private String value;
    private boolean nodeAttribute = true;
    private boolean environmentAttribute = true;
    private boolean environmentDefaultAttribute = false;

    public ChefAttribute() {
    }

    public ChefAttribute(ChefAttribute attribute) {
        name = attribute.name;
        value = attribute.value;
        nodeAttribute = attribute.nodeAttribute;
        environmentAttribute = attribute.environmentAttribute;
        environmentDefaultAttribute = attribute.environmentDefaultAttribute;
    }

    public ChefAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNodeAttribute() {
        return nodeAttribute;
    }

    public void setNodeAttribute(boolean nodeAttribute) {
        this.nodeAttribute = nodeAttribute;
    }

    public boolean isEnvironmentAttribute() {
        return environmentAttribute;
    }

    public void setEnvironmentAttribute(boolean environmentAttribute) {
        this.environmentAttribute = environmentAttribute;
    }

    public boolean isEnvironmentDefaultAttribute() {
        return environmentDefaultAttribute;
    }

    public void setEnvironmentDefaultAttribute(boolean environmentDefaultAttribute) {
        this.environmentDefaultAttribute = environmentDefaultAttribute;
    }

    public List<String> getConstraintNames() {
        return ChefEnvironmentDeployer.getConstraintNames(getValue());
    }
}
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

package org.ecloudmanager.deployment.core;

public class EndpointTemplate {
    private String name;
    private String description;
    private Integer port;
    private boolean constant;

    public EndpointTemplate() {
    }

    public EndpointTemplate(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return port == null ? name : name + ":" + port;
    }

    public Endpoint toDeployment() {
        Endpoint endpoint = new Endpoint();
        endpoint.setName(getName());
        endpoint.setDescription(getDescription());
        ConstraintField portField = new ConstraintField();
        portField.setName("port");
        portField.setRequired(true);
        endpoint.addField(portField);
        if (!isConstant()) {
            if (getPort() != null) {
                portField.setDefaultValue(getPort().toString());
            }
        } else {
            portField.setReadOnly(true);
            endpoint.setValue(portField.getName(), ConstraintValue.value(getPort().toString()));
        }

        ConstraintField hostField = new ConstraintField();
        hostField.setName("host");

//        hostField.setRequired(true);
        endpoint.addField(hostField);
        return endpoint;
    }
}

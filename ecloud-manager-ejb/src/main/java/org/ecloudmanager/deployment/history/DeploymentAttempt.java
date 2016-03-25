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

package org.ecloudmanager.deployment.history;

import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.jeecore.domain.MongoObject;
import org.ecloudmanager.service.execution.Action;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;

public class DeploymentAttempt extends MongoObject {
    private static final long serialVersionUID = -3745386506846125067L;

    public enum Type {CREATE, UPDATE, DELETE}

    @Embedded
    private Deployable deployment;
    private Action action;
    private Date timestamp;
    private Type type;

    private DeploymentAttempt() {
    }

    public DeploymentAttempt(Deployable deployment, Action action, Type type) {
        this.deployment = deployment;
        this.action = action;
        this.type = type;
        timestamp = new Date();
    }

    public Deployable getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployable deployment) {
        this.deployment = deployment;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

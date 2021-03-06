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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class Deployable extends DeploymentObject {
    protected Deployable() {
    }

    @NotNull
    public abstract Deployer getDeployer();

    public void specifyConstraints() {
        getDeployer().specifyConstraints(this);
        children(Deployable.class).forEach(Deployable::specifyConstraints);
    }

    public Stream<Deployable> getRequired() {
        return children(Deployable.class).stream()
            .flatMap(Deployable::getRequired)
            .filter(d -> (!d.equals(this) && !children().contains(d))).distinct();
    }

    public List<Endpoint> getEndpoints() {
        return Collections.emptyList();
    }

    public List<String> getRequiredEndpoints() {
        return Collections.emptyList();
    }

    public List<String> getRequiredEndpointsIncludingTemplateName() {
        List<String> result = new ArrayList<>();
        getRequiredEndpoints().forEach(e -> result.add(getName() + ":" + e));
        return result;
    }

    public List<String> getEndpointsIncludingTemplateName() {
        List<String> result = new ArrayList<>();
        getEndpoints().forEach(e -> result.add(getName() + ":" + e.getName()));
        return result;
    }
}

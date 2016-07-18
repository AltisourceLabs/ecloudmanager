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

import org.ecloudmanager.deployment.core.AbstractDeployer;
import org.ecloudmanager.deployment.core.ConstraintField;
import org.ecloudmanager.deployment.core.ConstraintValue;
import org.ecloudmanager.deployment.core.Endpoint;
import org.ecloudmanager.service.execution.Action;

public class ApplicationDeployer extends AbstractDeployer<ApplicationDeployment> {
    private static String PORT = "port";

    @Override
    public Action getBeforeChildrenCreatedAction(ApplicationDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenCreatedAction(ApplicationDeployment deployable) {
        return null;
    }

    @Override
    public Action getAfterChildrenDeletedAction(ApplicationDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenDeletedAction(ApplicationDeployment deployable) {
        return null;
    }

    @Override
    public Action getBeforeChildrenUpdatedAction(ApplicationDeployment before, ApplicationDeployment after) {
        return null;
    }

    @Override
    public Action getAfterChildrenUpdatedAction(ApplicationDeployment before, ApplicationDeployment after) {
        return null;
    }

    @Override
    public void specifyConstraints(ApplicationDeployment deployment) {
        deployment.stream(Endpoint.class).forEach(endpoint -> {
            ConstraintField.Builder builder = ConstraintField.builder().name(PORT).required(true);
            if (endpoint.isConstant()) {
                builder.readOnly(true);
                endpoint.setValue(PORT, ConstraintValue.value(endpoint.getPort().toString()));
            } else {
                if (endpoint.getPort() != null) {
                    builder.defaultValue(endpoint.getPort().toString());
                }
            }
            endpoint.addField(builder.build());
        });
    }

}

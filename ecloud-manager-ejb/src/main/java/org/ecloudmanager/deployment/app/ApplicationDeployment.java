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
import org.mongodb.morphia.annotations.Entity;

@JsonIgnoreProperties({"id", "version", "new"})
@Entity("deployments")
public class ApplicationDeployment extends Deployable {
    private static final long serialVersionUID = -8557535271917698832L;

    ApplicationDeployment() {
    }

    @Override
    public Deployer<ApplicationDeployment> getDeployer() {
        return new ApplicationDeployer();
    }

}

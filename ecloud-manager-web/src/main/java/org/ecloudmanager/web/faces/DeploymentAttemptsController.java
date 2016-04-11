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

package org.ecloudmanager.web.faces;

import org.apache.commons.lang3.StringUtils;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.jeecore.web.faces.Controller;
import org.ecloudmanager.jeecore.web.faces.FacesSupport;
import org.ecloudmanager.repository.deployment.DeploymentAttemptRepository;
import org.omnifaces.cdi.Param;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Controller
public class DeploymentAttemptsController extends FacesSupport implements Serializable {
    private static final long serialVersionUID = -1666963273498261957L;

    @Inject
    private transient DeploymentAttemptRepository deploymentAttemptRepository;
    @Inject
    private transient IdentityManager identityManager;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Param(converter = "applicationDeploymentConverter")
    private ApplicationDeployment deployment;

    private List<DeploymentAttempt> deploymentAttempts;

    @PostConstruct
    private void init() {
        refresh();
    }

    void refresh() {
        deploymentAttempts = deploymentAttemptRepository.findAttempts(deployment);
    }

    public List<DeploymentAttempt> getDeploymentAttempts() {
        return deploymentAttempts;
    }

    public ApplicationDeployment getDeployment() {
        return deployment;
    }

    public void delete(DeploymentAttempt entity) {
        deploymentAttemptRepository.delete(entity);
        refresh();
    }

    public String getUsernameFromAttempt(DeploymentAttempt deploymentAttempt) {
        String username = "unknown user";
        if (!StringUtils.isEmpty(deploymentAttempt.getOwner())) {
            User user = identityManager.lookupById(User.class, deploymentAttempt.getOwner());
            username = user.getLoginName();
        }
        return username;
    }
}


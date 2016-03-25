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

package org.ecloudmanager.service.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.app.ApplicationTemplate;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.deployment.ps.cg.ComponentGroupDeployment;
import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.infrastructure.Infrastructure;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.repository.deployment.DeploymentAttemptRepository;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.ActionExecutor;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ApplicationDeploymentService extends ServiceSupport {

    @Inject
    private Logger log;

    @Inject
    private DeploymentAttemptRepository deploymentAttemptRepository;

    public ApplicationDeployment create(ApplicationTemplate app, String infrastructure) {
        log.info("Creating deployment for application " + app.getName() + ", " + infrastructure);
        Infrastructure infra = Infrastructure.valueOf(infrastructure);
        ApplicationDeployment ad = app.toDeployment();

        ad.stream(ComponentGroupDeployment.class).forEach(x -> x.setInfrastructure(infra));
        ad.stream(VMDeployment.class).forEach(x -> x.setInfrastructure(infra));
        ad.specifyConstraints();
        return ad;
    }

    public void save(ApplicationDeployment ad) {
        log.info("Saving " + ad.getName());
        datastore.save(ad);
        fireEntityCreated(ad);
    }

    public void update(ApplicationDeployment ad) {
        log.info("Updating " + ad.getName());
        datastore.save(ad);
        fireEntityUpdated(ad);
    }

    public void remove(ApplicationDeployment ad) {
        log.info("Deleting " + ad.getName());
        datastore.delete(ad);
        fireEntityDeleted(ad);
    }

    public String toYaml(DeploymentObject app) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String str = mapper.writeValueAsString(app);
        return str;
    }

    public void execute(final Deployable deployment, Action action, DeploymentAttempt.Type actionType) {
        log.info("Starting deployment action (" + actionType + ") for " + deployment.getName());
        log.info("Submitting action:" + action.toString());

        ActionExecutor executor = new ActionExecutor();
        try {
            executor.execute(action, () -> {
                // Need to reload deployment from the DB because the changes made by actions are stored there
                // but not reflected in the object instance referenced by the 'deployment' var
                Deployable reloadedDeployment = datastore.get(deployment);
                deploymentAttemptRepository.save(new DeploymentAttempt(reloadedDeployment, action, actionType));
                executor.shutdown();
            });
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Deployment interrupted: ", e);
        } catch (Throwable e) {
            log.error("Error during application deployment (" + actionType + "): " + e.getMessage());
        }
    }
}
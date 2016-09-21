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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.deployment.app.ApplicationDeployment;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.jeecore.service.ServiceSupport;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.repository.deployment.ApplicationDeploymentRepository;
import org.ecloudmanager.repository.deployment.DeploymentAttemptRepository;
import org.ecloudmanager.repository.template.RecipeRepository;
import org.ecloudmanager.service.execution.Action;
import org.ecloudmanager.service.execution.ActionCompletionCallback;
import org.ecloudmanager.service.execution.ActionException;
import org.ecloudmanager.service.execution.ActionExecutor;
import org.ecloudmanager.service.template.VirtualMachineTemplateService;
import org.mongodb.morphia.Morphia;
import org.picketlink.Identity;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

@Stateless
public class ApplicationDeploymentService extends ServiceSupport {
    @Inject
    ActionExecutor actionExecutor;
    @Inject
    Identity identity;

    @Inject
    private Logger log;

    @Inject
    private DeploymentAttemptRepository deploymentAttemptRepository;
    @Inject
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    @Inject
    private RecipeRepository recipeRepository;
    @Inject
    private VirtualMachineTemplateService virtualMachineTemplateService;

    @Inject
    private Morphia morphia;

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

    public String toFormattedJson(DeploymentObject app) throws JsonProcessingException {
        String notFormatted = morphia.toDBObject(app).toString();
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement el = parser.parse(notFormatted);
        return gson.toJson(el);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void execute(final DeploymentAttempt attempt, ActionCompletionCallback onComplete, LoggingEventListener... listeners) {
        log.info("Starting deployment action (" + attempt.getType() + ") for " + attempt.getDeployment().getName());
        log.info("Submitting action:" + attempt.toString());

        try {
            actionExecutor.execute(attempt.getAction(), (Exception e) -> {
                // Need to reload deployment from the DB because the changes made by actions are stored there
                // but not reflected in the object instance referenced by the 'deployment' var
                Deployable reloadedDeployment = datastore.get(attempt.getDeployment());

                DeploymentAttempt deploymentAttempt = new DeploymentAttempt(reloadedDeployment, attempt.getAction(), attempt.getType());
                deploymentAttempt.setOwner(identity.getAccount().getId());
                deploymentAttemptRepository.save(deploymentAttempt);
                actionExecutor.shutdown();
                if (onComplete != null) {
                    switch (attempt.getAction().getStatus()) {
                        case SUCCESSFUL:
                            onComplete.onComplete(null);
                            break;
                        case PENDING:
                        case RUNNING:
                        case FAILED:
                        case NOT_RUN:
                            onComplete.onComplete(new ActionException("Action finished with status " + attempt.getAction().getStatus().name()));
                            break;
                    }
                }
            }, listeners);
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Deployment interrupted: ", e);
            onComplete.onComplete(e);
        } catch (Exception e) {
            log.error("Error during application deployment (" + attempt.getType() + "): " + e.getMessage());
            onComplete.onComplete(e);
        }
    }

    public DeploymentAttempt getDeployAction(ApplicationDeployment deployment) {
        DeploymentAttempt lastAttempt = deploymentAttemptRepository.findLastAttempt(deployment);
        return lastAttempt != null && lastAttempt.getType() != DeploymentAttempt.Type.DELETE ?
                new DeploymentAttempt(deployment, deployment.getDeployer().getUpdateAction(lastAttempt, (ApplicationDeployment) lastAttempt.getDeployment(), deployment), DeploymentAttempt.Type.UPDATE) :
                new DeploymentAttempt(deployment, deployment.getDeployer().getCreateAction(deployment), DeploymentAttempt.Type.CREATE);
    }

    public boolean isDeployed(ApplicationDeployment applicationDeployment) {
        DeploymentAttempt lastAttempt = deploymentAttemptRepository.findLastAttempt(applicationDeployment);
        if (
                lastAttempt != null &&
                lastAttempt.getType() != DeploymentAttempt.Type.DELETE &&
                lastAttempt.getAction().getStatus() == Action.Status.SUCCESSFUL
        ) {
            return true;
        }

        return false;
    }
}
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

package org.ecloudmanager.actions;

import org.ecloudmanager.deployment.vm.VMDeployment;
import org.ecloudmanager.deployment.vm.provisioning.ChefEnvironment;
import org.ecloudmanager.jeecore.service.Service;
import org.ecloudmanager.repository.deployment.LoggingEventRepository;
import org.ecloudmanager.service.chef.ChefService;
import org.ecloudmanager.service.execution.Action;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;

import static org.ecloudmanager.node.LoggableFuture.submitAndWait;

@Service
public class ChefActions {
    public static String CREATE_ENVIRONMENT_ACTION = "Create Chef Environment";
    @Inject
    @Named("contextExecutorService")
    ExecutorService executorService;
    @Inject
    private ChefService chefService;
    @Inject
    private LoggingEventRepository loggingEventRepository;

    public Action getCreateChefEnvironmentAction(ChefEnvironment chefEnvironment) {
        String actionId = Action.newId();
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(ChefActions.class, actionId);
        return Action.single(CREATE_ENVIRONMENT_ACTION,
                () -> {
                    submitAndWait(() -> {
                        chefService.createEnvironment(chefEnvironment);
                        return null;
                    }, executorService, actionLog);
                    return null;
                },
                chefEnvironment, actionId);
    }

    public Action getDeleteChefEnvironmentAction(ChefEnvironment chefEnvironment) {
        String actionId = Action.newId();
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(ChefActions.class, actionId);
        return Action.single("Delete Chef Environment",
                () -> {
                    submitAndWait(() -> {
                        chefService.deleteEnvironment(chefEnvironment);
                        return null;
                    }, executorService, actionLog);
                    return null;
                },
                chefEnvironment, actionId);
    }

    public Action getDeleteChefNodeAndClientAction(VMDeployment vmDeployment) {
        String actionId = Action.newId();
        LoggingEventRepository.ActionLogger actionLog = loggingEventRepository.createActionLogger(ChefActions.class, actionId);
        return Action.single("Delete Chef Node and Client",
                () -> {
                    submitAndWait(() -> {
                    chefService.deleteNodeAndClient(vmDeployment);
                    return null;
                    }, executorService, actionLog);
                    return null;
                },
                vmDeployment, actionId);
    }

    public boolean needUpdateChefEnvironment(ChefEnvironment deployment) {
        return chefService.needUpdateChefEnvironment(deployment);
    }
}

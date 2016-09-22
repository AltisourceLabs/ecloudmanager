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

package org.ecloudmanager.service.execution;

import org.apache.logging.log4j.Logger;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.repository.deployment.ActionLogger;
import org.ecloudmanager.repository.deployment.LoggingEventRepository;
import org.jetbrains.annotations.Nullable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
@Stateless
public class ActionExecutor {
    @Inject
    Logger log;

    @Inject
    @Named("contextExecutorService")
    ExecutorService executorService;

    @Inject
    LoggingEventRepository loggingEventRepository;

//    @Resource
//    ManagedExecutorService executorService;
//    @Resource
//    ContextService cs;

    public void execute(Action action, @Nullable ActionCompletionCallback onComplete, LoggingEventListener... listeners) throws InterruptedException {
        executorService.submit(() -> {
            while (!action.isDone()) {
                SingleAction a = action.getAvailableAction(action);
                if (a != null) {
                    ActionLogger actionLog = loggingEventRepository.createActionLogger(a.getDeployable().getPath(":"), a.getId(), listeners);
                    log.info("Submitting action for execution: " + a.getLabel());
                    a.setStatus(Action.Status.RUNNING);
                    executorService.submit(() -> a.apply(actionLog));
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Todo - cancel execution
                        throw new ActionException(e);
                    }
                }
                //System.out.println(action);
            }
            if (onComplete != null) {
                onComplete.onComplete(null);
            }
        });
    }


    public void shutdown() {
        executorService.shutdown();
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}

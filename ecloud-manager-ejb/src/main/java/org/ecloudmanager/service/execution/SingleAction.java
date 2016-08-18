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

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.deployment.core.DeploymentObject;
import org.ecloudmanager.node.model.ExecutionDetails;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.annotations.Transient;

import java.util.concurrent.Callable;

public class SingleAction extends Action implements Runnable {

    @Transient
    private Callable<ExecutionDetails> callable;
    @Transient
    private Callable rollback;
    @Serialized
    private Throwable failure;
    @Serialized
    private Throwable rollbackFailure;
    private String description;
    @Reference
    private DeploymentObject topDeployable;
    private String pathToDeployable;
    @Transient
    private Logger log = LogManager.getLogger(SingleAction.class);

    protected SingleAction() {
    }

    public SingleAction(Callable<ExecutionDetails> task, String description) {
        super();
        this.callable = task;
        this.description = description;
    }

    public SingleAction(Callable<ExecutionDetails> task, String description, Deployable deployable) {
        super();
        this.callable = task;
        this.description = description;
        this.topDeployable = deployable.getTop();
        this.pathToDeployable = topDeployable.relativePathTo(deployable);
    }

    public SingleAction(Callable<ExecutionDetails> task, Callable rollback, String description) {
        this(task, description);
        this.rollback = rollback;
    }

    private static void logDetails(Logger log, ExecutionDetails details) {
        details.getLog().forEach(e -> {
            switch (e.getLevel()) {
                case INFO:
                    log.info(e.getMessage());
                    break;
                case ERROR:
                    log.error(e.getMessage());
                    break;
                case WARNING:
                    log.warn(e.getMessage());
                    break;
            }
        });
        switch (details.getStatus()) {
            case OK:
                log.info("Action completed. " + Strings.nullToEmpty(details.getMessage()));
                break;
            case FAILED:
                log.error("Action failed. " + Strings.nullToEmpty(details.getMessage()));
                break;
        }
    }

    public String getDescription() {
        return description;
    }

    @Override
    public SingleAction getAvailableAction(Action fullAction) {
        if (isReady(fullAction)) {
            return this;
        }
        return null;
    }

    @Override
    public SingleAction getAvailableRollbackAction() {
        if (!isRollbackReady()) {
            return null;
        }
        return this;
    }

    @Override
    public void run() {
        Status status = getStatus();
        if (status == Status.RUNNING) {
            try {
                ThreadContext.clearAll();
                ThreadContext.put("action", getId());
                ThreadContext.put("topDeployable", topDeployable.getName());
                ThreadContext.put("deployable", getDeployable().getName());
                ExecutionDetails details = callable.call();
                if (details != null) {
                    logDetails(log, details);
                    switch (details.getStatus()) {
                        case OK:
                            setStatus(Status.SUCCESSFUL);
                            break;
                        case FAILED:
                            setStatus(Status.FAILED);
                            break;
                    }
                } else {
                    setStatus(Status.SUCCESSFUL);
                }
                ThreadContext.clearAll();
            } catch (Exception t) {
                failure = t;
                log.error("Failed to execute action " + getLabel() + ": ", t);
                setStatus(Status.FAILED);
                ThreadContext.clearAll();
            }
        } else if (status == Status.ROLLBACK_RUNNING) {
            try {
                System.out.println("Rollback : " + toString());
                if (rollback != null) {
                    rollback.call();
                }
                setStatus(Status.PENDING);
            } catch (Exception t) {
                rollbackFailure = t;
                // PENDING ????
                setStatus(Status.PENDING);
            }

        } else {
            throw new RuntimeException("Invalid action state " + toString());
        }
    }

    public Throwable getFailure() {
        return failure;
    }

    public Deployable getDeployable() {
        if (topDeployable == null || pathToDeployable == null) {
            return null;
        }

        return (Deployable) topDeployable.getByPath(pathToDeployable);
    }

    @Override
    public String getLabel() {
        return description +
            (getDeployable() == null ? "" : "[" + getDeployable().getName() + "]");
    }

    public String toString() {
        String result = description +
            (getDeployable() == null ? "" : "[" + getDeployable().getName() + "]") +
            (rollback != null ? "|" + rollback.toString() : "") + " " + getStatus();
        if (getFailure() != null) {
            result = result + " " + getFailure().getMessage();
        }
//        List<Action> depends = dependsOn();
//        if (!depends.isEmpty()) {
//            result = result + " Depends:" + dependsOn();
//        }
        return result;
    }

    public void setCallable(Callable callable) {
        this.callable = callable;
    }

}

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

import org.ecloudmanager.deployment.core.Deployable;
import org.ecloudmanager.node.model.ExecutionDetails;
import org.mongodb.morphia.annotations.Transient;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Action {
    private final Set<String> dependsIds = new HashSet<>();
    @Transient
    private final List<Action> depends = new ArrayList<>();
    @Transient
    private final List<Action> requiredBy = new ArrayList<>();
    private final String id = UUID.randomUUID().toString();
    private Status status = Status.PENDING;

    public Action() {
    }

    public static ActionGroup actionSequence(String name, Action... actions) {
        return Action.actionSequence(name, Arrays.asList(actions));
    }

    public static ActionGroup actionSequence(String name, Collection<Action> actions) {
        ActionGroup sequence = new ActionGroup(name, null);
        Action dependency = null;
        for (Action a : actions) {
            if (a != null) {
                if (dependency != null) {
                    a.addDependencies(dependency);
                }
                sequence.addAction(a);
                dependency = a;
            }
        }
        return sequence;
    }

    public static ActionGroup actionGroup(String name, Action... actions) {
        ActionGroup group = new ActionGroup(name, null);
        for (Action a : actions) {
            group.addAction(a);
        }
        return group;
    }

    public static ActionGroup actionGroup(String name, Collection<Action> actions) {
        ActionGroup group = new ActionGroup(name, null);
        for (Action a : actions) {
            group.addAction(a);
        }
        return group;
    }

    public static Action single(String description, Callable<ExecutionDetails> callable) {
        return new SingleAction(callable, description);
    }

    public static Action single(String description, Callable<ExecutionDetails> runnable, Deployable deployable) {
        return new SingleAction(runnable, description, deployable);
    }

    public String getId() {
        return id;
    }

    public void addDependencies(Action... dependencies) {
        for (Action a : dependencies) {
            if (a != null) {
                this.depends.add(a);
                this.dependsIds.add(a.getId());
                a.requiredBy.add(this);
            }
        }
    }

    public abstract SingleAction getAvailableAction(Action fullAction);

    public abstract SingleAction getAvailableRollbackAction();

    public synchronized Status getStatus() {
        return status;
    }

    protected synchronized void setStatus(Status status) {
        this.status = status;
        if (status == Status.FAILED || status == Status.NOT_RUN) {
            for (Action a : requiredBy) {
                a.setStatus(Status.NOT_RUN);
            }
        }
    }

    public boolean isDone() {
        return getStatus() == Status.FAILED || getStatus() == Status.SUCCESSFUL;
    }

    protected boolean isReady(Action fullAction) {
        if (getStatus() != Status.PENDING) {
            return false;
        }
        boolean result = true;
        for (Action action : getDependencies(fullAction)) {
            if (action.getStatus() == Status.FAILED || action.getStatus() == Status.NOT_RUN) {
                setStatus(Status.NOT_RUN);
            }

            if (action.getStatus() != Status.SUCCESSFUL) {
                result = false;
            }
        }
        return result;
    }

    protected boolean isRollbackReady() {
        if (!isDone()) {
            return false;
        }
        for (Action action : requiredBy) {
            if (action.getStatus() != Status.PENDING) {
                return false;
            }
        }
        return true;
    }

    public Stream<Action> stream() {
        return Stream.of(this);
    }

    public <T extends Action> Stream<T> stream(Class<T> type) {
        return stream().filter(type::isInstance).map(type::cast);
    }

    public abstract String getLabel();

    public List<Action> getDependencies(Action fullAction) {
        return depends;
    }

    public void restoreDependencies() {
        Map<String, Action> idToAction = stream().collect(Collectors.toMap(Action::getId, Function.identity()));
        stream().forEach(action -> {
            action.dependsIds.stream().forEach(id -> action.addDependencies(idToAction.get(id)));
        });
    }

    public enum Status {
        PENDING,
        RUNNING,
        ROLLBACK_RUNNING,
        SUCCESSFUL,
        FAILED,
        NOT_RUN     // has FAILED dependencies
    }
}

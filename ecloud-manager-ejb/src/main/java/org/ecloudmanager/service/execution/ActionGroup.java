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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ActionGroup extends Action {
    private final List<Action> actions = new ArrayList<>();
    private String name;

    private ActionGroup() {
    }

    public ActionGroup(String name, Collection<Action> actions) {
        super();
        if (actions != null) {
            this.actions.addAll(actions);
        }
        this.name = name;
    }

    void addAction(Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    @Override
    public synchronized SingleAction getAvailableAction(Action fullAction) {
        if (!isReady(fullAction) && getStatus() != Status.RUNNING) {
            return null;
        }
        for (Action a : actions) {
            SingleAction result = a.getAvailableAction(fullAction);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public synchronized SingleAction getAvailableRollbackAction() {
        if (!isRollbackReady() && getStatus() != Status.ROLLBACK_RUNNING) {
            return null;
        }
        for (Action a : actions) {
            SingleAction result = a.getAvailableRollbackAction();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public synchronized Status getStatus() {
        boolean hasFailed = false;
        boolean hasFinished = false;
        boolean hasNotFinished = false;
        boolean hasRunning = false;
        boolean hasRollback = false;
        Status oldStatus = super.getStatus();

        if (oldStatus == Status.NOT_RUN) {
            return oldStatus;
        }

        for (Action a : actions) {
            switch (a.getStatus()) {
                case PENDING:
                    hasNotFinished = true;
                    break;
                case RUNNING:
                    hasRunning = true;
                    break;
                case ROLLBACK_RUNNING:
                    hasRollback = true;
                    break;
                case SUCCESSFUL:
                    hasFinished = true;
                    break;
                case FAILED:
                    hasFinished = true;
                    hasFailed = true;
                    break;
                case NOT_RUN:
                    break;
            }
            if (hasFinished && hasNotFinished && (oldStatus == Status.ROLLBACK_RUNNING || oldStatus == Status
                .RUNNING)) {
                return oldStatus;
            }

            if (hasRollback) {
                setStatus(Status.ROLLBACK_RUNNING);
                return super.getStatus();
            }
            if (hasRunning) {
                setStatus(Status.RUNNING);
                return super.getStatus();
            }
        }
        if (hasNotFinished) {
            setStatus(Status.PENDING);
            return super.getStatus();
        }
        if (hasFailed) {
            setStatus(Status.FAILED);
        } else {
            setStatus(Status.SUCCESSFUL);
        }
        return super.getStatus();
    }

    @Override
    public Stream<Action> stream() {
        return Stream.concat(
            Stream.of(this),
            actions.stream().flatMap(Action::stream));
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public String getLabel() {
        return name;
    }

    public String toString() {
        String result = name + " " + getStatus() + actions;
//        List<Action> depends = dependsOn();
//        if (!depends.isEmpty()) {
//            result = result + " Depends:" + dependsOn();
//        }
        return result;
    }
}

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

package org.ecloudmanager.deployment.core;

import org.ecloudmanager.deployment.history.DeploymentAttempt;
import org.ecloudmanager.service.execution.Action;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractDeployer<T extends Deployable> implements Deployer<T> {
    private static void initDependencies(Map<Deployable, Action> actionMap, boolean reverse) {
        actionMap.forEach((d, a) -> {
            d.getRequired().filter(actionMap::containsKey).forEach(r -> {
                if (reverse) {
                    actionMap.get(r).addDependencies(a);
                } else {
                    a.addDependencies(actionMap.get(r));
                }
            });
        });
    }

    public abstract Action getBeforeChildrenCreatedAction(T deployable);

    public abstract Action getAfterChildrenCreatedAction(T deployable);

    public abstract Action getAfterChildrenDeletedAction(T deployable);

    public abstract Action getBeforeChildrenDeletedAction(T deployable);

    public abstract Action getBeforeChildrenUpdatedAction(T before, T after);

    public abstract Action getAfterChildrenUpdatedAction(T before, T after);

    public final Action getCreateAction(T deployable) {
        Map<Deployable, Action> createActions = new HashMap<>();
        deployable.children(Deployable.class).forEach(s -> createActions.put(s, s.getDeployer().getCreateAction(s)));
//        initDependencies(createActions, false);

        String childrenActionName = "Create " + deployable.getName() + " children";
        String mainActionName = "Create " + deployable.getName();

        return generateCompositeAction(
            childrenActionName,
            mainActionName,
            createActions.values(),
            getBeforeChildrenCreatedAction(deployable),
            getAfterChildrenCreatedAction(deployable)
        );
    }

    public final Action getDeleteAction(T deployable) {
        Map<Deployable, Action> deleteActions = new HashMap<>();
        deployable.children(Deployable.class).forEach(s -> deleteActions.put(s, s.getDeployer().getDeleteAction(s)));
        // initDependencies(deleteActions, true);

        String childrenActionName = "Delete " + deployable.getName() + " children";
        String mainActionName = "Delete " + deployable.getName();

        return generateCompositeAction(
            childrenActionName,
            mainActionName,
            deleteActions.values(),
            getBeforeChildrenDeletedAction(deployable),
            getAfterChildrenDeletedAction(deployable)
        );
    }

    public final Action getUpdateAction(DeploymentAttempt lastAttempt, T before, T after) {
        if (isRecreateActionRequired(before, after)) {
            return Action.actionSequence(
                "Recreate  " + before.getName(),
                getDeleteAction(before),
                getCreateAction(after)
            );
        } else {
            Stream<Deployable> toUpdate = after.children(Deployable.class).stream().filter(c -> before.getChildByName
                (c.getName()) != null);
            Stream<Deployable> toCreate = after.children(Deployable.class).stream().filter(c -> before.getChildByName
                (c.getName()) == null);
            Stream<Deployable> toDelete = before.children(Deployable.class).stream().filter(c -> after.getChildByName
                (c.getName()) == null);

            List<Action> actions = new ArrayList<>();

            Map<Deployable, Action> updateActions = new HashMap<>();
            toUpdate.forEach(s -> updateActions.put(s, s.getDeployer().getUpdateAction(lastAttempt, (Deployable)
                before.getChildByName(s
                    .getName()), s)));
//            initDependencies(updateActions, false);

            Map<Deployable, Action> createActions = new HashMap<>();
            toCreate.forEach(s -> createActions.put(s, s.getDeployer().getCreateAction(s)));
//            initDependencies(createActions, false);

            Map<Deployable, Action> deleteActions = new HashMap<>();
            toDelete.forEach(s -> deleteActions.put(s, s.getDeployer().getDeleteAction(s)));
//            initDependencies(deleteActions, true);

            actions.addAll(updateActions.values());
            actions.addAll(createActions.values());
            actions.addAll(deleteActions.values());

            String childrenActionName = "Update " + after.getName() + " children";
            String mainActionName = "Update " + after.getName();

            return generateCompositeAction(
                childrenActionName,
                mainActionName,
                actions,
                getBeforeChildrenUpdatedAction(before, after),
                getAfterChildrenUpdatedAction(before, after)
            );
        }
    }

    private Action generateCompositeAction(
        String childrenActionName,
        String mainActionName,
        Collection<Action> childrenActions,
        Action beforeAction,
        Action afterAction
    ) {
        boolean multipleChildren = childrenActions.size() > 1;
        boolean hasBeforeOrAfter = beforeAction != null || afterAction != null;
        if (multipleChildren && hasBeforeOrAfter) {
            Action childrenAction = Action.actionGroup(childrenActionName, childrenActions);
            return Action.actionSequence(
                mainActionName,
                beforeAction,
                childrenAction,
                afterAction
            );
        } else if (multipleChildren) { // No before/after - put children actions on top level
            return Action.actionGroup(
                mainActionName,
                childrenActions
            );
        } else if (childrenActions.size() == 1 || hasBeforeOrAfter) { // One or zero children - no need to create
            // group for children
            return Action.actionSequence(
                mainActionName,
                beforeAction,
                childrenActions.size() > 0 ? childrenActions.iterator().next() : null,
                afterAction
            );
        } else { // Nothing to do
            return null;
        }
    }

}

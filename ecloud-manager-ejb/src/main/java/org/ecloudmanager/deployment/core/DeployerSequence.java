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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// combines two or more Deployers
public class DeployerSequence<T extends Deployable> implements Deployer<T> {
    private final List<Deployer<T>> sequence;

    @SafeVarargs
    public DeployerSequence(Deployer<T>... sequence) {
        this.sequence = Arrays.asList(sequence);
    }

    @Override
    public void specifyConstraints(T deployable) {
        sequence.forEach(s -> s.specifyConstraints(deployable));
    }

    @Override
    public Action getCreateAction(T deployable) {
        return Action.actionSequence(deployable.getName() + "_create", sequence.stream().map(s -> s.getCreateAction
            (deployable)).collect(Collectors.toList()));
    }

    @Override
    public Action getDeleteAction(T deployable) {
        return Action.actionSequence(deployable.getName() + "_delete", sequence.stream().map(s -> s.getDeleteAction
            (deployable)).collect(Collectors.toList()));
    }

    @Override
    public Action getUpdateAction(DeploymentAttempt lastAttempt, T before, T after) {
        return Action.actionSequence(after.getName() + "_update", sequence.stream()
            .map(s -> s.getUpdateAction(lastAttempt, before, after))
            .collect(Collectors.toList())
        );
    }
}

package org.ecloudmanager.service.execution;

import org.ecloudmanager.repository.deployment.ActionLogger;

@FunctionalInterface
public interface ActionCallable<V> {
    V apply(ActionLogger actionLogger) throws Exception;
}

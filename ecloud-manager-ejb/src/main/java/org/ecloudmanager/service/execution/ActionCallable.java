package org.ecloudmanager.service.execution;

import org.ecloudmanager.repository.deployment.ActionLogger;

import java.util.concurrent.ExecutorService;

@FunctionalInterface
public interface ActionCallable<V> {
    V apply(ExecutorService executorService, ActionLogger actionLogger) throws Exception;
}

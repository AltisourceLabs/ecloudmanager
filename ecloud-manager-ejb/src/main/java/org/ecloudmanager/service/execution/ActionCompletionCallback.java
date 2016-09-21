package org.ecloudmanager.service.execution;

@FunctionalInterface
public interface ActionCompletionCallback {
    void onComplete(Exception exception);
}

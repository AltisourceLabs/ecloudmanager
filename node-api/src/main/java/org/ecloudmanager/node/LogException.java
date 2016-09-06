package org.ecloudmanager.node;

import org.ecloudmanager.node.model.LoggingEvent;

import java.util.concurrent.ExecutionException;

public class LogException extends ExecutionException {
    private LoggingEvent error;

    public LogException error(LoggingEvent error) {
        this.error = error;
        return this;
    }

    public LoggingEvent getError() {
        return error;
    }

    public void setError(LoggingEvent error) {
        this.error = error;
    }
}

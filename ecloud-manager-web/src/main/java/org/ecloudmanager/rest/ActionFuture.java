package org.ecloudmanager.rest;

import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.service.execution.ActionCompletionCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ActionFuture implements LoggableFuture<Void>, LoggingEventListener, ActionCompletionCallback {
    private BlockingQueue<LoggingEvent> logs = new LinkedBlockingQueue<>();
    private boolean done = false;
    private Exception exception = null;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(500); //FIXME
        }
        if (exception != null) {
            throw new ExecutionException("Action execution failed", exception);
        }
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<LoggingEvent> pollLogs() {
        List<LoggingEvent> result = new ArrayList<>();
        logs.drainTo(result);
        return result;
    }

    @Override
    public void log(Collection<LoggingEvent> events) {
        logs.addAll(events);
    }

    @Override
    public void onComplete(Exception exception) {
        this.exception = exception;
        this.done = true;
    }
}

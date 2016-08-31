package org.ecloudmanager.node;

import org.ecloudmanager.node.model.LoggingEvent;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LocalLoggableFuture<T> implements LoggableFuture<T> {

    private String id;
    private Future<T> f;

    LocalLoggableFuture(String id, Future<T> f) {
        this.id = id;
        this.f = f;
    }

    @Override
    public List<LoggingEvent> pollLogs() {
        return LocalTaskLogs.pollLogs(id);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return f.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return f.isCancelled();
    }

    @Override
    public boolean isDone() {
        return f.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return f.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return f.get(timeout, unit);
    }

    public String getId() {
        return id;
    }

}

package org.ecloudmanager.node.rest;

import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.model.TaskException;
import org.ecloudmanager.node.model.TaskInfo;
import org.ecloudmanager.node.rest.client.TasksApi;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RestLoggableFuture<T> implements LoggableFuture<T> {

    private final String taskId;
    private final TasksApi tasksApi;
    private final Class<T> type;

    RestLoggableFuture(String taskId, TasksApi tasksApi, Class<T> type) {

        this.taskId = taskId;
        this.tasksApi = tasksApi;
        this.type = type;
    }

    private static <V> V call(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LoggingEvent> pollLogs() {
        try {
            return tasksApi.pollLog(taskId);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

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
        try {
            TaskInfo info = tasksApi.getTask(taskId);
            return info.getDone();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        TaskInfo info = call(() -> tasksApi.getTask(taskId));
        while (!info.getDone()) {
            Thread.sleep(500); //FIXME
            info = call(() -> tasksApi.getTask(taskId));
        }
        TaskException te = info.getException();
        if (te != null) {
            throw new ExecutionException(te.getMessage() + " Exception type: " + te.getType(), new Throwable());
        }
        Object value = info.getValue();

        if (value == null) {
            return null;
        }
        if (String.class.equals(type)) {
            return (T) value.toString();
        }
        if (Integer.class.equals(type)) {
            if (Number.class.isInstance(value)) {
                return (T) new Integer(Number.class.cast(value).intValue());
            }
            T result;
            try {
                result = (T) Integer.valueOf(value.toString());
            } catch (NumberFormatException e) {
                throw new ExecutionException("Can't parse result as Integer", e);
            }
        }

        return (T) info.getValue();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        //TODO
        throw new RuntimeException("Not implemented");
    }
}

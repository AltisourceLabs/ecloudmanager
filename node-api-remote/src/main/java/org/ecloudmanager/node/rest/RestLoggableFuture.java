package org.ecloudmanager.node.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.ecloudmanager.node.LogException;
import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.model.SecretKey;
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
    private final SecretKey credentials;


    RestLoggableFuture(SecretKey credentials, String taskId, TasksApi tasksApi, Class<T> type) {
        this.credentials = credentials;
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
            return tasksApi.pollLog(credentials.getName(), credentials.getSecret(), taskId);
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
            TaskInfo info = tasksApi.getTask(credentials.getName(), credentials.getSecret(), taskId);
            return info.getDone();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T get() throws InterruptedException, LogException {
        TaskInfo info = call(() -> tasksApi.getTask(credentials.getName(), credentials.getSecret(), taskId));
        while (!info.getDone()) {
            Thread.sleep(500); //FIXME
            info = call(() -> tasksApi.getTask(credentials.getName(), credentials.getSecret(), taskId));
        }
        try {
            LoggingEvent error = info.getException();
            if (error != null) {
                throw new LogException().error(error);
            }
            Object value = info.getValue();
            JsonElement jsonElement = new Gson().toJsonTree(value);
            return new Gson().fromJson(jsonElement, type);
        } finally {
            try {
                tasksApi.deleteTask(credentials.getName(), credentials.getSecret(), taskId);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }

//        if (String.class.equals(type)) {
//            return (T) value.toString();
//        }
//        if (Integer.class.equals(type)) {
//            if (Number.class.isInstance(value)) {
//                return (T) new Integer(Number.class.cast(value).intValue());
//            }
//            T result;
//            try {
//                result = (T) Integer.valueOf(value.toString());
//            } catch (NumberFormatException e) {
//                throw new ExecutionException("Can't parse result as Integer", e);
//            }
//        }
//        if (Map.class.isInstance(value)) {
//        return (T) info.getValue();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        //TODO
        throw new RuntimeException("Not implemented");
    }
}

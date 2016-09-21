package org.ecloudmanager.rest;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.model.TaskInfo;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Tasks {
    private final static Map<String, ActionFuture> tasks = new ConcurrentHashMap<>();
    private static AtomicLong idCounter = new AtomicLong();

    private static String createID() {
        return String.valueOf(idCounter.getAndIncrement());
    }

    public static String addTask(ActionFuture actionFuture) {
        String id = createID();
        tasks.put(id, actionFuture);
        return id;
    }

    public ResponseContext getTask(RequestContext request, String taskId) {
        if (!tasks.containsKey(taskId)) {
            return new ResponseContext().status(Response.Status.NOT_FOUND);
        }
        ActionFuture f = tasks.get(taskId);
        TaskInfo response = new TaskInfo();
        if (!f.isDone()) {
            return new ResponseContext().status(Response.Status.OK).entity(response.done(false));
        }
        try {
            return new ResponseContext().status(Response.Status.OK).entity(response.done(true).value(f.get()));
        } catch (Exception e) {
            LoggingEvent loggingEvent = new LoggingEvent().level("ERROR").logger(Tasks.class.getName())
                    .message("Task " + taskId + " failed").timeStamp(System.currentTimeMillis())
                    .throwable(ExceptionUtils.getStackTrace(e));
            return new ResponseContext().status(Response.Status.OK).entity(response.done(true).exception(loggingEvent));
        }
    }

    public ResponseContext deleteTask(RequestContext request, String taskId) {
        if (!tasks.containsKey(taskId)) {
            return new ResponseContext().status(Response.Status.NOT_FOUND);
        }
        tasks.remove(taskId);
        return new ResponseContext().status(Response.Status.OK);
    }

    public ResponseContext pollLog(RequestContext request, String taskId) {
        if (!tasks.containsKey(taskId)) {
            return new ResponseContext().status(Response.Status.NOT_FOUND);
        }
        return new ResponseContext().status(Response.Status.OK).entity(tasks.get(taskId).pollLogs());
    }
}

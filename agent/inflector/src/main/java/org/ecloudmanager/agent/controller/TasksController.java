package org.ecloudmanager.agent.controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.ecloudmanager.agent.model.TaskInfo;
import org.ecloudmanager.agent.model.TaskLog;
import org.ecloudmanager.agent.model.Tasks;

import javax.ws.rs.core.Response.Status;
import java.util.Arrays;

public class TasksController {
    public ResponseContext getTasks(RequestContext request) {

        TaskInfo taskInfo1 = new TaskInfo().status("failed");
        TaskInfo taskInfo2 = new TaskInfo().status("failed");

        Tasks tasks = new Tasks().tasks(Arrays.asList(taskInfo1, taskInfo2));
        return new ResponseContext()
                .status(Status.OK)
                .entity(tasks);
    }

    public ResponseContext getTask(RequestContext request, String taskId) {
        TaskInfo taskInfo = new TaskInfo().status("Success");
        return new ResponseContext()
                .status(Status.OK)
                .entity(taskInfo);
    }

    public ResponseContext deleteTask(RequestContext request, String taskId) {
        return new ResponseContext()
                .status(Status.NOT_IMPLEMENTED);
    }

    public ResponseContext getLog(io.swagger.inflector.models.RequestContext request, Long taskId, Long skip, Long count) {
        TaskLog log = new TaskLog().entries(Arrays.asList("line1", "line2"));
        return new ResponseContext()
                .status(Status.OK)
                .entity(log);
    }
}
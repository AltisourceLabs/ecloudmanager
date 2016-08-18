package org.ecloudmanager.node.util;

import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.ExecutionDetails;
import org.ecloudmanager.node.model.LogEntry;
import org.ecloudmanager.node.model.NodeInfo;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class NodeUtil {
    public static NodeInfo wait(NodeBaseAPI api, Credentials credentials, String nodeId) throws Exception {
        Callable<NodeInfo> poll = () -> api.getNode(credentials, nodeId);
        Predicate<NodeInfo> check =
                (result) -> NodeInfo.StatusEnum.RUNNING.equals(result.getStatus()) && result.getIp() != null;
        NodeInfo result = new SynchronousPoller().poll(
                    poll, check,
                    1, 600, 20,
                "wait for instance " + nodeId + " to become ready."
            );
        return result;
    }

    public static void logInfo(ExecutionDetails details, String message) {
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.INFO).message(message));
    }

    public static void logError(ExecutionDetails details, String message) {
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message(message));
        details.setStatus(ExecutionDetails.StatusEnum.FAILED);
    }

    public static void logWarn(ExecutionDetails details, String message) {
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.WARNING).message(message));
    }

    public static void logError(ExecutionDetails details, String message, Throwable t) {
        logError(details, message);
        logError(details, "Exception: " + t.getClass().getName());
        logError(details, "Message: " + t.getMessage());
    }

    public static void logWarn(ExecutionDetails details, String s, Exception e) {
        logWarn(details, s);
        logWarn(details, "Exception: " + e.getClass().getName());
        logWarn(details, "Message: " + e.getMessage());
    }

    public static ExecutionDetails merge(ExecutionDetails first, ExecutionDetails second) {
        ExecutionDetails.StatusEnum status = ExecutionDetails.StatusEnum.OK;
        if (first.getStatus().equals(ExecutionDetails.StatusEnum.FAILED) || second.getStatus().equals(ExecutionDetails.StatusEnum.FAILED)) {
            status = ExecutionDetails.StatusEnum.FAILED;
        }
        String message = first.getMessage();
        if (second.getMessage() != null && !Objects.equals(first.getMessage(), second.getMessage())) {
            message = message + second.getMessage();
        }
        ExecutionDetails result = new ExecutionDetails().status(status).message(message);
        if (first.getLog() != null) {
            result.getLog().addAll(first.getLog());
        }
        if (second.getLog() != null) {
            result.getLog().addAll(second.getLog());
        }
        return result;
    }
}

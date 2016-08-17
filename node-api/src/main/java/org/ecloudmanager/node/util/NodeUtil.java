package org.ecloudmanager.node.util;

import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.ExecutionDetails;
import org.ecloudmanager.node.model.LogEntry;
import org.ecloudmanager.node.model.NodeInfo;

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
        details.setStatus(ExecutionDetails.StatusEnum.FAILED);
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message(message));
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message("Message: " + t.getMessage()));
    }
}

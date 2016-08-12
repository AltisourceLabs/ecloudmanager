package org.ecloudmanager.node.util;

import org.ecloudmanager.node.NodeBaseAPI;
import org.ecloudmanager.node.model.*;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class NodeUtil {
    public static CreateNodeResponse createNodeAndWait(NodeBaseAPI api, Credentials credentials, Map<String, String> parameters) throws Exception {
        CreateNodeResponse response = api.createNode(credentials, parameters);
        ExecutionDetails details = response.getDetails();
        if (details.getStatus().equals(ExecutionDetails.StatusEnum.FAILED)) {
            return response;
        }
        Callable<NodeInfo> poll = () -> api.getNode(credentials, response.getNodeId());
        Predicate<NodeInfo> check =
                (result) -> NodeInfo.StatusEnum.RUNNING.equals(result.getStatus()) && result.getIp() != null;
        try {
            NodeInfo result = new SynchronousPoller().poll(
                    poll, check,
                    1, 600, 20,
                    "wait for instance " + response.getNodeId() + " to become ready."
            );
            logInfo(details, "Node running with ip : " + result.getIp());
        } catch (Exception e) {
            logError(details, "Can't obtain node ip address", e);
            ExecutionDetails deleteDetails = api.deleteNode(credentials, response.getNodeId());
            details.getLog().addAll(deleteDetails.getLog());
            // TODO remove node?
        }
        return response;
    }

    public static void logInfo(ExecutionDetails details, String message) {
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.INFO).message(message));
    }

    public static void logError(ExecutionDetails details, String message) {
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message(message));
        details.setStatus(ExecutionDetails.StatusEnum.FAILED);
    }

    public static void logError(ExecutionDetails details, String message, Throwable t) {
        details.setStatus(ExecutionDetails.StatusEnum.FAILED);
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message(message));
        details.addLogItem(new LogEntry().level(LogEntry.LevelEnum.ERROR).message("Message: " + t.getMessage()));
    }
}

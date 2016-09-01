package org.ecloudmanager.node.util;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.ecloudmanager.node.AsyncNodeBaseAPI;
import org.ecloudmanager.node.model.Credentials;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.model.NodeInfo;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class NodeUtil {
    private static ThrowableProxyConverter converter = new ThrowableProxyConverter();

    public static NodeInfo wait(AsyncNodeBaseAPI api, Credentials credentials, String nodeId) throws Exception {
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


    public static LoggingEvent fromLogback(ILoggingEvent event) {
        return new LoggingEvent().level(event.getLevel().toString()).message(event.getFormattedMessage()).timeStamp(event.getTimeStamp()).throwable(converter.convert(event)).logger(event.getLoggerName());
    }
}

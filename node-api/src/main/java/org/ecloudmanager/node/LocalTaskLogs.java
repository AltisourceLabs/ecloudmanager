package org.ecloudmanager.node;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class LocalTaskLogs extends AppenderBase<ILoggingEvent> {
    public final static String MDC_KEY = "TASK_ID";
    private static AtomicLong idCounter = new AtomicLong();
    private static Map<String, BlockingQueue<LoggingEvent>> logs = new ConcurrentHashMap<>();
    private static Logger log = LoggerFactory.getLogger(LocalTaskLogs.class);
    static {
        //init();
    }

    public static String createID() {
        return String.valueOf(idCounter.getAndIncrement());
    }

//    private static void init() {
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        Appender taskLogsAppender = new LocalTaskLogs();
//        taskLogsAppender.setContext(lc);
//        taskLogsAppender.start();
//        lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(taskLogsAppender);
//    }

    public static List<LoggingEvent> pollLogs(String taskId) {
        BlockingQueue<LoggingEvent> events = logs.get(taskId);
        if (events == null) {
            return Collections.emptyList();
        }
        List<LoggingEvent> result = new ArrayList<>();
        events.drainTo(result);
        return result;
    }

    public static void deleteLogs(String taskId) {
        if (!logs.containsKey(taskId)) {
            return;
        }
        BlockingQueue<LoggingEvent> events = logs.get(taskId);
        if (events != null && !events.isEmpty()) {
            log.warn("Removing not empty logs for task " + taskId);
        }
        logs.remove(taskId);
    }

    @Override
    protected void append(ILoggingEvent o) {
        String taskId = o.getMDCPropertyMap().get(MDC_KEY);
        if (taskId != null) {
            logs.putIfAbsent(taskId, new LinkedBlockingQueue<>());
            Queue<LoggingEvent> queue = logs.get(taskId);
            queue.add(NodeUtil.fromLogback(o));
        }
    }


}

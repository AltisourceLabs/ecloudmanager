package org.ecloudmanager.repository.deployment;

import ch.qos.logback.classic.Level;
import org.ecloudmanager.domain.LoggingEventEntity;
import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.node.model.LoggingEvent;
import org.ecloudmanager.node.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ActionLogger implements LoggingEventListener {
    Logger logger;
    LoggingEventRepository repository;
    private String actionId;
    private String fqcn;
    private ExecutorService executor;

    ActionLogger(Class caller, String actionId, LoggingEventRepository repository, ExecutorService executor) {
        this.actionId = actionId;
        this.fqcn = caller.getName();
        logger = LoggerFactory.getLogger(fqcn);
        this.repository = repository;
        this.executor = executor;
    }

    public <V> V submitAndWait(Callable<V> c) throws ExecutionException, InterruptedException {
        return LoggableFuture.waitFor(LoggableFuture.submit(c, executor), this);
    }

    @Override
    public void log(Collection<LoggingEvent> events) {
        repository.saveAll(events.stream().map(e -> new LoggingEventEntity(actionId, e)).collect(Collectors.toList()));
    }


    private void recordEvent(Level level, String msg, Object[] args, Throwable throwable) {
        ch.qos.logback.classic.spi.LoggingEvent logbackEvent = new ch.qos.logback.classic.spi.LoggingEvent(fqcn, (ch.qos.logback.classic.Logger) logger, level, msg, throwable, args);

        repository.save(new LoggingEventEntity(actionId, NodeUtil.fromLogback(logbackEvent)));
    }

    public void error(String msg, Throwable t) {
        recordEvent(Level.ERROR, msg, null, t);
    }

    public void error(String msg) {
        recordEvent(Level.ERROR, msg, null, null);
    }

    public void info(String msg) {
        recordEvent(Level.INFO, msg, null, null);
    }
}

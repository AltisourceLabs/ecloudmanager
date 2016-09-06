package org.ecloudmanager.repository.deployment;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ecloudmanager.domain.LoggingEventEntity;
import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.node.model.LoggingEvent;
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

    public void log(LoggingEvent event) {
        repository.save(new LoggingEventEntity(actionId, event));
    }

    private void recordEvent(String level, String msg, Throwable throwable) {
        repository.save(new LoggingEventEntity(actionId, new LoggingEvent().level(level).logger(fqcn).message(msg)
                .throwable(throwable == null ? null : ExceptionUtils.getStackTrace(throwable))
                .timeStamp(System.currentTimeMillis())));
    }

    public void error(String msg, Throwable t) {
        recordEvent("ERROR", msg, t);
    }

    public void error(String msg) {
        recordEvent("ERROR", msg, null);
    }

    public void info(String msg) {
        recordEvent("INFO", msg, null);
    }
}

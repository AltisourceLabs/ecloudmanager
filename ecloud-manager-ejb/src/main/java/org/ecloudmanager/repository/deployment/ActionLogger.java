package org.ecloudmanager.repository.deployment;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ecloudmanager.domain.LoggingEventEntity;
import org.ecloudmanager.node.LoggableFuture;
import org.ecloudmanager.node.LoggingEventListener;
import org.ecloudmanager.node.model.LoggingEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ActionLogger implements LoggingEventListener {
    private LoggingEventRepository repository;
    private String actionId;
    private String fqcn;
    private ExecutorService executor;
    private Collection<LoggingEventListener> listeners;

    ActionLogger(Class caller, String actionId, LoggingEventRepository repository, ExecutorService executor, LoggingEventListener... listeners) {
        this.actionId = actionId;
        this.fqcn = caller.getName();
        this.repository = repository;
        this.executor = executor;
        this.listeners = Arrays.asList(listeners);
    }

    public <V> V submitAndWait(Callable<V> c) throws ExecutionException, InterruptedException {
        return LoggableFuture.waitFor(LoggableFuture.submit(c, executor), this);
    }

    @Override
    public void log(Collection<LoggingEvent> events) {
        listeners.forEach(l -> l.log(events));
        repository.saveAll(events.stream().map(e -> new LoggingEventEntity(actionId, e)).collect(Collectors.toList()));
    }

    public void log(LoggingEvent event) {
        log(Collections.singletonList(event));
    }

    private void recordEvent(String level, String msg, Throwable throwable) {
        log(new LoggingEvent().level(level).logger(fqcn).message(msg)
                .throwable(throwable == null ? null : ExceptionUtils.getStackTrace(throwable))
                .timeStamp(System.currentTimeMillis()));
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

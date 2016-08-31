package org.ecloudmanager.node;

import org.ecloudmanager.node.model.LoggingEvent;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.ecloudmanager.node.LocalTaskLogs.MDC_KEY;

public interface LoggableFuture<T> extends Future<T> {
    public static <T> LoggableFuture<T> failedFuture(String message, Throwable clause) {
        return new LoggableFuture<T>() {
            ExecutionException executionException = new ExecutionException(message, clause);

            @Override
            public List<LoggingEvent> pollLogs() {
                return Collections.emptyList();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                throw executionException;
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw executionException;
            }
        };
    }

    static <V> V waitFor(LoggableFuture<V> future, LoggingEventListener... listeners) throws ExecutionException, InterruptedException {
        List<LoggingEventListener> listenerList = Arrays.asList(listeners);
        boolean hasLogs = false;
        do {
            Thread.sleep(500); //FIXME
            final List<LoggingEvent> events = future.pollLogs();
            if (!events.isEmpty()) {
                listenerList.forEach(l -> l.log(events));
                hasLogs = true;
            } else {
                hasLogs = false;
            }
        } while (!future.isDone() || hasLogs);
        return future.get();
    }

    static <V> V submitAndWait(Callable<V> c, ExecutorService e, LoggingEventListener... listeners) throws ExecutionException, InterruptedException {
        return waitFor(submit(c, e), listeners);
    }

    static <V> LocalLoggableFuture<V> submit(Callable<V> c, ExecutorService e) {
        String id = LocalTaskLogs.createID();
        Future<V> f = e.submit(() -> {
            try (MDC.MDCCloseable closeable = MDC.putCloseable(MDC_KEY, id)) {
                return c.call();
            }
        });
        return new LocalLoggableFuture<V>(id, f);
    }

    List<LoggingEvent> pollLogs();
}

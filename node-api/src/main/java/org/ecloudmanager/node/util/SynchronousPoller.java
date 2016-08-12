package org.ecloudmanager.node.util;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class SynchronousPoller {
    private Logger log = LoggerFactory.getLogger(SynchronousPoller.class);

    public <T> T poll(
            Callable<T> check,
            Predicate<T> condition,
            double pollsPerSecond,
            long timeout,
            String taskName
    ) {
        return poll(check, condition, pollsPerSecond, timeout, 0, taskName);
    }

    public <T> T poll(
            Callable<T> check,
            Predicate<T> condition,
            double pollsPerSecond,
            long timeout,
            int retriesOnException,
            String taskName
    ) {
        log.info("Started execution: " + taskName);
        Stopwatch stopwatch = Stopwatch.createStarted();
        RateLimiter limiter = RateLimiter.create(pollsPerSecond);
        T result;
        boolean exceptionSuppressed;
        do {
            exceptionSuppressed = false;
            try {
                if (stopwatch.elapsed(TimeUnit.SECONDS) > timeout) {
                    throw new TimeoutException("Timeout while executing: " + taskName);
                }
                limiter.acquire();
                result = check.call();
            } catch (Exception e) {
                retriesOnException--;
                if (retriesOnException <= 0 || e instanceof TimeoutException) {
                    log.error("Error while executing: " + taskName, e);
                    throw new RuntimeException(e);
                } else {
                    log.warn("Error while executing: " + taskName + ". Retry attempts left: " + (retriesOnException + 1) + ".", e);
                    exceptionSuppressed = true;
                    result = null;
                }
            }
        } while (exceptionSuppressed || !condition.test(result));

        return result;
    }

}

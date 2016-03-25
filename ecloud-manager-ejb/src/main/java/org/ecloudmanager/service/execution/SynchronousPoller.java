/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.service.execution;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.Logger;
import org.ecloudmanager.jeecore.service.Service;

import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

@Service
public class SynchronousPoller {
    @Inject
    private Logger log;

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
                    throw new ActionException(e);
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

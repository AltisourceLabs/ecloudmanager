package org.ecloudmanager.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newCachedThreadPool();

        Function<Integer, Supplier<List<Integer>>> getFirstTenMultiples = num ->
                () -> {
                    String id = MDC.get(LocalTaskLogs.MDC_KEY);
                    log.info("getFirstTen: " + id);
                    return Stream.iterate(num, i -> i + num).limit(3).collect(Collectors.toList());
                };

        Supplier<List<Integer>> multiplesSupplier = getFirstTenMultiples.apply(11);

        //Original CompletionStage
        CompletableFuture<List<Integer>> getMultiples = CompletableFuture.supplyAsync(multiplesSupplier, service);

        //Function that takes input from orignal CompletionStage
        Function<List<Integer>, CompletableFuture<Integer>> sumNumbers = multiples ->
                CompletableFuture.supplyAsync(() -> {
                    String id = MDC.get(LocalTaskLogs.MDC_KEY);
                    log.info("sum: " + id);
                    return multiples.stream().mapToInt(Integer::intValue).sum();
                });

        //The final CompletableFuture composed of previous two.
        CompletableFuture<Integer> summedMultiples = getMultiples.thenComposeAsync(sumNumbers, service);

        System.out.println(summedMultiples.get());
        service.shutdown();
//        LoggableThreadPoolExecutor e = new LoggableThreadPoolExecutor(4);
//        LoggableFutureTask<String> f1 = e.submit(()->{
//            log.info("Started 1-1");
//            log.info("Started 1-2");
//            for (int i = 0; i < 5; i++) {
//                log.info("Execution 1 Step: " + i);
//                Thread.sleep(2000);
//            }
//            return "OK";
//        });
//        LoggableFutureTask<String> f2 = e.submit(()->{
//            log.error("Started execution 2");
//            for (int i = 0; i < 5; i++) {
//                if (i==2) {
//                    throw new Exception("ops");
//                }
//                log.info("Execution 2 Step: " + i);
//                Thread.sleep(1000);
//            }
//            return "OK";
//        });
//        while (!f1.isDone() || !f2.isDone()) {
//            log.info("Check logs 1:");
//            f1.pollLogs().forEach(le -> System.out.println(le.getMessage()));
//            log.info("Check logs 2:");
//            f2.pollLogs().forEach(le -> System.out.println(le.getMessage()));
//            Thread.sleep(1200);
//        }
//        try {
//            log.info("f1: " + f1.get());
//        } catch (ExecutionException e1) {
//            log.error("f1", e1);
//        }
//        try {
//            log.info("f2: " + f2.get());
//        } catch (ExecutionException e1) {
//            log.error("f2", e1);
//        }
//        e.shutdown();
    }
}

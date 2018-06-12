package wm.assignment.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorRegistry {
    private final static Log log = LogFactory.getLog(ExecutorRegistry.class);

    private static List<ExecutorService> executors = new ArrayList<>();

    public static void register(ExecutorService executor) {
        executors.add(executor);
    }

    public static void shutdown() {
        executors.forEach(ExecutorRegistry::shutdownExecutorService);
    }

    public static void runInRegistryContext(Runnable r) {
        try {
            r.run();
        }
        finally {
            log.info("Shutting down the services in the ExecutorRegistry");
            shutdown();
        }
    }

    private static void shutdownExecutorService(ExecutorService executor) {
        try {
            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            log.warn("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                log.warn("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }
    }



}

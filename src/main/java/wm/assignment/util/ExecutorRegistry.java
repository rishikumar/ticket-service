package wm.assignment.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In memory registry to track and manage the executors created by the application.
 * This class manages the lifecycle of the executors
 */
public class ExecutorRegistry {
    private final static Log log = LogFactory.getLog(ExecutorRegistry.class);

    private static List<ExecutorService> executors = new ArrayList<>();

    /**
     * Register an ExecutorService
     * @param executor
     */
    public static void register(ExecutorService executor) {
        executors.add(executor);
    }

    /**
     * Run the provided runnable within the context of the registry. Shut down when complete
     * @param r
     */
    public static void runInRegistryContext(Runnable r) {
        try {
            r.run();
        }
        finally {
            log.info("Shutting down the services in the ExecutorRegistry");
            shutdown();
        }
    }

    /**
     * Invokes shutdownExecutorService on each registered Executor
     */
    private static void shutdown() {
        executors.forEach(ExecutorRegistry::shutdownExecutorService);
    }

    /**
     * Attempt to gracefully shut down the executors with a grace period. If it expires, shutdown immediately.
     * @param executor
     */
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

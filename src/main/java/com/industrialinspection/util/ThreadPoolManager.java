package com.industrialinspection.util;

import java.util.concurrent.*;

/**
 * Enterprise Concurrency Manager providing specialized thread pools
 * for background image processing, async audit logging, and report generation.
 */
public class ThreadPoolManager {

    private static volatile ThreadPoolManager instance;

    // Dedicated Executor for image analysis simulation
    private final ExecutorService imageProcessingPool;

    // Dedicated single-thread Executor for background audit logging (FIFO guarantee)
    private final ExecutorService auditLoggingPool;

    // General asynchronous tasks (Reports, exports, notifications)
    private final ExecutorService generalTaskPool;

    private ThreadPoolManager() {
        int cores = Runtime.getRuntime().availableProcessors();
        this.imageProcessingPool = Executors.newFixedThreadPool(Math.max(2, cores), new NamedThreadFactory("AI-Image-Worker"));
        this.auditLoggingPool = Executors.newSingleThreadExecutor(new NamedThreadFactory("Audit-Log-Worker"));
        this.generalTaskPool = Executors.newFixedThreadPool(Math.max(2, cores), new NamedThreadFactory("General-Task-Worker"));

        // Register JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public <T> Future<T> submitImageTask(Callable<T> task) {
        return imageProcessingPool.submit(task);
    }

    public void executeImageTask(Runnable task) {
        imageProcessingPool.execute(task);
    }

    public void executeAuditLog(Runnable logTask) {
        auditLoggingPool.execute(logTask);
    }

    public void executeBackgroundTask(Runnable task) {
        generalTaskPool.execute(task);
    }

    public void shutdown() {
        shutdownPool(imageProcessingPool, "ImageProcessingPool");
        shutdownPool(auditLoggingPool, "AuditLoggingPool");
        shutdownPool(generalTaskPool, "GeneralTaskPool");
    }

    private void shutdownPool(ExecutorService pool, String poolName) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private int count = 1;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + (count++));
            t.setDaemon(true);
            return t;
        }
    }
}

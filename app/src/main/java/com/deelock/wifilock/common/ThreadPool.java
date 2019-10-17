package com.deelock.wifilock.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static ThreadPool pool;
    private ExecutorService service = Executors.newCachedThreadPool();

    public static ThreadPool getPool() {
        if (pool == null) {
            synchronized (ThreadPool.class) {
                if (pool == null) {
                    pool = new ThreadPool();
                }
            }
        }
        return pool;
    }

    public ExecutorService getService() {
        return service;
    }

    public void endAllThread() {
        service.shutdown();
        service.shutdownNow();
    }
}

/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.concurrency;

import static com.girbola.messages.Messages.sprintf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marko Lokka
 */
public class ConcurrencyUtils {

    private static AtomicInteger execCounter = new AtomicInteger();

    public static ExecutorService[] exec = new ExecutorService[3];

    public static int getExecCounter() {
        return execCounter.get();
    }
    public static ExecutorService getExec() {
    	return exec[execCounter.get()];
    }

    public static void initExecutionService() {
        stopExecThreadNow();
    	execCounter.incrementAndGet();
        sprintf("========NEW initExecutionService initializing execThreads: " + getExecCounter());

        if (getExecCounter() >= 1) {
            exec = new ExecutorService[getExecCounter() + 1];
            sprintf("initExecutionService execThreads is: " + getExecCounter());
        }
        exec[getExecCounter()] = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Markon ExecutionService thread: " + getExecCounter());
            t.setDaemon(true); // allows app to exit if tasks are running
            return t;
        });

    }

    public static void stopExecThreadNow() {
        if (exec[getExecCounter()] != null) {
            if (!exec[getExecCounter()].isTerminated()) {
                exec[getExecCounter()].shutdownNow();
                sprintf("execThread stopped: " + getExecCounter());
            }
            if (!exec[getExecCounter()].isShutdown()) {
                exec[getExecCounter()].shutdownNow();
                sprintf("execThread stopped: " + getExecCounter());
            }
        }
    }

    public static void stopExecThread() {
        if (exec[getExecCounter()] != null) {
            if (!exec[getExecCounter()].isTerminated()) {
                exec[getExecCounter()].shutdown();
                sprintf("execThread stopped: " + getExecCounter());
            }
            if (!exec[getExecCounter()].isShutdown()) {
                exec[getExecCounter()].shutdown();
                sprintf("execThread stopped: " + getExecCounter());
            }
        }
    }
}

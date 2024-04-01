/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.concurrency;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class ConcurrencyUtils {

    private static AtomicInteger execCounter = new AtomicInteger();

    public static ExecutorService[] exec = new ExecutorService[1];

    public static synchronized int incrementAndGetExecCounter() {
        int newVal = execCounter.incrementAndGet();
        if (newVal >= exec.length) {
            // Increase the size of the array if necessary
            exec = Arrays.copyOf(exec, newVal + 1);
        }
        return newVal;
    }

    public static void initSingleExecutionService() {
        stopExecThreadNow();
        int currentCounter = incrementAndGetExecCounter();
        sprintf("========NEW initExecutionService initializing execThreads: " + currentCounter);

        exec[getExecCounter()] = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("MDir_FX ExecutionService thread: " + getExecCounter());
            t.setDaemon(true); // allows app to exit if tasks are running
            return t;
        });

    }

    public static void stopExecThreadNow() {
        int currentCounter = execCounter.get();

        if (exec[currentCounter] != null) {
            if (!exec[currentCounter].isTerminated()) {
                exec[currentCounter].shutdownNow();
                sprintf("execThread stopped: " + currentCounter);
            }
            if (!exec[currentCounter].isShutdown()) {
                exec[currentCounter].shutdownNow();
                sprintf("execThread stopped: " + currentCounter);
            }
        }
    }

    public static void stopExecThread() {
        int currentCounter = execCounter.get();
        if (exec[currentCounter] != null) {
            if (!exec[currentCounter].isTerminated()) {
                exec[currentCounter].shutdown();
                sprintf("execThread stopped: " + currentCounter);
            }
            if (!exec[currentCounter].isShutdown()) {
                exec[currentCounter].shutdown();
                sprintf("execThread stopped: " + currentCounter);
            }
        }
    }


    public static int getExecCounter() {
        return execCounter.get();
    }
}

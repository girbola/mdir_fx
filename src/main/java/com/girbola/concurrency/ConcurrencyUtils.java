
package com.girbola.concurrency;

import com.girbola.messages.Messages;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.messages.Messages.sprintf;


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

    public static void initNewSingleExecutionService() {
        stopExecThreadNow();
//        boolean terminated = exec[getExecCounter()].isTerminated();
//        if(!terminated) {
//            Messages.sprintf("Single Thread Execution Service were not terminated");
//            return;
//        }

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

    public static ExecutorService getExec() {
        return exec[execCounter.get()];
    }

    public static void stopAllExecThreadNow() {
        for(int i = 0; i < execCounter.get(); i++) {
            if(!exec[execCounter.get()].isShutdown() || !exec[execCounter.get()].isTerminated()) {
                exec[execCounter.get()].shutdownNow();
                Messages.sprintf("Killing executor service tasks");
            }
        }
    }
}

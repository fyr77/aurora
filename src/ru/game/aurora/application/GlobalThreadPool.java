/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 08.07.13
 * Time: 13:49
 */
package ru.game.aurora.application;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class GlobalThreadPool {
    private static final ThreadPoolExecutor executor;

    static {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors > 1) {
            // leave one core to a main thread
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(availableProcessors - 1);
        } else {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        }
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}

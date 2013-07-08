/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 08.07.13
 * Time: 13:49
 */
package ru.game.aurora.application;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class GlobalThreadPool
{
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}

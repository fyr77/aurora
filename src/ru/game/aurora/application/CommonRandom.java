/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:06
 */
package ru.game.aurora.application;

import java.util.Random;

/**
 * Single random instance that should be used everywhere
 */
public final class CommonRandom {
    private static final Random r = new Random();

    public static Random getRandom() {
        return r;
    }

    public static int nextInt(int min, int max){
        return r.nextInt(max - min + 1) + min;
    }
}
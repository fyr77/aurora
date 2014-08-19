package ru.game.aurora.util;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.03.14
 * Time: 0:26
 */

public class GameTimer implements Serializable {
    private static final long serialVersionUID = -1L;

    private int count;

    private final int limit;

    public GameTimer(int limit) {
        this.limit = limit;
        this.count = limit;
    }

    public boolean update() {
        if (--count == 0) {
            count = limit;
            return true;
        }

        return false;
    }

}

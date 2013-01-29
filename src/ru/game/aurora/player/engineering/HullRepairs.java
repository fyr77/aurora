package ru.game.aurora.player.engineering;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 16:16
 */
public class HullRepairs implements Serializable {
    private static final long serialVersionUID = 1453173830700322400L;

    public int engineersAssigned;

    public int remainingPoints;

    public int calcResCost() {
        return remainingPoints * 5;
    }
}

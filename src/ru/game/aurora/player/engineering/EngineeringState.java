/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 16:13
 */
package ru.game.aurora.player.engineering;


import ru.game.aurora.world.World;

import java.io.Serializable;

public class EngineeringState implements Serializable {
    private static final long serialVersionUID = 4160857126094990451L;

    private int idleEngineers;

    private HullRepairs hullRepairs;

    public EngineeringState(int idleEngineers) {
        this.idleEngineers = idleEngineers;
        hullRepairs = new HullRepairs();
    }

    public void update(World world) {
        hullRepairs.update(world);
    }


    public HullRepairs getHullRepairs() {
        return hullRepairs;
    }

    public int getIdleEngineers() {
        return idleEngineers;
    }

    public void setIdleEngineers(int idleEngineers) {
        this.idleEngineers = idleEngineers;
    }

    public void addIdleEngineers(int amount) {
        idleEngineers += amount;
    }
}

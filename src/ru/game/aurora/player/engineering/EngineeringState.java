/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 16:13
 */
package ru.game.aurora.player.engineering;


import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EngineeringState implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idleEngineers;

    private HullRepairs hullRepairs;

    private List<EngineeringProject> projects = new LinkedList<>();

    public EngineeringState(int idleEngineers) {
        this.idleEngineers = idleEngineers;
        hullRepairs = new HullRepairs();
    }

    public void update(World world) {
        hullRepairs.update(world);

        for (Iterator<EngineeringProject> iter = projects.iterator(); iter.hasNext();) {
            EngineeringProject ep = iter.next();
            if (!ep.update(world)) {
                iter.remove();
            }
        }
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

    public List<EngineeringProject> getProjects() {
        return projects;
    }
}

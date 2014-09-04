package ru.game.aurora.npc.ai;

import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Listenable;
import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 */
public abstract class AITask extends Listenable implements Serializable, Comparable<AITask> {
    private static final long serialVersionUID = -3386100083786113511L;

    protected int priority;

    protected boolean completed;

    protected AITask(int priority) {
        this.priority = priority;
        this.completed = false;
    }

    public int getPriority() {
        return priority;
    }

    public abstract void perform(World world, GameObject myObject);


    public boolean isCompleted() {
        return completed;
    }

    @Override
    public int compareTo(AITask o) {
        return Integer.compare(priority, o.priority);
    }
}

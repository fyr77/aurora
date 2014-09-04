package ru.game.aurora.npc.ai;

import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;

public class MoveToTargetTask extends AITask {

    private static final long serialVersionUID = 8222614054985915774L;

    private Positionable target;

    protected MoveToTargetTask(int priority) {
        super(priority);
    }

    @Override
    public void perform(World world, GameObject myObject) {
        if (BasePositionable.getDistance(myObject, target) == 0) {
            completed = true;
            return;
        }
    }
}

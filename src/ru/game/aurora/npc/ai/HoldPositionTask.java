package ru.game.aurora.npc.ai;

import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

/**
 * Just stand on current position
 */
public class HoldPositionTask extends AITask {
    protected HoldPositionTask(int priority) {
        super(priority);
    }

    @Override
    public void perform(World world, GameObject myObject) {
        // do nothing
    }
}

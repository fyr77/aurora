package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Just keeps its orbit
 */
public class KeepOrbitAI implements NPCShipAI {
    private final boolean overridable;

    public KeepOrbitAI(boolean overridable) {

        this.overridable = overridable;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        int normalX = ship.getY();
        int normalY = ship.getX();

        if (CommonRandom.getRandom().nextInt(Math.abs(normalX) + Math.abs(normalY)) < Math.abs(normalX)) {
            if (normalX > 0) {
                ship.moveRight();
            } else {
                ship.moveLeft();
            }
        } else {
            if (normalY > 0) {
                ship.moveUp();
            } else {
                ship.moveDown();
            }
        }

    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean isOverridable() {
        return overridable;
    }
}

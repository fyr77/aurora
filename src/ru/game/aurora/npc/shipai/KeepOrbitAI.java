package ru.game.aurora.npc.shipai;

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
        // todo: flying
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

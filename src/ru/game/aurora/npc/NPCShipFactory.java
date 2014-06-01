package ru.game.aurora.npc;

import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

import java.io.Serializable;

/**
 * Used to create NPC ships for a given race.
 */

public interface NPCShipFactory extends Serializable {
    /**
     * Create a new NPCShip object of a given type.
     * Generally, all races should define shipType=0 as their default most-common ship, that should be used in default
     * ship spawn events.
     */
    public NPCShip createShip(World world, int shipType);
}

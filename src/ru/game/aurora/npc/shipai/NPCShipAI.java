/**
 * User: jedi-philosopher
 * Date: 08.01.13
 * Time: 20:37
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;

public interface NPCShipAI extends Serializable {

    void update(NPCShip ship, World world, StarSystem currentSystem);

    boolean isAlive();

    // if set to false this ship will not change this ai to any other
    // otherwise it will e.g. change to combat ai if attacked
    boolean isOverridable();
}

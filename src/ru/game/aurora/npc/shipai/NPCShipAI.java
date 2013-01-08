/**
 * User: jedi-philosopher
 * Date: 08.01.13
 * Time: 20:37
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

public interface NPCShipAI {
    public void update(NPCShip ship, World world);
}

/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 20:11
 */
package ru.game.aurora.world.space;

import ru.game.aurora.npc.AlienRace;

public class HomeworldGenerator
{
    public static StarSystem generateGardenerHomeworld(int x, int y, int maxSizeX, int maxSizeY, AlienRace race)
    {
        StarSystem ss = GalaxyMap.generateRandomStarSystem(x, y, maxSizeX, maxSizeY);
        NPCShip ship = new NPCShip(5, 5, "gardener_ship", race, null, "Sequoia");
        ss.getShips().add(ship);
        return ss;
    }
}

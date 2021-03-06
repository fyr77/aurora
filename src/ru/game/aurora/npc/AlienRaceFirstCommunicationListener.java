/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.04.14
 * Time: 13:47
 */

package ru.game.aurora.npc;

import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

public class AlienRaceFirstCommunicationListener extends GameEventListener {
    private static final long serialVersionUID = 1042436493645317917L;

    @Override
    public boolean onPlayerContactedOtherShip(World world, GameObject ship) {
        Faction faction = ship.getFaction();
        if (faction != null && faction instanceof AlienRace) {
            ((AlienRace) faction).setKnown(true);
        }
        return false;
    }
}

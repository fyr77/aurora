/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.12.12
 * Time: 15:37
 */
package ru.game.aurora.npc;

import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Random;

/**
 * When player enters some star system that is close to some alien race homeworld, he has a chance of meeting that race's ship
 */
public class StandartAlienShipEvent implements GameEventListener {
    private AlienRace race;

    private static final Random r = new Random();

    public StandartAlienShipEvent(AlienRace race) {
        this.race = race;
    }

    @Override
    public void onPlayerEnterStarSystem(World world, StarSystem ss) {
        double probability = 1 - GalaxyMap.getDistance(ss, race.getHomeworld()) / race.getTravelDistance();
        if (probability < 0) {
            return;
        }

        if (r.nextDouble() < probability) {
            ss.getShips().add(new NPCShip(r.nextInt(ss.getRadius()), r.nextInt(ss.getRadius()), race.getShipSprite(), race, null, "Dummy"));
        }
    }

    @Override
    public boolean isAlive() {
        // these events never expire
        return true;
    }
}

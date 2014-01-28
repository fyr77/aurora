/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.12.12
 * Time: 15:37
 */
package ru.game.aurora.npc;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.npc.shipai.LandOnPlanetAI;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * When player enters some star system that is close to some alien race homeworld, he has a chance of meeting that race's ship
 */
public class StandardAlienShipEvent extends GameEventListener {
    private static final long serialVersionUID = -3413422560284690414L;

    private AlienRace race;

    private NPCShipFactory shipFactory;

    /**
     * If true, will ignore questLocation flag on star systems and will still spawn ships in them.
     */
    private boolean spawnInQuestStarSystems = false;

    public StandardAlienShipEvent(AlienRace race) {
        this.race = race;
        this.shipFactory = race.getDefaultFactory();
    }

    public StandardAlienShipEvent(AlienRace race, NPCShipFactory shipFactory) {
        this.race = race;
        this.shipFactory = shipFactory;
    }

    public StandardAlienShipEvent(AlienRace race, NPCShipFactory shipFactory, boolean spawnInQuestStarSystems) {
        this(race, shipFactory);
        this.spawnInQuestStarSystems = spawnInQuestStarSystems;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation() && !spawnInQuestStarSystems) {
            return false;
        }
        double probability = 1 - GalaxyMap.getDistance(ss, race.getHomeworld()) / race.getTravelDistance();
        if (probability < 0) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() < probability) {
            NPCShip ship = shipFactory.createShip(0);
            ss.setRandomEmptyPosition(ship);
            ss.getShips().add(ship);
            if (CommonRandom.getRandom().nextBoolean() || world.getCurrentStarSystem().getPlanets().length == 0) {
                ship.setAi(new LeaveSystemAI());
            } else {
                ship.setAi(new LandOnPlanetAI(CollectionUtils.selectRandomElement(world.getCurrentStarSystem().getPlanets())));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        // these events never expire
        return true;
    }
}

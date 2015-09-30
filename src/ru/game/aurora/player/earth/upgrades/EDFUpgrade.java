package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.shipai.KeepOrbitAI;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Adds Earth Defence Forces - some armed patrolling spaceships in solar system
 */
public class EDFUpgrade extends EarthUpgrade {
    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "edf", "news"));
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "edf_2.sender", "edf_2", "mail"));
        StarSystem solarSystem = ((StarSystem) world.getGlobalVariables().get("solar_system"));
        for (int i = 0; i < 2; ++i) {
            NPCShip ship = new NPCShip(0, 0, "earth_destroyer", world.getFactions().get(HumanityGenerator.NAME), null, "Patrol Ship", 8);
            ship.setSpeed(2);
            ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"));
            ship.setAi(new KeepOrbitAI(true));
            solarSystem.setRandomEmptyPosition(ship, 0.3, 0.5);
            solarSystem.getObjects().add(ship);
        }

    }
}

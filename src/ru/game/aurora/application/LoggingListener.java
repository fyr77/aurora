package ru.game.aurora.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.StarSystem;

/**
 * Logs game events
 */
public class LoggingListener extends GameEventListener {
    private static final Logger logger = LoggerFactory.getLogger(LoggingListener.class);

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        logger.info("Player entered star system {} at [{}, {}]", ss.getName(), ss.getX(), ss.getY());
        return false;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        logger.info("Player left star system {} at [{}, {}]", ss.getName(), ss.getX(), ss.getY());
        return false;
    }

    @Override
    public boolean onPlayerContactedOtherShip(World world, GameObject ship) {
        logger.info("Player contacted alien ship {}", ship.getName());
        return false;
    }

    @Override
    public boolean onReturnToEarth(World world) {
        logger.info("Player returned to Earth");
        return false;
    }

    @Override
    public boolean onCrewChanged(World world) {
        final Ship ship = world.getPlayer().getShip();
        logger.info("Player crew amount changed from to {}M/{}E/{}S", ship.getMilitary(), ship.getEngineers(), ship.getScientists());
        return false;
    }

    @Override
    public boolean onPlayerEnteredDungeon(World world, Dungeon dungeon) {
        logger.info("Player entered a dungeon {}", dungeon.getUserData());
        return false;
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        logger.info("Player landed planet at [{}, {}]", planet.getX(), planet.getY());
        return false;
    }

    @Override
    public boolean onPlayerLeftPlanet(World world, Planet planet) {
        logger.info("Player has left a planet");
        return false;
    }
}

package ru.game.aurora.player.engineering;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.player.EarthCountry;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 16:16
 */
public class HullRepairs implements Serializable {

    private static final long serialVersionUID = 1453173830700322400L;

    public int engineersAssigned;

    public int remainingPoints;

    public static final int WORK_FOR_POINT = 40;

    public static final int POINT_RES_COST = 5;

    private int currentWorkRemaining = WORK_FOR_POINT;

    private int getRealPointCost(World world) {
        int result = POINT_RES_COST;
        if (world.getPlayer().getMainCountry() == EarthCountry.EUROPE) {
            result = (int) Math.floor(result * Configuration.getDoubleProperty("player.europe.engineeringPriceMultiplier"));
        }
        return result;
    }

    public void update(World world) {
        if (remainingPoints == 0 || engineersAssigned == 0) {
            return;
        }

        currentWorkRemaining -= engineersAssigned;
        if (currentWorkRemaining <= 0) {
            currentWorkRemaining = WORK_FOR_POINT;

            final Ship playerShip = world.getPlayer().getShip();
            if (playerShip.getHull() < playerShip.getMaxHull()) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.hull_point_repaired"));
                playerShip.setHull(playerShip.getHull() + 1);
            }

            remainingPoints--;

            final int pointResCost = getRealPointCost(world);
            if (remainingPoints > 0 && world.getPlayer().getResourceUnits() < pointResCost) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_resources_hull"));
                remainingPoints = 0;
            }

            if (playerShip.getHull() == playerShip.getMaxHull() || remainingPoints == 0) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.repairs_completed"));
                world.getPlayer().getEngineeringState().addIdleEngineers(engineersAssigned);
                engineersAssigned = 0;
                remainingPoints = 0;
                return;
            }

            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() - pointResCost);
        }
    }

    public void cancel(World world) {
        world.getPlayer().getEngineeringState().setIdleEngineers(world.getPlayer().getEngineeringState().getIdleEngineers() + engineersAssigned);
        // return resources for all points
        world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + remainingPoints * getRealPointCost(world));
        remainingPoints = 0;
        resetProgress();
    }

    public int calcResCost(World world) {
        return remainingPoints * getRealPointCost(world);
    }

    public void resetProgress() {
        currentWorkRemaining = WORK_FOR_POINT;
    }
}

package ru.game.aurora.player.engineering;

import ru.game.aurora.application.GameLogger;
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

    public static final int POINT_RES_COST = 1;

    private int currentWorkRemaining = WORK_FOR_POINT;

    public void update(World world) {
        if (remainingPoints == 0 || engineersAssigned == 0) {
            return;
        }

        currentWorkRemaining -= engineersAssigned;
        if (currentWorkRemaining <= 0) {
            currentWorkRemaining = WORK_FOR_POINT;

            final Ship playerShip = world.getPlayer().getShip();
            if (playerShip.getHull() < playerShip.getMaxHull()) {
                GameLogger.getInstance().logMessage("Engineers finished repairing 1 point of hull damage");
                playerShip.setHull(playerShip.getHull() + 1);
            }

            remainingPoints--;

            if (remainingPoints > 0 && world.getPlayer().getResourceUnits() < POINT_RES_COST) {
                GameLogger.getInstance().logMessage("Not enough resources to continue repairs");
                remainingPoints = 0;
            }

            if (playerShip.getHull() == playerShip.getMaxHull() || remainingPoints == 0) {
                GameLogger.getInstance().logMessage("Hull repairs completed");
                world.getPlayer().getEngineeringState().addIdleEngineers(engineersAssigned);
                engineersAssigned = 0;
                remainingPoints = 0;
                return;
            }

            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() - POINT_RES_COST);
        }
    }

    public int calcResCost() {
        return remainingPoints * POINT_RES_COST;
    }

    public void resetProgress() {
        currentWorkRemaining = WORK_FOR_POINT;
    }
}

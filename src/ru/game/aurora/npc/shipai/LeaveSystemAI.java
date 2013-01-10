/**
 * User: jedi-philosopher
 * Date: 08.01.13
 * Time: 20:39
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

public class LeaveSystemAI implements NPCShipAI {

    private boolean isAway = false;

    private int dirX;

    private int dirY;

    public LeaveSystemAI() {
        dirY = CommonRandom.getRandom().nextInt(3) - 1;
        if (dirY == 0) {
            dirX = CommonRandom.getRandom().nextBoolean() ? 1 : -1;
        }
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        ship.setPos(ship.getX() + dirX, ship.getY() + dirY);
        isAway = !currentSystem.isInside(ship);
        if (isAway) {
            GameLogger.getInstance().logMessage("Ship '" + ship.getName() + "' has left star system");
        }
    }

    @Override
    public boolean isAlive() {
        return !isAway;
    }
}

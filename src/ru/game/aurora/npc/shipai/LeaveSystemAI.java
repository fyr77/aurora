/**
 * User: jedi-philosopher
 * Date: 08.01.13
 * Time: 20:39
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

public class LeaveSystemAI implements NPCShipAI {

    private static final long serialVersionUID = -5263145319771641988L;

    private boolean isAway = false;

    private int dirX;

    private final int dirY;

    public LeaveSystemAI() {
        dirY = CommonRandom.getRandom().nextInt(3) - 1;
        if (dirY == 0) {
            dirX = CommonRandom.getRandom().nextBoolean() ? 1 : -1;
        }
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        if ((dirX==0)&&(dirY==1)) {
            ship.moveDown();
        } else if ((dirX==0)&&(dirY==-1)) {
            ship.moveUp();
        } else if ((dirX==1)&&(dirY==0)) {
            ship.moveRight();
        } else if ((dirX==-1)&&(dirY==0)) {
            ship.moveLeft();
        }
        //obsolete
        //ship.move(dirX, dirY);
        isAway = !currentSystem.isInside(ship);
        if (isAway) {
            // escaped ship will report about reputation changes
            world.getReputation().merge(currentSystem.getReputation());
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.ship_warped"), ship.getName()));
        }
    }

    @Override
    public boolean isAlive() {
        return !isAway;
    }
}

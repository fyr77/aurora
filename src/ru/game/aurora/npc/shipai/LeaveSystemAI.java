/**
 * User: jedi-philosopher
 * Date: 08.01.13
 * Time: 20:39
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

public class LeaveSystemAI implements NPCShipAI {
    private int dirX;

    private int dirY;

    public LeaveSystemAI() {
        dirY = CommonRandom.getRandom().nextInt(3) - 1;
        if (dirY == 0) {
            dirX = CommonRandom.getRandom().nextBoolean() ? 1 : -1;
        }
    }

    @Override
    public void update(NPCShip ship, World world) {
        ship.setPos(ship.getX() + dirX, ship.getY() + dirY);
    }
}

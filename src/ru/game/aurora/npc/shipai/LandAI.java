/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.03.13
 * Time: 14:42
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Moves to a desired planet and lands on it
 */
public class LandAI implements NPCShipAI
{
    private static final long serialVersionUID = 1;

    private final Positionable target;

    private boolean isOverridable = true;

    private boolean hasLanded = false;

    public LandAI(Positionable target) {
        this.target = target;
    }

    private void moveToTargetHorizontal(NPCShip ship) {
        if (target.getX() < ship.getX()) {
            ship.moveLeft();
        } else if (target.getX() > ship.getX()) {
            ship.moveRight();
        }
    }

    private void moveToTargetVertical(NPCShip ship) {
        if (target.getY() < ship.getY()) {
            ship.moveUp();
        } else if (target.getY() > ship.getY()) {
            ship.moveDown();
        }
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        boolean canMoveX = ship.getX() != target.getX();
        boolean canMoveY = ship.getY() != target.getY();
        if (canMoveX && canMoveY) {
            // select randomly either x or y direction to move
            if (CommonRandom.getRandom().nextBoolean()) {
                moveToTargetHorizontal(ship);
            } else {
                moveToTargetVertical(ship);
            }
        } else if (canMoveX) {
            moveToTargetHorizontal(ship);
        } else if (canMoveY) {
            moveToTargetVertical(ship);
        } else {
            hasLanded = true;
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.ship_landed"), ship.getName()));
        }
    }

    @Override
    public boolean isAlive() {
        return !hasLanded;
    }

    @Override
    public boolean isOverridable() {
        return isOverridable;
    }

    public void setIsOverridable(boolean isOverridable) {
        this.isOverridable = isOverridable;
    }
}

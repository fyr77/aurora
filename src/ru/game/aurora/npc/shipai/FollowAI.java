package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by User on 24.06.2016.
 * Follows a given target
 */
public class FollowAI implements NPCShipAI {
    private static final long serialVersionUID = 1;

    protected final Positionable target;

    private boolean isOverridable = true;

    public FollowAI(Positionable target) {
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
        boolean canMoveX = Math.abs(ship.getX() - target.getX()) > 1;
        boolean canMoveY = Math.abs(ship.getY() - target.getY()) > 1;
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
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean isOverridable() {
        return isOverridable;
    }

    public void setIsOverridable(boolean isOverridable) {
        this.isOverridable = isOverridable;
    }
}

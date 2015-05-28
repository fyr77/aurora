/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.03.13
 * Time: 14:42
 */
package ru.game.aurora.npc.shipai;

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

    private boolean hasLanded = false;

    public LandAI(Positionable target) {
        this.target = target;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        if (target.getX() < ship.getX()) {
            ship.moveLeft();
        } else if (target.getX() > ship.getX()) {
            ship.moveRight();
        } else if (target.getY() < ship.getY()) {
            ship.moveUp();
        } else if (target.getY() > ship.getY()) {
            ship.moveDown();
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
        return true;
    }
}

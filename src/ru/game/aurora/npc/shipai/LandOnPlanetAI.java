/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.03.13
 * Time: 14:42
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Moves to a desired planet and lands on it
 */
public class LandOnPlanetAI implements NPCShipAI
{
    private static final long serialVersionUID = -8083999261802300585L;

    private BasePlanet target;

    private boolean hasLanded = false;

    public LandOnPlanetAI(BasePlanet target) {
        this.target = target;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        if (target.getGlobalX() < ship.getX()) {
            ship.move(-1, 0);
        } else if (target.getGlobalX() > ship.getX()) {
            ship.move(1, 0);
        } else if (target.getGlobalY() < ship.getY()) {
            ship.move(0, -1);
        } else if (target.getGlobalY() > ship.getY()) {
            ship.move(0, 1);
        } else {
            hasLanded = true;
            GameLogger.getInstance().logMessage(ship.getName() + " has landed on planet");
        }
    }

    @Override
    public boolean isAlive() {
        return !hasLanded;
    }
}

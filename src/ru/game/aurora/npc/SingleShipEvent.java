/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 13:53
 */
package ru.game.aurora.npc;


import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Event when player has a fixed chance of meeting specific ship when entering arbitrary star system
 */
public class SingleShipEvent implements GameEventListener {

    private double chance;

    private NPCShip ship;

    public SingleShipEvent(double chance, NPCShip ship) {
        this.chance = chance;
        this.ship = ship;
    }

    @Override
    public void onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (CommonRandom.getRandom().nextDouble() < chance) {
            ship.setPos(CommonRandom.getRandom().nextInt(2 * ss.getRadius()) - ss.getRadius(), CommonRandom.getRandom().nextInt(2 * ss.getRadius()) - ss.getRadius());
            ss.getShips().add(ship);
            ship = null;
        }
    }

    @Override
    public boolean isAlive() {
        return ship != null;
    }
}
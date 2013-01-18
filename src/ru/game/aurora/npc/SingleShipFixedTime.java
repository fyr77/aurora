/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 17:13
 */
package ru.game.aurora.npc;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates given alien ship when player enters Xth starsystem for first time
 */
public class SingleShipFixedTime implements GameEventListener {
    private int x;

    private int count = 0;

    private NPCShip ship;

    public SingleShipFixedTime(int x, NPCShip ship) {
        this.x = x;
        this.ship = ship;
    }

    @Override
    public void onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (!ss.isVisited() && ++count == x) {
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

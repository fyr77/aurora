/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:44
 */
package ru.game.aurora.world.space;

import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class BaseSpaceRoom implements Room
{
    protected Player player;

    @Override
    public void enter(Player player) {
        this.player = player;
    }

    @Override
    public void update(JGEngine engine, World world) {
        double x = player.getShip().getLastX();
        double y = player.getShip().getLastY();

        if (engine.getKey(JGEngineInterface.KeyUp) && y > 0) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyDown) && y < engine.getHeight()) {
            y ++;
            world.setUpdatedThisFrame(true);
        }

        if (engine.getKey(JGEngineInterface.KeyLeft) && x > 0) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyRight) && x < engine.getWidth()) {
            x++;
            world.setUpdatedThisFrame(true);
        }

        player.getShip().setPos(x, y);
    }

    @Override
    public void draw(JGEngine engine) {
        player.getShip().draw(engine);
    }
}

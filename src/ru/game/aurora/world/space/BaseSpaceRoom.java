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

public class BaseSpaceRoom implements Room
{
    protected Player player;

    @Override
    public void enter(Player player) {
        this.player = player;
    }

    @Override
    public void update(JGEngine engine) {
        double x = player.getShip().getLastX();
        double y = player.getShip().getLastY();

        if (engine.getKey(JGEngineInterface.KeyUp) && y > 0) {
            y-= engine.tileHeight();
        }
        if (engine.getKey(JGEngineInterface.KeyDown) && y < engine.getHeight()) {
            y += engine.tileHeight();
        }

        if (engine.getKey(JGEngineInterface.KeyLeft) && x > 0) {
            x -= engine.tileWidth();
        }
        if (engine.getKey(JGEngineInterface.KeyRight) && x < engine.getWidth()) {
            x += engine.tileWidth();
        }

        player.getShip().setPos(x, y);
    }

    @Override
    public void draw(JGEngine engine) {
        player.getShip().draw(engine);
    }
}

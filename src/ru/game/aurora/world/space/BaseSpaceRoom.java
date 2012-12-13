/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:44
 */
package ru.game.aurora.world.space;

import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class BaseSpaceRoom implements Room {
    protected Player player;

    @Override
    public void enter(World world) {
        this.player = world.getPlayer();
    }

    @Override
    public void update(JGEngine engine, World world) {
        int x = player.getShip().getX();
        int y = player.getShip().getY();

        if (engine.getKey(JGEngineInterface.KeyUp)) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyDown)) {
            y++;
            world.setUpdatedThisFrame(true);
        }

        if (engine.getKey(JGEngineInterface.KeyLeft)) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyRight)) {
            x++;
            world.setUpdatedThisFrame(true);
        }

        player.getShip().setPos(x, y);
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        player.getShip().draw(engine, camera);
    }
}

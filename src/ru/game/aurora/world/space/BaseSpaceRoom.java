/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:44
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

public class BaseSpaceRoom implements Room {
    private static final long serialVersionUID = 1L;

    protected Player player;

    @Override
    public void enter(World world) {
        this.player = world.getPlayer();
    }

    @Override
    public void update(GameContainer container, World world) {
        if (!player.getShip().nowMoving()) {
            if (container.getInput().isKeyPressed(Input.KEY_UP)) {
                player.getShip().moveUp();
                world.setUpdatedThisFrame(true);
            }
            if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
                player.getShip().moveDown();
                world.setUpdatedThisFrame(true);
            }
            if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
                player.getShip().moveLeft();
                world.setUpdatedThisFrame(true);
            }
            if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
                player.getShip().moveRight();
                world.setUpdatedThisFrame(true);
            }
        }
        player.getShip().update(container, world);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        player.getShip().draw(container, g, camera);
    }
}

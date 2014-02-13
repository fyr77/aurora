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

    private int xClick = 0, yClick = 0;

    @Override
    public void enter(World world) {
        this.player = world.getPlayer();
    }

    @Override
    public void update(GameContainer container, World world) {
        Camera myCamera = world.getCamera();
        if (container.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
            xClick = container.getInput().getMouseX() - myCamera.getViewportX();
            yClick = container.getInput().getMouseY() - myCamera.getViewportY();
        }
        if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            myCamera.setViewportX(container.getInput().getMouseX() - xClick);
            myCamera.setViewportY(container.getInput().getMouseY() - yClick);
        }
        if (!player.getShip().nowMoving()) {
            if (container.getInput().isKeyPressed(Input.KEY_UP)) {
                player.getShip().moveUp();
                shipMove(world);
            }
            if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
                player.getShip().moveDown();
                shipMove(world);
            }
            if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
                player.getShip().moveLeft();
                shipMove(world);
            }
            if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
                player.getShip().moveRight();
                shipMove(world);
            }
        }
        player.getShip().update(container, world);
    }

    private void shipMove(World world) {
        world.getCamera().resetViewPort();
        world.setUpdatedThisFrame(true);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        player.getShip().draw(container, g, camera);
    }
}

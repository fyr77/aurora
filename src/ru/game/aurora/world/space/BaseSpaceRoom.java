/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:44
 */
package ru.game.aurora.world.space;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
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
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        int x = player.getShip().getX();
        int y = player.getShip().getY();

        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            y++;
            world.setUpdatedThisFrame(true);
        }

        if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            x++;
            world.setUpdatedThisFrame(true);
        }

        player.getShip().setPos(x, y);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        player.getShip().draw(container, g, camera);
    }
}

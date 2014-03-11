/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:14
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Positionable;

import java.io.Serializable;

public interface GalaxyMapObject extends Serializable, Positionable {
    /**
     * Draws tile for this object on global map
     */
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY);

    /**
     * Returns true if this galaxy map object is a room and can be entered
     */
    public boolean canBeEntered();

    /**
     * Processes player ship entering this tile
     */
    public void processCollision(GameContainer container, Player player);

    public String getName();
}

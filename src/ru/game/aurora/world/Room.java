/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:06
 */
package ru.game.aurora.world;

import java.io.Serializable;

public interface Room extends Updatable, Serializable, IDrawable {
    /**
     * Called when player enters this room from some other room. Should reset room state (e.g. put player on a starting position) and open gui.
     */
    public void enter(World world);

    /**
     * Called when player returns to this room from internal room. Should not reset room state, only open gui; Also called when game is loaded
     */
    public void returnTo(World world);

    public ITileMap getMap();

}

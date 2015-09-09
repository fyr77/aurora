package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 25.11.13
 * Time: 22:51
 *
 * Base interface for objects that should be updated on each game tick
 */
public interface Updatable {
    /**
     * Called each frame to update object state
     *
     * @param container Application state object that provides access to input, window and other system stuff
     * @param world     Game state object
     */
    void update(GameContainer container, World world);

}

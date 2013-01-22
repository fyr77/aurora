/**
 * User: jedi-philosopher
 * Date: 23.12.12
 * Time: 21:26
 */
package ru.game.aurora.world;

import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;

public interface GameEventListener extends Serializable {
    /**
     * Called when player enters star system, before it is shown
     */
    public void onPlayerEnterStarSystem(World world, StarSystem ss);

    /**
     * Returns false if this event will never happen again and should be disposed
     */
    public boolean isAlive();
}

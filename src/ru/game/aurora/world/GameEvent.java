package ru.game.aurora.world;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.12.12
 * Time: 15:33
 */

import ru.game.aurora.world.space.StarSystem;

/**
 * Events may happen when player enters star system
 * They may add some NPC ships to this system, temporary anomalies etc
 */
public interface GameEvent {
    /**
     * Called when player enters star system, before it is shown
     */
    public void onPlayerEnterStarSystem(World world, StarSystem ss);

    /**
     * Returns false if this event will never happen again and should be disposed
     */
    public boolean isAlive();
}

/**
 * User: jedi-philosopher
 * Date: 23.12.12
 * Time: 21:26
 */
package ru.game.aurora.world;

import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;

public abstract class GameEventListener implements Serializable {

    private static final long serialVersionUID = -4189717114829655272L;

    protected boolean isAlive = true;

    /**
     * Called when player enters star system, before it is shown
     */
    public void onPlayerEnterStarSystem(World world, StarSystem ss) {
        // nothing
    }

    public void onTurnEnded(World world) {
        // nothing
    }

    public void onReturnToEarth(World world) {
        // nothing
    }

    public void onPlayerShipDamaged(World world) {

    }

    public void onCrewChanged(World world) {

    }

    /**
     * Returns false if this event will never happen again and should be disposed
     */
    public boolean isAlive() {
        return isAlive;
    }
}

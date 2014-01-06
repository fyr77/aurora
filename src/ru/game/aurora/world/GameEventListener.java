/**
 * User: jedi-philosopher
 * Date: 23.12.12
 * Time: 21:26
 */
package ru.game.aurora.world;

import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for game event listeners.
 * All methods return boolean. A method in a subclass should return true, if it has made changes to the world state.
 * Each listener can define a set of tags for itself.
 * If a game event happen, only one listener for any tag should be called.
 * E.g. if there are multiple listeners used to add encounters when player enter star system, only one encounter can be created at a time
 */
public abstract class GameEventListener implements Serializable {

    public static enum EventGroup {
        ENCOUNTER_SPAWN,
    }

    private static final long serialVersionUID = -4189717114829655272L;

    protected boolean isAlive = true;

    protected Set<EventGroup> groups;

    /**
     * Called when player enters star system, before it is shown
     */
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        return false;
    }

    public boolean onPlayerContactedOtherShip(World world, SpaceObject ship) {
        return false;
    }

    public boolean onTurnEnded(World world) {
        return false;
    }

    public boolean onReturnToEarth(World world) {
        return false;
    }

    public boolean onPlayerShipDamaged(World world) {
        return false;
    }

    public boolean onCrewChanged(World world) {
        return false;
    }

    public boolean onPlayerEnteredDungeon(World world, Dungeon dungeon) {
        return false;
    }

    /**
     * Returns false if this event will never happen again and should be disposed
     */
    public boolean isAlive() {
        return isAlive;
    }

    public Set<EventGroup> getGroups() {
        if (groups == null) {
            return Collections.emptySet();
        }
        return groups;
    }

    public void setGroups(EventGroup... g) {
        this.groups = new HashSet<>();
        Collections.addAll(this.groups, g);
    }
}

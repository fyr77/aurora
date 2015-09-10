/**
 * User: jedi-philosopher
 * Date: 23.12.12
 * Time: 21:26
 */
package ru.game.aurora.world;

import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMapObject;
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

    private static final long serialVersionUID = -4189717114829655272L;
    protected boolean isAlive = true;
    protected Set<EventGroup> groups;

    /**
     * Called when player enters star system, before it is shown
     */
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        return false;
    }

    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        return false;
    }

    public boolean onPlayerContactedOtherShip(World world, GameObject ship) {
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

    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        return false;
    }

    public boolean onPlayerLeftPlanet(World world, Planet planet) {
        return false;
    }

    // should be called after hp is reduced, so in case target was killed its isAlive() should return false here
    public boolean onGameObjectAttacked(World world, GameObject attacker, GameObject target, int damage) {return false;}

    public boolean onEarthUpgradeUnlocked(World world, EarthUpgrade upgrade) {
        return false;
    }

    /**
     * Changed amount of item in ships inventory.
     * For items collected by landing party this method is called either when dumping items on shuttle
     * or when party returns to ship
     */
    public boolean onItemAmountChanged(World world, InventoryItem item, int amount) {
        return false;
    }

    /**
     * Returns false if this event will never happen again and should be disposed
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Event listeners are often used to create quests and encounters.
     * Quests usually have a set of quest locations in different star systems.
     * Player should see such star systems marked on a global map, and as well see a quest-related text
     * message when hovering over them with mouse.
     * This method checks if this quest has something to do with the star system, and if that is true - returns a related localized
     * message string.
     */
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        return null;
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

    public enum EventGroup {
        ENCOUNTER_SPAWN,
    }
}

/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:16
 */
package ru.game.aurora.world;

import org.newdawn.slick.Image;
import ru.game.aurora.npc.Faction;

import java.io.Serializable;

/**
 * Base interface for game objects like ships, monsters, interactable items
 */
public interface GameObject extends Serializable, Updatable, IMovable, IDrawable {

    /**
     * Get a single image of this object.
     * If this object is an animation - should retrun a single frame of it
     */
    Image getImage();

    /**
     * Get a name of this object
     */
    String getName();

    /**
     * Return true if this object can be interacted with. Like hailing an alien ship or pressing a button.
     * If it is true then when player stands on same tile with this object,
     * an 'interact' button will appear.
     * @param world
     */
    boolean canBeInteracted(World world);

    /**
     * Player is standing on the same tile and has pressed an 'interact' button
     */
    boolean interact(World world);

    /**
     * Get a text for interact button. Like 'Hail', 'Use' or 'Enter'
     */
    String getInteractMessage();

    /**
     * If set to true, this object can be attacked. It will be targeted by hostile AI objects, and it can be targeted by
     * player landing party or ship
     */
    boolean canBeAttacked();

    /**
     * Called when this object is attacked.
     *
     * @param world    World state
     * @param attacker Game object that has performed an attack
     * @param damaged  Total damage done
     */
    void onAttack(World world, GameObject attacker, int damaged);

    /**
     * Indicates if this object is alive.
     * Objects that return false are immediately removed from the game
     */
    boolean isAlive();

    /**
     * If this object can be scanned, this is text that will appear on the scan screen
     */
    String getScanDescription(World world);

    /**
     * ScanGroup will define by what color this object will be marked on planet scan screen or planet map.
     */
    ScanGroup getScanGroup();

    /**
     * Faction (like alien race) to which this object belongs. Factions define object relations like hostility.
     */
    Faction getFaction();

    /**
     * Objects with higher value are drawn on top of objects with lower one when located on same tile
     */
    int getDrawOrder();
}

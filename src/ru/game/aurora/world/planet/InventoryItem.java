/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 17:42
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.Image;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.World;

import java.io.Serializable;
// todo: make an abstract base class
public interface InventoryItem extends Serializable
{
    String getId();

    String getName();

    String getDescription();

    Image getImage();

    /**
     * Price for selling or buying this item from a merchant
     */
    double getPrice();

    /**
     * Called when player receives this item (buys, picks up etc)
     */
    void onReceived(World world, int amount);

    /**
     * Called when player looses this item (sells it, drops it etc)
     */
    void onLost(World world, int amount);

    /**
     * If this item is automatically transfered from landing party to a ship
     */
    boolean isDumpable();

    /**
     * If returns true then a 'use' button will be showed in inventory screen near this item
     */
    boolean isUsable();

    /**
     * If true, player will not be able to buy second one
     */
    boolean isUnique();

    int getWeight();

    /**
     * Checks that this item can be sold to a merchant of this faction. Will appear in the trade screen only if returned
     * true
     */
    boolean canBeSoldTo(World world, Faction faction);
}

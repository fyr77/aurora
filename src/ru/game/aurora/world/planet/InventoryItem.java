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
    public String getName();

    public String getDescription();

    public Image getImage();

    public double getPrice();

    public void onReceived(World world, int amount);

    public void onLost(World world, int amount);

    /**
     * If this item is automatically transfered from landing party to a ship
     */
    public boolean isDumpable();

    public boolean isUsable();

    public boolean isUnique();

    public int getWeight();

    public boolean canBeSoldTo(Faction faction);
}

/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 17:42
 */
package ru.game.aurora.world.planet;

import ru.game.aurora.world.World;

import java.io.Serializable;

public interface InventoryItem extends Serializable {
    public String getName();

    public void onReturnToShip(World world, int amount);
}

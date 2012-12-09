/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 17:42
 */
package ru.game.aurora.world.planet;

import ru.game.aurora.world.World;

public interface InventoryItem
{
    public String getName();

    public void onReturnToShip(World world, int amount);
}

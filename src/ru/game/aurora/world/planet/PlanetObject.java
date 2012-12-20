/**
 * User: jedi-philosopher
 * Date: 08.12.12
 * Time: 0:19
 */
package ru.game.aurora.world.planet;

import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;

/**
 * Base interface for objects that are located on planet surface
 */
public interface PlanetObject extends Positionable, GameObject {
    public boolean canBePickedUp();

    public boolean canBeShotAt();

    public void onShotAt(int damage);

    public void onPickedUp(World world);

    public boolean isAlive();

    public String getName();

    /**
     * Called when landing party is standing on top of this object
     * Print some hint like 'press enter to pick up'
     */
    public void printStatusInfo();
}

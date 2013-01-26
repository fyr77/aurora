/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 13:48
 */
package ru.game.aurora.world.space;

import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;

/**
 * Base class for stuff that flies in space, everything except planets.
 */
public interface SpaceObject extends GameObject, Positionable {
    public void onContact(World world);

    public void onAttack(World world, SpaceObject attacker, int dmg);

    public boolean isAlive();

    public String getName();
}

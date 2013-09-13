/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.09.13
 * Time: 14:09
 */
package ru.game.aurora.world.dungeon;

import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Interface for objects that define success for a dungeon
 */
public interface IVictoryCondition extends Serializable
{
    public boolean isSatisfied(World world);
}

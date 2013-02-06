/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:04
 */
package ru.game.aurora.world.generation;

import ru.game.aurora.world.World;

/**
 * Base interface for classes that participate in initial world generation.
 * Each subclass aggregates generation of some connected part of game universe, e.g. a quest line or an alien race
 */
public interface WorldGeneratorPart
{
    public void updateWorld(World world);
}

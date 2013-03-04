/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 14:37
 */

package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;

public class RoguesGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = -8911801330633122269L;

    @Override
    public void updateWorld(World world) {
        AlienRace rogueRace = new AlienRace("Rogues", null, 5, null);

        world.getRaces().put(rogueRace.getName(), rogueRace);
    }
}

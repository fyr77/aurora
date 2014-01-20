package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.13
 * Time: 18:17
 */
public class ZorsanGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 1083992211652099884L;

    @Override
    public void updateWorld(World world) {
        AlienRace race = new AlienRace("Zorsan", "zorsan_scout", Dialog.loadFromFile("dialogs/zorsan_main.json"));
        world.getRaces().put(race.getName(), race);
    }
}

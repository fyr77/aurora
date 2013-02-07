/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:12
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.npc.StandartAlienShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates Klisk alien race
 */
public class KliskGenerator implements WorldGeneratorPart
{
    @Override
    public void updateWorld(World world) {
        AlienRace kliskRace = new AlienRace("Klisk", "klisk_ship", 8, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/klisk_default_dialog.json")));
        StarSystem kliskHomeworld = HomeworldGenerator.generateKliskHomeworld(5, 5, kliskRace);
        kliskRace.setHomeworld(kliskHomeworld);

        world.addListener(new StandartAlienShipEvent(kliskRace));
        world.getGalaxyMap().getObjects().add(kliskHomeworld);
        world.getGalaxyMap().setTileAt(5, 5, world.getGalaxyMap().getObjects().size() - 1);

        world.getRaces().put(kliskRace.getName(), kliskRace);
    }
}

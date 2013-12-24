/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:12
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Creates Klisk alien race
 */
public class KliskGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -6983386879381885934L;

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile("dialogs/klisk_1.json");
        final AlienRace kliskRace = new AlienRace("Klisk", "klisk_ship", mainDialog);
        mainDialog.setListener(new DialogListener() {
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

                switch (returnCode) {
                    case 2:
                    case 4:
                        world.getGlobalVariables().put("klisk.planet_info", true);
                        break;
                    case 10:
                        // decided to take time
                        return;
                }

                Dialog newDefaultDialog = Dialog.loadFromFile("dialogs/klisk_main.json");
                newDefaultDialog.setListener(new KliskMainDialogListener(kliskRace));
                kliskRace.setDefaultDialog(newDefaultDialog);
            }
        });

        StarSystem kliskHomeworld = HomeworldGenerator.generateKliskHomeworld(world, 15, 15, kliskRace);
        kliskRace.setHomeworld(kliskHomeworld);

        world.addListener(new StandardAlienShipEvent(kliskRace));
        world.getGalaxyMap().getObjects().add(kliskHomeworld);
        world.getGalaxyMap().setTileAt(15, 15, world.getGalaxyMap().getObjects().size() - 1);

        world.getRaces().put(kliskRace.getName(), kliskRace);
    }
}

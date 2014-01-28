/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:12
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Creates Klisk alien race
 */
public class KliskGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -6983386879381885934L;

    public static final String NAME = "Klisk";

    // ship IDs used in factory generation
    public static final int DEFAULT_SHIP = 0;

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile("dialogs/klisk_1.json");
        final AlienRace kliskRace = new AlienRace(NAME, "klisk_ship", mainDialog);
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

                if (flags.containsKey("klisk.knows_about_path_philosophy")) {
                    world.getGlobalVariables().put("klisk.knows_about_path_philosophy", true);
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

        kliskRace.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = 5473066320214324094L;

            @Override
            public NPCShip createShip(int shipType) {
                if (shipType == DEFAULT_SHIP) {
                    NPCShip ship = new NPCShip(0, 0, "klisk_ship", kliskRace, null, "Klisk Ship");
                    ship.setHp(15);
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_small_laser"), ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
                    return ship;
                }

                throw new IllegalArgumentException("Klisk race does not define ship of type " + shipType);
            }
        });
    }
}

package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.10.13
 * Time: 17:12
 */
public class BorkGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = 3878128826545719756L;

    public static final String NAME = "Bork";

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/encounters/swarm_first_dialog.json"));
        final AlienRace borkRace = new AlienRace(NAME, "bork_ship", mainDialog);

        borkRace.setDefaultFactory(new NPCShipFactory() {
            @Override
            public NPCShip createShip() {
                NPCShip ship = new NPCShip(0, 0, "bork_ship", borkRace, null, "Bork ship");
                ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("simple_cannon"), ResourceManager.getInstance().getWeapons().getEntity("bork_missiles"));
                ship.setHp(5);
                return ship;
            }
        });


        mainDialog.setListener(new DialogListener() {

            private static final long serialVersionUID = -4476192487724362451L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                // after first dialog swarm becomes hostile
                borkRace.setRelation(world.getPlayer().getShip().getRace(), 0);
            }
        });

        world.addListener(new BorkShipGenerator(0.5, 3, null, borkRace.getDefaultFactory(), 10));

        world.getRaces().put(borkRace.getName(), borkRace);
    }
}

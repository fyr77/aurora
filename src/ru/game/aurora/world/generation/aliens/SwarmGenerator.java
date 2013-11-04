package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.10.13
 * Time: 17:12
 */
public class SwarmGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 3878128826545719756L;

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/encounters/swarm_first_dialog.json"));
        final AlienRace swarmRace = new AlienRace("Swarm", "swarm_ship", mainDialog);

        swarmRace.setDefaultFactory(new NPCShipFactory() {
            @Override
            public NPCShip createShip() {
                NPCShip ship = new NPCShip(0, 0, "swarm_ship", swarmRace, null, "Swarm ship");
                ship.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("simple_cannon"), StarshipWeapon.MOUNT_ALL));
                ship.setHp(3);
                return ship;
            }
        });


        mainDialog.setListener(new DialogListener() {

            private static final long serialVersionUID = -4476192487724362451L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode) {
                // after first dialog swarm becomes hostile
                swarmRace.setRelation(world.getPlayer().getShip().getRace(), 0);
            }
        });

        world.addListener(new SwarmShipGenerator(0.5, 3, null, swarmRace.getDefaultFactory(), 10));

        world.getRaces().put(swarmRace.getName(), swarmRace);
    }
}

package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;

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
        mainDialog.setListener(new DialogListener() {
            @Override
            public void onDialogEnded(World world, int returnCode) {
                // after first dialog swarm becomes hostile
                swarmRace.setRelation(world.getPlayer().getShip().getRace(), 0);
            }
        });

        world.addListener(new SwarmShipGenerator(0.5, 3, null, swarmRace.getDefaultFactory(), 10));

        world.getRaces().put(swarmRace.getName(), swarmRace);
    }
}

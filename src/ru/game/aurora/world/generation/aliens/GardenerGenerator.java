/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:14
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipFixedTime;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.GardenersShip;

/**
 * Creates Gardener alien race
 */
public class GardenerGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -2142318077060757284L;

    @Override
    public void updateWorld(World world) {
        final Dialog dialog = Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/gardener_default_dialog.json"));
        AlienRace gardenerRace = new AlienRace("Gardeners", "gardener_ship", 8, dialog);
        final GardenersShip gardenerShip = new GardenersShip(0, 0, gardenerRace.getShipSprite(), gardenerRace, null, "Sequoia");
        gardenerShip.setAi(null);
        dialog.setListener(new DialogListener() {
            private static final long serialVersionUID = -743686006546787750L;

            @Override
            public void onDialogEnded(World world, int returnCode) {
                gardenerShip.warpAwayNextTurn();
            }
        });
        gardenerShip.setCaptain(new NPC(dialog));
        SingleShipFixedTime listener = new SingleShipFixedTime(1, gardenerShip, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/gardener_ship_detected.json")));
        listener.setGroups(GameEventListener.EventGroup.ENCOUNTER_SPAWN);
        world.addListener(listener);
        world.getRaces().put(gardenerRace.getName(), gardenerRace);

        world.getGlobalVariables().put("gardeners.first_warp", true);
    }
}

package ru.game.aurora.world.generation.aliens.bork;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.Resources;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.DamagedRoguesScoutEventGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 17.02.14
 */


public class BorkShipDialogListener implements DialogListener {
    private static final long serialVersionUID = -338473827627208845L;

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 105 || returnCode == 110) {
            // this is a damaged rogue scout event
            world.getGlobalVariables().put("rogues.damage_scout_result", "sold");
            for (GameEventListener listener : world.getListeners()) {
                if (listener instanceof DamagedRoguesScoutEventGenerator.MeetDamagedRogueEvent) {
                    ((DamagedRoguesScoutEventGenerator.MeetDamagedRogueEvent) listener).remove(world);
                }
            }

            world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
            world.getPlayer().changeResource(world, Resources.CREDITS, returnCode - 100);
        }
    }
}

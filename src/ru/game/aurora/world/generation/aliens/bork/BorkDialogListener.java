package ru.game.aurora.world.generation.aliens.bork;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.EarthInvasionGenerator;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.03.14
 * Time: 22:07
 */
public class BorkDialogListener implements DialogListener {
    private static final long serialVersionUID = -6184207223388040791L;

    private void removeBlockade(World world) {
        world.getPlayer().getJournal().addQuestEntries("bork_blockade", "withdraw");
        world.getGlobalVariables().put("bork_blockade.result", "withdraw");
        StarSystem ss = world.getRaces().get(HumanityGenerator.NAME).getHomeworld();
        for (Iterator<SpaceObject> iter = ss.getShips().iterator(); iter.hasNext(); ) {
            SpaceObject so = iter.next();
            if (so instanceof EarthInvasionGenerator.BorkBlockadeShip) {
                iter.remove();
            }
        }

    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (flags.containsKey("bork_blockade.withdraw")) {
            removeBlockade(world);
        }

        if (flags.containsKey("bork.war_help")) {
            world.getGlobalVariables().put("bork.war_help", true);
        }
    }
}

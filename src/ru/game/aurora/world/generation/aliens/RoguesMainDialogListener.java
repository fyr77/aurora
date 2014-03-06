package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.11.13
 * Time: 16:57
 */
public class RoguesMainDialogListener implements DialogListener {
    private static final long serialVersionUID = 1;

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 100) {
            // pay fine
            int fine = (Integer) world.getGlobalVariables().get("rogues.fine");
            if (world.getPlayer().getCredits() < fine) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_credits"));
                return;
            }

            world.getPlayer().changeCredits(world, -fine);
            world.getGlobalVariables().remove("rogues.fine");
        }

        if (flags.containsKey("rogues_altar.withdraw")) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("rogues_altar_withdraw", "news"));
            world.getPlayer().getJournal().addQuestEntries("rogues_altar", "withdraw");
            StarSystem humanityHomeworld = world.getRaces().get(HumanityGenerator.NAME).getHomeworld();

            Planet moon = (Planet) humanityHomeworld.getPlanets()[2].getSatellites().get(0);
            for (PlanetObject po : moon.getPlanetObjects()) {
                if (po instanceof DungeonEntrance) {
                    ((DungeonEntrance) po).setLocked("rogues_altar_abandoned");
                }
            }
        }

        if (flags.containsKey("zorsan.quest") && !world.getGlobalVariables().containsKey("zorsan.war_preparations")) {
            ZorsanGenerator.addWarDataDrop();
        }

        if (flags.containsKey("war_help")) {
            world.getGlobalVariables().put("rogues.war_help", true);
        }
    }
}

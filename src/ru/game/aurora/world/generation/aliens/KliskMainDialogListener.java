/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.03.13
 * Time: 12:58
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.research.projects.AlienRaceResearch;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.JournalEntry;

import java.util.Map;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener {
    private static final long serialVersionUID = -2351530187782245878L;

    private AlienRace kliskRace;

    public KliskMainDialogListener(AlienRace kliskRace) {
        this.kliskRace = kliskRace;
    }


    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        AlienRaceResearch research;
        switch (returnCode) {
            case 1:
            case 2:
                GameLogger.getInstance().logMessage("Trade coming soon");
                break;

// initial quest - trading of race information

            case 500:
                world.getPlayer().changeCredits(world, 7);
                break;

///////
        }

        if (returnCode >= 100 && returnCode <= 500) {

            if (flags.containsKey("klisk.bork_info")) {
                research = new AlienRaceResearch("bork", world.getRaces().get("Bork"), new JournalEntry("bork", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.klisk_info")) {
                research = new AlienRaceResearch("klisk", world.getRaces().get("Klisk"), new JournalEntry("klisk", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.rogues_info")) {
                research = new AlienRaceResearch("rogues", world.getRaces().get("Rogues"), new JournalEntry("rogues", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.zorsan_info")) {
                research = new AlienRaceResearch("zorsan", world.getRaces().get("Zorsan"), new JournalEntry("zorsan", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }


            if (flags.containsKey("planet_info")) {
                if (flags.containsKey("base_info")) {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_minimal");
                } else {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_max");
                }
            } else {
                if (flags.containsKey("base_info")) {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_minimal_no_coords");
                } else {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_max_no_coords");
                }
            }
            world.getGlobalVariables().put("klisk.coordinates_traded", true);
        }
    }
}

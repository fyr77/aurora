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
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.quest.EarthInvasionGenerator;
import ru.game.aurora.world.quest.JournalEntry;

import java.util.Map;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener {
    private static final long serialVersionUID = 1L;

    public KliskMainDialogListener() {
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
                if (flags.containsKey("base_info")) {
                    world.getPlayer().changeCredits(world, 5);
                } else {
                    world.getPlayer().changeCredits(world, 10);
                }
                break;

///////
        }

        if (flags.containsKey("planet_info")) {
            world.getPlayer().getJournal().addQuestEntries("colony_search", "klisk_help");
            world.getGlobalVariables().put("klisk.planet_info", "1");
            flags.remove("planet_info");
        }

        if (returnCode >= 100 && returnCode <= 500) {
            if (flags.containsKey("klisk.discount")) {
                world.getGlobalVariables().put("klisk.discount", 10);
            }
            if (flags.containsKey("klisk.bork_info")) {
                research = new AlienRaceResearch("bork", (AlienRace) world.getFactions().get(BorkGenerator.NAME), new JournalEntry("bork", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.klisk_info")) {
                research = new AlienRaceResearch("klisk", (AlienRace) world.getFactions().get(KliskGenerator.NAME), new JournalEntry("klisk", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.rogues_info")) {
                research = new AlienRaceResearch("rogues", (AlienRace) world.getFactions().get(RoguesGenerator.NAME), new JournalEntry("rogues", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }
            if (flags.containsKey("klisk.zorsan_info")) {
                research = new AlienRaceResearch("zorsan", (AlienRace) world.getFactions().get(ZorsanGenerator.NAME), new JournalEntry("zorsan", "main"));
                world.getPlayer().getResearchState().addNewAvailableProject(research);
            }


            if (flags.containsKey("planet_info") || world.getGlobalVariables().containsKey("klisk.planet_info")) {
                if (flags.containsKey("base_info")) {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_minimal");
                } else {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_max");
                    new EarthInvasionGenerator().updateWorld(world);
                }
            } else {
                if (flags.containsKey("base_info")) {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_minimal_no_coords");
                } else {
                    world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_max_no_coords");
                    new EarthInvasionGenerator().updateWorld(world);
                }
            }
            world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("klisk_homeworlds");
            world.getGlobalVariables().put("klisk.coordinates_traded", true);

            ((AlienRace) world.getFactions().get(ZorsanGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(BorkGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(RoguesGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(KliskGenerator.NAME)).setKnown(true);
        }

        if (returnCode == 101) {
            world.getPlayer().changeCredits(world, -6);
            world.getGlobalVariables().put("energy_sphere.started", 1);
            world.getPlayer().getJournal().addQuestEntries("energy_sphere", "klisk");
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/energy_sphere_communication.json"));
        }
    }
}

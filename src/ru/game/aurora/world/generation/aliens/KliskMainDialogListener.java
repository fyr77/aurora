/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.03.13
 * Time: 12:58
 */
package ru.game.aurora.world.generation.aliens;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.TradeScreenController;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.research.ResearchSellItem;
import ru.game.aurora.player.research.projects.AlienRaceResearch;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.quest.EarthInvasionGenerator;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.quest.JournalEntry;

import java.util.Map;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener {
    private static final long serialVersionUID = 1L;

    public KliskMainDialogListener() {
    }

    private Multiset<InventoryItem> defaultTradeInventory;

    public Multiset<InventoryItem> getDefaultTradeInventory() {
        if (defaultTradeInventory == null) {
            defaultTradeInventory = HashMultiset.create();
            defaultTradeInventory.add(new KliskTradeItems.AdvancedRadarsSellItem());
            defaultTradeInventory.add(new KliskTradeItems.AlienAlloysSellItem());
            defaultTradeInventory.add(new KliskTradeItems.ResourceSellItem());
            defaultTradeInventory.add(new KliskTradeItems.ScienceTheorySellItem("math"));
            defaultTradeInventory.add(new KliskTradeItems.ScienceTheorySellItem("physics"));
            defaultTradeInventory.add(new KliskTradeItems.ScienceTheorySellItem("biology"));
            defaultTradeInventory.add(new KliskTradeItems.ScienceTheorySellItem("chemistry"));
            defaultTradeInventory.add(new ResearchSellItem("energy_concentration", "technology_research", 25, true));
        }
        return defaultTradeInventory;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        AlienRaceResearch research;
        switch (returnCode) {
            case 1:
            case 2:
                TradeScreenController.openTrade("klisk_dialog", getDefaultTradeInventory(), world.getFactions().get(KliskGenerator.NAME));
                break;

// initial quest - trading of race information

            case 500:
                if (flags.containsKey("base_info")) {
                    world.getPlayer().changeResource(world, Resources.CREDITS, 5);
                } else {
                    world.getPlayer().changeResource(world, Resources.CREDITS, 10);
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
            world.getPlayer().getJournal().questCompleted("last_beacon", "klisk_homeworlds", "end");
            world.getGlobalVariables().put("klisk.coordinates_traded", true);

            ((AlienRace) world.getFactions().get(ZorsanGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(BorkGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(RoguesGenerator.NAME)).setKnown(true);
            ((AlienRace) world.getFactions().get(KliskGenerator.NAME)).setKnown(true);
        }

        if (returnCode == 101) {
            world.getPlayer().changeResource(world, Resources.CREDITS, -6);
            world.getGlobalVariables().put("energy_sphere.started", 1);
            world.getPlayer().getJournal().addQuestEntries("energy_sphere", "klisk");
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/energy_sphere_communication.json"));
        }
    }
}

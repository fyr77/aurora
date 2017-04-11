package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Map;

/**
 * Created by di Grigio on 01.04.2017.
 * Main line Act2, War Line, quest 1 - Explore
 */
public class WarLineExploreQuest extends GameEventListener implements WorldGeneratorPart {

    private static final long serialVersionUID = -41897171144322272L;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WarLineExploreQuest.class);

    public WarLineExploreQuest(){

    }

    @Override
    public void updateWorld(final World world) {
        final Dialog warLineStartDialog = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_start.json");
        warLineStartDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 532527922378808472L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "desription");
                if(returnCode == 0){
                    choseAlternative(world);
                }
                else if(returnCode == 1){
                    choseShip(world);
                }
                else{
                    throw new IllegalArgumentException("Incorrect return code: " + returnCode);
                }

                endEarthDialog(world);
            }
        });
        world.addOverlayWindow(warLineStartDialog);
    }

    private void endEarthDialog(World world) {
        // hack
        AlienRace humanity = ((AlienRace) world.getFactions().get(HumanityGenerator.NAME));
        StarSystem solarSystem = humanity.getHomeworld();
        Earth earth = (Earth) solarSystem.getPlanets()[2];

        world.setCurrentRoom(earth.getOwner());
        GUI.getInstance().getNifty().gotoScreen("star_system_gui");
    }

    public static void choseShip(final World world) {
        logger.info("Player select the zorsan scout way");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "chose_ship");
        world.getGlobalVariables().put("war1_explore.chose_ship", true);
    }

    private void choseAlternative(final World world) {
        logger.info("Player select the alternative way");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "chose_alternative");

        new UnityLineGenerator().updateWorld(world);

        if(world.getGlobalVariables().containsKey("rebels_line.complete")){
            new RebelsLineGenerator().updateWorld(world);
        }
    }

    {
        /*
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system1_success");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system1_failed");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system2_success");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system2_failed");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system3_success");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "system3_failed");
            world.getPlayer().getJournal().addQuestEntries("war1_explore", "timeout_failed");
            world.getPlayer().getJournal().questCompleted("war1_explore");
        */

        /*
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_start.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_end_success.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_end_failed.json");

            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_sarah.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_genry.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_scientist.json");
            Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_gordon_after_return.json");
         */
    }
}
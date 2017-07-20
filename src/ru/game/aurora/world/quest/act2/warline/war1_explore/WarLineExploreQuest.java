package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
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

        resetAlternativeLines(world);
        switchAurora(world);
    }

    private static void resetAlternativeLines(final World world) {
        UnityLineGenerator.disposeQuestLine(world);
        RebelsLineGenerator.disposeQuestLine(world);
    }

    // change Aurora to Zorsan scout ship
    private static void switchAurora(final World world) {
        final Ship aurora = world.getPlayer().getShip();
        world.getGlobalVariables().put("war1_explore_aurora_backup", aurora);

        final Ship zorsanScout = new Ship(world, "zorsan_scout", aurora.getX(), aurora.getY());
        zorsanScout.setBaseCrew(5, 5, 0);

        // move aurora crew memebers to zorsan scout
        for(CrewMember member: aurora.getCrewMembers().values()){
            zorsanScout.addCrewMember(world, member);
        }

        // Set specific faction: all aliens (no zorsans) is hostile to zorsan scouts
        final Faction scoutFaction = new ZorsanScoutFaction();
        world.getReputation().setHostile(ZorsanScoutFaction.NAME, KliskGenerator.NAME);
        world.getReputation().setHostile(ZorsanScoutFaction.NAME, RoguesGenerator.NAME);
        world.getReputation().setHostile(ZorsanScoutFaction.NAME, BorkGenerator.NAME);

        zorsanScout.setFaction(scoutFaction);

        zorsanScout.setLandingPartyBlock(true);
        zorsanScout.setResearchBlock(true);
        zorsanScout.setEngineeringBlock(true);
        zorsanScout.setInventoryBlock(true);

        zorsanScout.getWeapons().add(new WeaponInstance(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon")));
        zorsanScout.getWeapons().add(new WeaponInstance(ResourceManager.getInstance().getWeapons().getEntity("scanner")));

        // set zorsan scout ship to player
        world.getPlayer().setSetCustomShip(world, zorsanScout);
        world.getGlobalVariables().put("war1_explore_stations_scanned", 0);

        // generate encounter star systems
        final QuestStarSystemEncounter encounterListener = new QuestStarSystemEncounter();
        encounterListener.updateWorld(world);

        // start encounter if player enter to the generated quest zorsan star systems
        world.addListener(encounterListener);
    }

    public static void scanStation(World world) {
        world.addListener(new GameEventListener() {
            private static final long serialVersionUID = 5301829116104880081L;
            @Override
            public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
                this.removeListener(world);
                return false;
            }
        });
    }

    private static void choseAlternative(final World world) {
        logger.info("Player select the alternative way");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "chose_alternative");

        new UnityLineGenerator().updateWorld(world);

        if(world.getGlobalVariables().containsKey("rebels_line.complete")){
            new RebelsLineGenerator().updateWorld(world);
        }
    }

    /*
    world.getPlayer().getJournal().addQuestEntries("war1_explore", "timeout_failed");
    world.getPlayer().getJournal().questCompleted("war1_explore");
    */

    /*
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_start.json");
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_end_success.json");
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/earth/war1_explore_earth_end_failed.json");

    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_sarah.json");
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_genry.json");
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_scientist.json");
    Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_crew_gordon_after_return.json");
    */
}
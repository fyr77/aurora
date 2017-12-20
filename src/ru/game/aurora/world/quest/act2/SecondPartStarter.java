package ru.game.aurora.world.quest.act2;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.SaveGameManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.Reputation;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.quest.act2.metropole.MetropoleBurdenQuest;
import ru.game.aurora.world.quest.act2.unity.UnityQuest;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Map;

/**
 * Created by User on 07.01.2017.
 * This code starts second part of a global plot, after Obliterator visits Solar system
 *
 * Begin quests:
 * - Unity
 * - The burden of the metropolis
 */
public class SecondPartStarter implements WorldGeneratorPart {

    private static final long serialVersionUID = 3401155966034871085L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondPartStarter.class);

    public void start(final World world) {
        world.getGlobalVariables().put("2nd_part", true);
        logger.info("Starting 2nd story part");

        // fade out screen, show new intro and dialogs after that
        FadeOutScreenController.makeFade(new IStateChangeListener() {
            private static final long serialVersionUID = 33172965260750148L;

            @Override
            public void stateChanged(Object param) {
                showIntro(world);
            }
        });
    }

    private void showIntro(World world) {
        IntroDialog dialog;
        if (world.getGlobalVariables().containsKey("colony_established")) {
            dialog = IntroDialog.load("story/intro_2_with_colony.json");
        }
        else {
            dialog = IntroDialog.load("story/intro_2_no_colony.json");
        }

        IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getName());
        introDialogController.pushDialog(dialog);
        introDialogController.setEndListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void stateChanged(World world) {
                updateWorld(world);
            }
        });
        GUI.getInstance().getNifty().gotoScreen("intro_dialog");
    }

    public void updateWorld(final World world) {
        world.getGlobalVariables().put("autosave_disabled", true);
        // Apply Act II world changes
        updateYear(world);
        fixDiplomacy(world);
        setResources(world);
        removeObliteratorBackground(world);
        movePlayerShipToEarth(world);
        world.getGlobalVariables().remove("autosave_disabled");

        if (!world.getGlobalVariables().containsKey("colony_established")) {
            // player did not establish a colony, force finish a colony quest
            world.getPlayer().getJournal().questCompleted("colony_search", "colony_created_for_act_2");
        }

        startUnityAndMetropoleQuests(world);

    }

    private void updateYear(final World world) {
        // Add 5 years after Act I ended
        world.addDays(365 * 5);
    }

    private void removeObliteratorBackground(final World world) {
        // todo: remove Obliterator background from cloned star system (MainQuestGenerator)
    }

    private void movePlayerShipToEarth(final World world) {
        StarSystem solarSystem = (StarSystem)world.getGlobalVariables().get("solar_system");
        if(solarSystem == null){
            logger.error("Solar system not found");
            return;
        }

        // Replace Aurora to Earth orbit
        BasePlanet earth = null;
        BasePlanet [] planets = solarSystem.getPlanets();
        for(int i = 0; i < planets.length; ++i){
            if(planets[i] != null && planets[i] instanceof Earth){
                earth = planets[i];
                break;
            }
        }

        if(earth == null){
            logger.error("Earth not found");
            return;
        }

        logger.info("Move player ship to Earth " + solarSystem.getCoordsString());
        world.setCurrentRoom(solarSystem);
        solarSystem.enter(world);
        world.getPlayer().getShip().setPos(earth.getX(), earth.getY());
    }

    private void startUnityAndMetropoleQuests(final World world) {
        final Dialog startDialog = Dialog.loadFromFile("dialogs/act2/act2_begin_martan.json");

        startDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 4488508909100895730L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                new UnityQuest().updateWorld(world);
                new MetropoleBurdenQuest().updateWorld(world);

                if (!world.getGlobalVariables().containsKey("autosave_disabled")) {
                    SaveGameManager.saveGame(SaveGameManager.getAutosaveSlot(), world);
                }
            }
        });
        world.addOverlayWindow(startDialog);
    }

    // fix hostile relations with all races except zorsan
    // as it will be impossible to pass main quests with hostile alien races
    private void fixDiplomacy(World world) {
        boolean atLeastOneRaceHostile = false;
        if (world.getReputation().isHostile(BorkGenerator.NAME, HumanityGenerator.NAME)) {
            world.getReputation().setReputation(BorkGenerator.NAME, HumanityGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            world.getReputation().setReputation(HumanityGenerator.NAME, BorkGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            atLeastOneRaceHostile = true;
        }

        if (world.getReputation().isHostile(KliskGenerator.NAME, HumanityGenerator.NAME)) {
            world.getReputation().setReputation(KliskGenerator.NAME, HumanityGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            world.getReputation().setReputation(HumanityGenerator.NAME, KliskGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            atLeastOneRaceHostile = true;
        }

        if (world.getReputation().isHostile(RoguesGenerator.NAME, HumanityGenerator.NAME)) {
            world.getReputation().setReputation(RoguesGenerator.NAME, HumanityGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            world.getReputation().setReputation(HumanityGenerator.NAME, RoguesGenerator.NAME, Reputation.NEUTRAL_REPUTATION);
            atLeastOneRaceHostile = true;
        }

        if (atLeastOneRaceHostile) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "act2_diplomacy_fix", "message"));
        }


    }

    // reset resources and credits to default values
    private void setResources(World world) {
        world.getPlayer().setResourceUnits(10);
        world.getPlayer().getInventory().setCount(Resources.CREDITS, 20);
    }
}
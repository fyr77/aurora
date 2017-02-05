package ru.game.aurora.world.quest;

import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 07.01.2017.
 * This code starts second part of a global plot, after Obliterator visits Solar system
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
        removeObliteratorBackground(world);
        updateColony(world);
        movePlayerShipToEarth(world);

        world.getGlobalVariables().remove("autosave_disabled");

        startUnityQuest(world);
    }

    private void updateYear(final World world) {
        // Add 5 years after Act I ended
        world.addDays(365 * 5);
    }

    private void removeObliteratorBackground(final World world) {
        // todo: remove Obliterator background from cloned star system (MainQuestGenerator)
    }

    private void updateColony(final World world) {
        // Replaces colony planet with a dialog planet like Earth (which can not be landed)
        if(world.getGlobalVariables().containsKey("colony_established")){
            colonyReplace(world);
        }
        else{
            // Create colony as AlienHomeworld
            colonyFound(world);
        }
    }

    private void colonyReplace(World world) {
        // Change colony planet type to AlienHomeworld
        if(!world.getGlobalVariables().containsKey("colony_search.coords")) {
            logger.error("Colony data not found, but global variable 'colony_established' exist");
            return;
        }

        Object obj = world.getGlobalVariables().get("colony_search.coords");
        if(obj == null){
            logger.error("Colony Planet data is not null");
            return;
        }

        if(obj instanceof Planet){
            Planet colonyPlanet = (Planet)obj;
            AlienHomeworld newColonyPlanet = buildColonyPlanet(world, colonyPlanet);

            // replace old colony planet
            BasePlanet[] planets = colonyPlanet.getOwner().getPlanets();
            for(int i = 0; i < planets.length; ++i){
                if(planets[i] == colonyPlanet){
                    logger.info("Colony type changed");
                    planets[i] = newColonyPlanet;
                    break;
                }
            }
        }
        else{
            logger.error("Colony Planet data is not instance of class " + Planet.class.getCanonicalName());
            return;
        }
    }

    private void colonyFound(World world) {
        int x = 0;
        int y = 0;

        Matcher m = Pattern.compile("\\d+").matcher((String)world.getGlobalVariables().get("colony_search.klisk_coords"));
        try {
            if (m.find()) {
                x = Integer.parseInt(m.group());

                if(m.find()) {
                    y = Integer.parseInt(m.group());
                }
            }
            else {
                logger.error("Cant parse 'colony_search.klisk_coords' - no coordinates data");
            }
        }
        catch (NumberFormatException e){
            logger.error("Cant parse 'colony_search.klisk_coords' coordinates.");
        }

        Object obj = world.getGalaxyMap().getObjectAt(x, y);
        if(obj != null && obj instanceof StarSystem){
            StarSystem colonyStarSystem = (StarSystem)obj;

            BasePlanet[] planets = colonyStarSystem.getPlanets();
            for(int i = 0; i < planets.length; ++i){
                if(planets[i] != null && planets[i].getSize() == 3 && planets[i].getAtmosphere() == PlanetAtmosphere.BREATHABLE_ATMOSPHERE){
                    // create colony here
                    AlienHomeworld newColonyPlanet = buildColonyPlanet(world, planets[i]);
                    planets[i] = buildColonyPlanet(world, planets[i]);
                    world.getGlobalVariables().put("colony_established", true);
                    world.getGlobalVariables().put("colony_search.coords", newColonyPlanet);
                    logger.info("Colony found in star system " + colonyStarSystem.getCoordsString());
                    break;
                }
            }
        }
        else{
            logger.error("Colony founding fail. Object [" + x + "," + y + "] is not star system.");
        }
    }

    private AlienHomeworld buildColonyPlanet(World world, BasePlanet sourcePlanet){
        return new AlienHomeworld(
                null,
                ((AlienRace) world.getFactions().get("Humanity")),
                loadColonyDialog(),
                sourcePlanet.getSize(),
                sourcePlanet.getY(),
                sourcePlanet.getOwner(),
                PlanetAtmosphere.BREATHABLE_ATMOSPHERE,
                sourcePlanet.getX(),
                PlanetCategory.PLANET_ROCK);
    }

    private Dialog loadColonyDialog() {
        // todo: build full colony dialog (quest "The burden of the metropolis")
        return Dialog.loadFromFile("dialogs/act2/colony_line/colony_dialog_before_landing.json");
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

    private void startUnityQuest(final World world) {
        Dialog startDialog = Dialog.loadFromFile("dialogs/act2/quest_union/act_2_begin_martan.json");

        startDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 4488508909100895730L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                new UnityQuest().updateWorld(world);
            }
        });
        world.addOverlayWindow(startDialog);
    }
}
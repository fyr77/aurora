package ru.game.aurora.world.quest;

import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;
import ru.game.aurora.world.space.earth.Earth;

/**
 * Created by User on 07.01.2017.
 * This code starts second part of a global plot, after Obliterator visits Solar system
 */
public class SecondPartStarter {

    private static final long serialVersionUID = 3401155966034871085L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondPartStarter.class);

    private static final int COLONY_FOUND_RADIUS_FROM_EARTH = 50;

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
        // Apply Act II world changes
        updateYear(world);
        removeObliteratorBackground(world);
        updateColony(world);
        startQuests(world);
        movePlayerShipToEarth(world);
    }

    private void updateYear(final World world) {
        // Add 5 years after Act I ended
        world.addDays(365 * 5);
    }

    private void removeObliteratorBackground(final World world) {
        // Remove Obliterator background from cloned star system (MainQuestGenerator)
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
        StarSystem solarSystem = (StarSystem)world.getGlobalVariables().get("solar_system");
        if(solarSystem == null){
            logger.error("Solar system not found - colony creating failed");
            return;
        }

        StarSystem colonyStarSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(solarSystem.getX(), solarSystem.getY(), COLONY_FOUND_RADIUS_FROM_EARTH,
            new StarSystemListFilter() {
            @Override
            public boolean filter(StarSystem ss) {
                BasePlanet[] planets = ss.getPlanets();
                if(planets.length == 0){
                    return false;
                }

                for (int i = 0; i < planets.length; ++i){
                    if(planets[i] != null && planets[i].getSize() == 3){
                        // atmosphere is not important(?)
                        return true;
                    }
                }
                return false;
            }
        });

        BasePlanet[] planets = colonyStarSystem.getPlanets();
        for(int i = 0; i < planets.length; ++i){
            if(planets[i] != null && planets[i].getSize() == 3){
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

    private AlienHomeworld buildColonyPlanet(World world, BasePlanet sourcePlanet){
        return new AlienHomeworld(
                "earth", // todo: change colony sprite
                ((AlienRace) world.getFactions().get("Humanity")),
                null, // todo: add dialog
                sourcePlanet.getSize(),
                sourcePlanet.getY(),
                sourcePlanet.getOwner(),
                PlanetAtmosphere.BREATHABLE_ATMOSPHERE,
                sourcePlanet.getX(),
                PlanetCategory.PLANET_ROCK);
    }

    private void startQuests(final World world) {
        world.addListener(new UnityQuest());
        world.getPlayer().getJournal().addQuestEntries("unity", "start");
        world.getPlayer().getJournal().addQuestEntries("colony_negotiation", "start");
    }

    private void movePlayerShipToEarth(final World world) {
        // Replace Aurora to start position
        StarSystem solarSystem = (StarSystem)world.getGlobalVariables().get("solar_system");
        if(solarSystem == null){
            logger.error("Solar system not found");
            return;
        }

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
}
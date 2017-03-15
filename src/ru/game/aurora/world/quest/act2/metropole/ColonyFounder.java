package ru.game.aurora.world.quest.act2.metropole;

import org.slf4j.LoggerFactory;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.StarSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by di Grigio on 28.02.2017.
 */
class ColonyFounder {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ColonyFounder.class);

    public static void foundColony(World world){
        // Replaces colony planet with a dialog planet like Earth (which can not be landed)
        if(world.getGlobalVariables().containsKey("colony_established")){
            colonyReplace(world);
        }
        else{
            // Create colony as AlienHomeworld
            colonyFound(world);
        }
    }

    private static void colonyReplace(World world) {
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

    private static void colonyFound(World world) {
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

                    planets[i] = newColonyPlanet;
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

    private static AlienHomeworld buildColonyPlanet(World world, BasePlanet sourcePlanet){
        return new AlienHomeworld(
                null,
                ((AlienRace) world.getFactions().get("Humanity")),
                null,
                sourcePlanet.getSize(),
                sourcePlanet.getY(),
                sourcePlanet.getOwner(),
                PlanetAtmosphere.BREATHABLE_ATMOSPHERE,
                sourcePlanet.getX(),
                PlanetCategory.PLANET_ROCK);
    }
}

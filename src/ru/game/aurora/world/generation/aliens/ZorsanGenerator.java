package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.Color;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.13
 * Time: 18:17
 */
public class ZorsanGenerator implements WorldGeneratorPart {
    public static final String NAME = "zorsan";

    private static final long serialVersionUID = 1083992211652099884L;

    private StarSystem generateHomeworld(World world, int x, int y, AlienRace race) {
        BasePlanet[] planets = new BasePlanet[1];
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.white), x, y);

        planets[0] = new AlienHomeworld("klisk_homeworld", race, race.getDefaultDialog(), 3, 0, ss, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 0, PlanetCategory.PLANET_ROCK);
        HomeworldGenerator.setCoord(planets[0], 3);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }


    @Override
    public void updateWorld(World world) {
        AlienRace race = new AlienRace(NAME, "zorsan_scout", Dialog.loadFromFile("dialogs/zorsan_main.json"));
        StarSystem homeworld = generateHomeworld(world, 3, 8, race);
        world.getGlobalVariables().put("zorsan.homeworld", String.format("[%d, %d]", homeworld.getGlobalMapX(), homeworld.getGlobalMapY()));
        world.getGalaxyMap().addObjectAndSetTile(homeworld, 3, 8);
        world.addListener(new StandardAlienShipEvent(race));
        race.setHomeworld(homeworld);
        race.setTravelDistance(5);
        world.getRaces().put(race.getName(), race);
    }
}

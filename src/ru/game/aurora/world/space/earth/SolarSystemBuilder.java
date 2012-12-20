/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.12.12
 * Time: 15:32
 */
package ru.game.aurora.world.space.earth;

import jgame.JGColor;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.StarSystem;

import java.util.Random;

public class SolarSystemBuilder {
    private static final Random r = new Random();

    private static void setCoord(Planet p, int radius) {
        int planetX = r.nextInt(2 * radius) - radius;

        int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (r.nextBoolean() ? -1 : 1));

        p.setGlobalX(planetX);
        p.setGlobalY(planetY);
    }

    public static StarSystem createSolarSystem() {
        // todo: gas giants and other planets

        Planet[] planets = new Planet[4];
        StarSystem ss = new StarSystem(new StarSystem.Star(2, JGColor.yellow), 9, 9);

        // mercury
        planets[0] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0, false);
        setCoord(planets[0], 2);

        // venus
        planets[1] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0, false);
        setCoord(planets[1], 3);

        planets[2] = new Earth(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.BREATHABLE_ATMOSPHERE, 3, 0, 0, true);
        setCoord(planets[2], 5);

        // mars
        planets[3] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0, false);
        setCoord(planets[3], 5);


        ss.setPlanets(planets);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }
}

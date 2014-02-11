/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 20:11
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.space.earth.Earth;

public class HomeworldGenerator {

    public static void setCoord(BasePlanet p, int radius) {
        int planetX = CommonRandom.getRandom().nextInt(2 * radius) - radius;

        int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));

        p.setPos(planetX, planetY);
    }

    public static StarSystem createSolarSystem(World world, AlienRace humanRace) {
        // todo: gas giants and other planets

        BasePlanet[] planets = new BasePlanet[6];
        StarSystem ss = new StarSystem("Solar system", new Star(2, Color.yellow), 9, 9);

        // mercury
        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        setCoord(planets[0], 2);

        // venus
        planets[1] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[1], 3);

        planets[2] = new Earth(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.BREATHABLE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[2], 5);
        planets[2].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));

        // mars
        planets[3] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[3], 7);

        // jupiter
        planets[4] = new GasGiant(0, 0, ss);
        setCoord(planets[4], 9);

        // saturn
        planets[5] = new GasGiant(0, 0, ss);
        setCoord(planets[5], 12);
        planets[5].setRings(1);
        planets[5].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));

        ss.setPlanets(planets);
        ss.setRadius(Math.max((int) (12 * 1.5), 10));

        NPCShip spaceStation = new NPCShip(planets[2].getX() + 1, planets[2].getY() - 1, "earth_station", humanRace, null, "Orbital Scaffold");
        spaceStation.setStationary(true);
        spaceStation.setAi(null);
        ss.getShips().add(spaceStation);

        return ss;
    }


}

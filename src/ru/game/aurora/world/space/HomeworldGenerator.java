/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 20:11
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.ResourceManager;
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

        BasePlanet[] planets = new BasePlanet[8];
        StarSystem ss = new StarSystem("Solar system", new Star(2, Color.yellow), 9, 9);

        // mercury
        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        setCoord(planets[0], 2);

        // venus
        planets[1] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[1], 3);

        planets[2] = new Earth(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.BREATHABLE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[2], 5);
        Planet Moon = new Planet(world, ss, PlanetCategory.PLANET_FULL_STONE, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        planets[2].addSatellite(Moon);

        // mars
        planets[3] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        setCoord(planets[3], 7);

        ss.setAsteroidBelt(10, 3);

        // jupiter
        planets[4] = new GasGiant(0, 0, ss);
        setCoord(planets[4], 15);
        planets[4].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));

        // saturn
        planets[5] = new GasGiant(0, 0, ss);
        setCoord(planets[5], 18);
        planets[5].setRings(1);
        planets[5].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));

        //uranus
        planets[6] = new GasGiant(0, 0, ss);
        setCoord(planets[6], 21);

        //neptune
        planets[7] = new GasGiant(0, 0, ss);
        setCoord(planets[7], 24);

        ss.setPlanets(planets);
        ss.setRadius(26);

        NPCShip spaceStation = new NPCShip(planets[2].getX() + 1, planets[2].getY() - 1, "earth_station", humanRace, null, "Orbital Scaffold", 25);
        if (spaceStation.getDistance(Moon) == 0) { // check that it does not intersect with moon
            spaceStation.setPos(spaceStation.getX(), spaceStation.getY() + 1);
        }
        spaceStation.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon2"), ResourceManager.getInstance().getWeapons().getEntity("humanity_missiles"));
        spaceStation.setStationary(true);
        spaceStation.setAi(null);
        ss.getShips().add(spaceStation);

        return ss;
    }
}

package ru.game.aurora.world.planet;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.AcidRainCloud;
import ru.game.aurora.world.planet.nature.RainCloud;
import ru.game.aurora.world.planet.nature.Tornado;

/**
 * Different types of natural dangers that can be met on a planet
 */
public class Environment {
    public static final byte RAIN = 0x01;
    public static final byte ACID_RAIN = 0x02;
    public static final byte LAVA = 0x04;
    public static final byte WIND = 0x08;

    // adds effects on planets
    public static class PlanetProcessor extends GameEventListener {
        @Override
        public boolean onPlayerLandedPlanet(World world, Planet planet) {
            if ((planet.getEnvironment() & WIND) != 0) {
                addTornados(planet);
            }

            if ((planet.getEnvironment() & RAIN) != 0) {
                addRain(planet, false);
            }

            if ((planet.getEnvironment() & ACID_RAIN) != 0) {
                addRain(planet, true);
            }
            return false;
        }

        private void addTornados(Planet p) {
            int count = 0;
            for (GameObject go : p.getPlanetObjects()) {
                if (go instanceof Tornado) {
                    ++count;
                }
            }

            int maxCount = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment.tornado.max_per_planet"));
            for (int i = count; i < maxCount; ++i) {
                Tornado t = new Tornado(p.getMap());
                p.setNearestFreePoint(t, CommonRandom.getRandom().nextInt(p.getWidth()), CommonRandom.getRandom().nextInt(p.getHeight()));
                p.getPlanetObjects().add(t);
            }
        }

        private void addRain(Planet p, boolean acid) {
            int count = 0;
            for (GameObject go : p.getPlanetObjects()) {
                if ((acid && go instanceof AcidRainCloud) || (!acid && go instanceof RainCloud)) {
                    ++count;
                }
            }

            int maxCount = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment." + (acid ? "acid_rain" : "rain") + ".max"));
            for (int i = count; i < maxCount; ++i) {
                RainCloud rc = acid ? new AcidRainCloud(p) : new RainCloud(p);
                p.getPlanetObjects().add(rc);
            }
        }
    }
}

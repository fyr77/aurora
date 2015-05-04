package ru.game.aurora.world.planet;

import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.effects.FallingMeteor;
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
    public static final byte METEORS = 0x10;

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

        @Override
        public boolean onTurnEnded(World world) {
            if (!(world.getCurrentRoom() instanceof Planet) || (((Planet) world.getCurrentRoom()).getEnvironment() & Environment.METEORS) == 0) {
                return false;
            }

            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.meteor.chance")) {
                ((Planet) world.getCurrentRoom()).getController().addEffect(new FallingMeteor(
                        CommonRandom.getRandom().nextInt(AuroraGame.tilesX * AuroraGame.tileSize / 2) + AuroraGame.tilesX * AuroraGame.tileSize / 4
                        , CommonRandom.getRandom().nextInt(AuroraGame.tilesY * AuroraGame.tileSize / 2) + AuroraGame.tilesY * AuroraGame.tileSize / 4
                ));
                return true;
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

    public static void appendScanText(StringBuilder sb, int env) {
        if ((env & WIND) != 0) {
            sb.append(Localization.getText("gui", "scan.tornado")).append('\n');
        }

        if ((env & ACID_RAIN) != 0) {
            sb.append(Localization.getText("gui", "scan.acid_rain")).append('\n');
        }

        if ((env & METEORS) != 0) {
            sb.append(Localization.getText("gui", "scan.meteor")).append('\n');
        }

        if ((env & LAVA) != 0) {
            sb.append(Localization.getText("gui", "scan.lava")).append('\n');
        }
        sb.append('\n');
    }
}

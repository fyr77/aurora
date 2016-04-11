package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;

/**
 * Same as rain but has different sprite and damages the landing party
 */
public class AcidRainCloud extends RainCloud {
    public AcidRainCloud(Planet planet) {
        super(planet, "acid_rain");
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);

        final LandingParty landingParty = world.getPlayer().getLandingParty();
        if (world.isUpdatedThisFrame() && landingParty.getDistanceWrapped(this, myPlanet.getWidth(), myPlanet.getHeight()) <= radius) {
            boolean underTree = false;
            for (GameObject go : world.getCurrentRoom().getMap().getObjects()) {
                if (landingParty.getDistanceWrapped(go, myPlanet.getWidth(), myPlanet.getHeight()) == 0 && Plant.class.isAssignableFrom(go.getClass())) {
                    underTree = true;
                    break;
                }
            }
            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.acid_rain.damage_chance")) {
                if (underTree) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.tornado.acid_rain_protected"));
                } else {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.tornado.acid_rain"));
                    landingParty.subtractHp(world, Configuration.getIntProperty("environment.acid_rain.damage"));
                }
            }
        }

    }
}

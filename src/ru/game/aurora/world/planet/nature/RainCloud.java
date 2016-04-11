package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetCategory;

/**
 * Area of a planet that is filled with rain.
 * Has no effect, just a visual.
 */
public class RainCloud extends BaseGameObject {
    public static final long serialVersionUID = -6720745635254295035L;

    protected int radius;

    protected int maxRadius;

    private int ttl;

    protected Planet myPlanet;

    public RainCloud(Planet planet) {
        this(planet, planet.getCategory() == PlanetCategory.PLANET_ICE ? "snow" : "rain");
    }

    public RainCloud(Planet planet, String sprite) {
        super(
                0
                , 0
                , new Drawable(sprite, true));
        this.myPlanet = planet;
        respawn();
    }

    private void respawn() {
        setPos(CommonRandom.getRandom().nextInt(myPlanet.getWidth())
                , CommonRandom.getRandom().nextInt(myPlanet.getHeight()));
        ttl = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment.rain.max_ttl")) + 10;
        maxRadius = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment.rain.max_size")) + 10;
        radius = 2;
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (world.isUpdatedThisFrame()) {
            --ttl;
            if (ttl == 0) {
                respawn();
            } else if (radius < maxRadius) {
                radius++;
            }
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        for (int xx = x - radius; xx <= x + radius; ++xx) {
            for (int yy = y - radius; yy <= y + radius; ++yy) {
                if (getDistanceWrapped(x, y, xx, yy, myPlanet.getWidth(), myPlanet.getHeight()) < radius && camera.isInViewport(xx, yy) && myPlanet.getMap().isTileVisible(xx, yy)) {
                    drawable.draw(g, camera.getXCoordWrapped(xx, myPlanet.getWidth()), camera.getYCoordWrapped(yy, myPlanet.getHeight()), false);
                }
            }
        }
    }
}

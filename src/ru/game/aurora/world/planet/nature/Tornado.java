package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.Planet;

import java.util.Collections;
import java.util.List;

/**
 * Tornado moves around planet surface, damages player and throws him in a random direction
 */
public class Tornado extends BaseGameObject implements IMonster {
    private int ttl;

    private MonsterController myController;

    public Tornado(ITileMap myMap) {
        super(0, 0, new Drawable("tornado", true));
        ttl = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment.tornado.max_ttl") + 10);
        myController = new MonsterController(myMap, this);
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        final ITileMap map = world.getCurrentRoom().getMap();
        final LandingParty landingParty = world.getPlayer().getLandingParty();
        double distanceWrapped = getDistanceWrapped(landingParty, map.getWidthInTiles(), map.getHeightInTiles());

        if (world.isUpdatedThisFrame()) {
            --ttl;
            myController.update(container, world);
            // tornado does not block path
            map.setTilePassable(getTargetX(), getTargetY(), true);
            if (ttl <= 0) {
                // spawn new tornado somewhere else
                if (distanceWrapped < world.getCamera().getNumTilesX() / 2) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.tornado.dissipate"));
                }
                ttl = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("environment.tornado.max_ttl") + 10);

                ((Planet) world.getCurrentRoom()).setNearestFreePoint(this, CommonRandom.getRandom().nextInt(map.getWidthInTiles()), CommonRandom.getRandom().nextInt(map.getHeightInTiles()));
                distanceWrapped = getDistanceWrapped(landingParty, map.getWidthInTiles(), map.getHeightInTiles());
                if (distanceWrapped < world.getCamera().getNumTilesX() / 2) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.tornado.appear"));
                }
            }
        }


        if (distanceWrapped == 0) {
            landingParty.subtractHp(world, Configuration.getIntProperty("environment.tornado.damage"));
            int dx = CommonRandom.getRandom().nextInt(5) - 2;
            int dy = CommonRandom.getRandom().nextInt(5) - 2;

            ((Planet) world.getCurrentRoom()).setNearestFreePoint(landingParty, landingParty.getX() + dx, landingParty.getY() + dy);

            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.tornado.attack"));
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return Localization.getText("gui", "surface.tornado.name");
    }

    @Override
    public int getHp() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getSpeed() {
        return CommonRandom.getRandom().nextInt(2) + 1;
    }

    @Override
    public List<WeaponInstance> getWeapons() {
        return Collections.emptyList();
    }

    @Override
    public MonsterBehaviour getBehaviour() {
        return MonsterBehaviour.PASSIVE;
    }
}

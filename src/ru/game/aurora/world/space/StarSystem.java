/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetScanScreen;

import java.util.ArrayList;
import java.util.List;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject {
    public static final JGColor[] possibleColors = {JGColor.red, JGColor.white, JGColor.yellow, new JGColor(122, 155, 243)};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    public static class Star {
        // 1 is largest star, 4 is smallest
        public final int size;
        public final JGColor color;

        public Star(int size, JGColor color) {
            this.size = size;
            this.color = color;
        }
    }

    private Star star;

    private Planet[] planets;

    private int globalMapX;

    private int globalMapY;

    private ParallaxBackground background;

    /**
     * Relation between tile size and max planet size
     * 3 means max planet will have radius of 3 tiles
     */
    public final static int PLANET_SCALE_FACTOR = 2;

    public final static int STAR_SCALE_FACTOR = 4;

    // size of star system. moving out of radius from the star initiates return to global map
    private int radius;

    private List<NPCShip> ships = new ArrayList<NPCShip>();

    public StarSystem(Star star, int globalMapX, int globalMapY) {
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
    }

    public void setPlanets(Planet[] planets) {
        this.planets = planets;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public void drawOnGlobalMap(JGEngine engine, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(tileX, tileY)) {
            return;
        }
        engine.setColor(star.color);
        engine.drawOval(camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, camera.getTileWidth() / star.size, camera.getTileHeight() / star.size, true, true);
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(JGEngine engine, Player player) {
    }

    @Override
    public void update(JGEngine engine, World world) {
        super.update(engine, world);

        int y = world.getPlayer().getShip().getY();
        int x = world.getPlayer().getShip().getX();

        if ((engine.getKey(JGEngineInterface.KeyUp) && y <= -radius)
                || (engine.getKey(JGEngineInterface.KeyDown) && y >= radius)
                || (engine.getKey(JGEngineInterface.KeyLeft) && x <= -radius)
                || (engine.getKey(JGEngineInterface.KeyRight) && x >= radius)) {
            GameLogger.getInstance().logMessage("Leaving star system...");
            world.setCurrentRoom(world.getGalaxyMap());
            world.getGalaxyMap().enter(world);
            player.getShip().setPos(globalMapX, globalMapY);
            // do not keep background
            background = null;
        }


        for (Planet p : planets) {
            if (x == p.getGlobalX() && y == p.getGlobalY()) {
                if (engine.getKey(JGEngineInterface.KeyEnter)) {
                    GameLogger.getInstance().logMessage("Descending to surface...");
                    world.getPlayer().setLandingParty(new LandingParty(0, 0, new LandingPartyWeapon(1, 3, "Assault rifles"), 1, 1, 1));
                    p.enter(world);
                    world.setCurrentRoom(p);
                    engine.clearKey(JGEngineInterface.KeyEnter);
                    break;
                } else if (engine.getLastKeyChar() == 's') {
                    PlanetScanScreen s = new PlanetScanScreen(this, p);
                    s.enter(world);
                    world.setCurrentRoom(s);
                    return;
                } else if (world.isUpdatedThisFrame()) {
                    p.processCollision(engine, world.getPlayer());
                    break;
                }
            }
        }


        for (NPCShip ship : ships) {
            if (ship.getX() == x && ship.getY() == y) {
                if (!ship.isHostile() && engine.getKey(JGEngineInterface.KeyEnter)) {
                    world.setCurrentDialog(ship.getRace().getDefaultDialog());
                }
            }
            if (world.isUpdatedThisFrame()) {
                ship.update(engine, world);
            }
        }

    }

    @Override
    public void enter(World world) {
        super.enter(world);
        player = world.getPlayer();
        player.getShip().setPos(-radius + 1, 0);
        // in star system camera is always fixed on center
        //world.getCamera().setTarget(new BasePositionable(world.getCamera().getNumTilesX() / 2, world.getCamera().getNumTilesY() / 2));
        world.getCamera().setTarget(player.getShip());
        if (background == null) {
            background = new ParallaxBackground(radius * world.getCamera().getTileWidth(), planets.length);
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        background.draw(engine, camera);
        player.getShip().draw(engine, camera);
        player.addGlobalStatus();
        GameLogger.getInstance().addStatusMessage("==========================");
        engine.setColor(star.color);

        final int starX = camera.getXCoord(0) + (camera.getTileWidth() / 2);
        final int starY = camera.getYCoord(0) + camera.getTileHeight() / 2;
        if (camera.isInViewport(0, 0)) {
            engine.drawOval(starX, starY, engine.tileWidth() * STAR_SCALE_FACTOR / star.size, engine.tileHeight() * STAR_SCALE_FACTOR / star.size, true, true);
        }

        for (Planet p : planets) {
            if (p.getGlobalX() == player.getShip().getX() && p.getGlobalY() == player.getShip().getY()) {
                GameLogger.getInstance().addStatusMessage("Approaching planet: ");
                GameLogger.getInstance().addStatusMessage("Press <S> to scan");
                GameLogger.getInstance().addStatusMessage("Press <enter> to launch surface party");
            }

            int planetX = camera.getXCoord(p.getGlobalX()) + (camera.getTileWidth() / 2);
            int planetY = camera.getYCoord(p.getGlobalY()) + camera.getTileWidth() / 2;
            double size = Math.sqrt(Math.pow((planetX - starX), 2) + Math.pow((planetY - starY), 2)) * 2;

            engine.drawOval(starX, starY, size, size, false, true, 1, JGColor.grey);
            p.drawOnGlobalMap(engine, camera, 0, 0);

        }
        for (NPCShip ship : ships) {
            ship.draw(engine, camera);
            if (ship.getX() == player.getShip().getX() && ship.getY() == player.getShip().getY()) {
                if (!ship.isHostile()) {
                    GameLogger.getInstance().addStatusMessage("Press <enter> to hail ship");
                }
            }
        }

    }

    public List<NPCShip> getShips() {
        return ships;
    }
}

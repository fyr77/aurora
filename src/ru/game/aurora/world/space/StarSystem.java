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
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetScanScreen;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject {
    public static final JGColor[] possibleColors = {JGColor.red, JGColor.white, JGColor.yellow, JGColor.blue};

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

    public StarSystem(Star star, int globalMapX, int globalMapY) {
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
    }

    public void setPlanets(Planet[] planets) {
        this.planets = planets;
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

        if ((engine.getKey(JGEngineInterface.KeyUp) && y == 0)
                || (engine.getKey(JGEngineInterface.KeyDown) && y == world.getCamera().getNumTilesY())
                || (engine.getKey(JGEngineInterface.KeyLeft) && x == 0)
                || (engine.getKey(JGEngineInterface.KeyRight) && x == world.getCamera().getNumTilesX())) {
            GameLogger.getInstance().logMessage("Leaving star system...");
            world.setCurrentRoom(world.getGalaxyMap());
            world.getGalaxyMap().enter(world);
            player.getShip().setPos(globalMapX, globalMapY);
        }


        for (Planet p : planets) {
            if (x == p.getGlobalX() && y == p.getGlobalY()) {
                if (engine.getKey(JGEngineInterface.KeyEnter)) {
                    GameLogger.getInstance().logMessage("Descending to surface...");
                    world.getPlayer().setLandingParty(new LandingParty(0, 0, 1, 1, 1));
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


    }

    @Override
    public void enter(World world) {
        super.enter(world);
        player = world.getPlayer();
        player.getShip().setPos(0, 0);
        // in star system camera is always fixed on center
        world.getCamera().setTarget(new Camera.FixedPosition(world.getCamera().getNumTilesX() / 2, world.getCamera().getNumTilesY() / 2));
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        player.getShip().draw(engine, camera);
        engine.setColor(star.color);

        engine.drawOval(camera.getNumTilesX() / 2 * engine.tileWidth() + (engine.tileWidth() / 2), camera.getNumTilesY() / 2 * engine.tileHeight() + engine.tileWidth() / 2, engine.tileWidth() / star.size, engine.tileHeight() / star.size, true, true);
        for (Planet p : planets) {
            if (p.getGlobalX() == player.getShip().getX() && p.getGlobalY() == player.getShip().getY()) {
                GameLogger.getInstance().addStatusMessage("Approaching planet: ");
                GameLogger.getInstance().addStatusMessage("Press <S> to scan");
                GameLogger.getInstance().addStatusMessage("Press <enter> to launch surface party");

            }
            p.drawOnGlobalMap(engine, camera, 0, 0);
        }
    }
}

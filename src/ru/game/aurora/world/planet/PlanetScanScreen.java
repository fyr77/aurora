/**
 * User: jedi-philosopher
 * Date: 04.12.12
 * Time: 22:21
 */
package ru.game.aurora.world.planet;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Invoked when in star system view player presses 'S' when orbiting a planet.
 * Shows general planet info and map of discovered regions
 */
public class PlanetScanScreen implements Room
{
    private Planet planet;

    private Camera myCamera;

    private StarSystem home;

    public PlanetScanScreen(StarSystem home, Planet planet) {
        this.home = home;
        this.planet = planet;
    }

    @Override
    public void enter(World world) {
        final Camera oldCamera = world.getCamera();
        final int newTileWidth = oldCamera.getNumTilesX() * oldCamera.getTileWidth() / planet.getWidth();
        final int newTileHeight = oldCamera.getNumTilesY() * oldCamera.getTileHeight() / planet.getHeight();
        myCamera = new Camera(0, 0, newTileWidth, newTileHeight, planet.getWidth(), planet.getHeight(), world.getCamera().getEngine());
        myCamera.setTarget(new BasePositionable(planet.getWidth() / 2, planet.getHeight() / 2));
    }

    @Override
    public void update(JGEngine engine, World world) {
        if (engine.getKey(JGEngine.KeyEnter) || engine.getKey(JGEngine.KeyEsc) || engine.getLastKeyChar() == 's') {
            world.setCurrentRoom(home);
            home.enter(world);
            world.getPlayer().getShip().setPos(planet.getGlobalX(), planet.getGlobalY());
            engine.clearLastKey();
            engine.clearKey(JGEngine.KeyEnter);
            engine.clearKey(JGEngine.KeyEsc);
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        planet.printPlanetStatus();
        planet.drawLandscape(engine, myCamera, false);
        GameLogger.getInstance().addStatusMessage("Press <enter>, <esc> or <S> to return");
    }
}

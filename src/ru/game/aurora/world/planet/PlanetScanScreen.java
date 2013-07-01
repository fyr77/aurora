/**
 * User: jedi-philosopher
 * Date: 04.12.12
 * Time: 22:21
 */
package ru.game.aurora.world.planet;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Invoked when in star system view player presses 'S' when orbiting a planet.
 * Shows general planet info and map of discovered regions
 */
public class PlanetScanScreen implements Room {
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
        myCamera = new Camera(0, 0, planet.getWidth(), planet.getHeight(), newTileWidth, newTileHeight);
        myCamera.setTarget(new BasePositionable(planet.getWidth() / 2, planet.getHeight() / 2));
    }

    @Override
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE) || container.getInput().isKeyPressed(Input.KEY_S)) {
            world.setCurrentRoom(home);
            home.enter(world);
            world.getPlayer().getShip().setPos(planet.getGlobalX(), planet.getGlobalY());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        planet.printPlanetStatus();
        planet.drawLandscape(container, graphics, myCamera, false);
    }
}

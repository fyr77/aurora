/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 15:29
 */
package ru.game.aurora.world.space;


import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Global map of the galaxy, all stars in single screen
 */
public class GalaxyMapScreen implements Room {
    /**
     * Renders galaxy objects in this camera, that is set up so that all galaxy will fit into render area
     */
    private Camera myCamera;

    private Ship ship;

    private GalaxyMap galaxyMap;

    @Override
    public void enter(World world) {
        galaxyMap = world.getGalaxyMap();
        final Camera oldCamera = world.getCamera();
        final int newTileWidth = oldCamera.getNumTilesX() * oldCamera.getTileWidth() / galaxyMap.getTilesX();
        final int newTileHeight = oldCamera.getNumTilesY() * oldCamera.getTileHeight() / galaxyMap.getTilesY();
        myCamera = new Camera(0, 0, newTileWidth, newTileHeight, galaxyMap.getTilesX(), galaxyMap.getTilesY(), world.getCamera().getEngine());
        myCamera.setTarget(new BasePositionable(galaxyMap.getTilesX() / 2, galaxyMap.getTilesY() / 2));
        ship = world.getPlayer().getShip();
    }

    @Override
    public void update(JGEngine engine, World world) {
        if (engine.getKey(JGEngine.KeyEnter) || engine.getKey(JGEngine.KeyEsc) || engine.getLastKeyChar() == 'm') {
            world.setCurrentRoom(world.getGalaxyMap());
            engine.clearLastKey();
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        for (int i = 0; i < galaxyMap.getTilesY(); ++i) {
            for (int j = 0; j < galaxyMap.getTilesX(); ++j) {
                GalaxyMapObject obj = galaxyMap.getObjectAt(j, i);
                if (obj != null) {
                    obj.drawOnGlobalMap(engine, myCamera, j, i);
                }
            }
        }

        engine.drawImage(myCamera.getXCoord(ship.getX()), myCamera.getYCoord(ship.getY()), "aurora");
        GameLogger.getInstance().addStatusMessage("Press <enter>, <m> or <esc> to close Galaxy map");
    }
}

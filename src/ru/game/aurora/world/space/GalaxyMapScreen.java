/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 15:29
 */
package ru.game.aurora.world.space;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
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

    private World world;

    @Override
    public void enter(World world) {
        this.world = world;
        galaxyMap = world.getGalaxyMap();
        final Camera oldCamera = world.getCamera();
        final float newTileWidth = oldCamera.getNumTilesX() * oldCamera.getTileWidth() / galaxyMap.getTilesX();
        final float newTileHeight = oldCamera.getNumTilesY() * oldCamera.getTileHeight() / galaxyMap.getTilesY();
        myCamera = new Camera(0, 0, galaxyMap.getTilesX(), galaxyMap.getTilesY(), newTileWidth, newTileHeight);
        myCamera.setTarget(new BasePositionable(galaxyMap.getTilesX() / 2, galaxyMap.getTilesY() / 2));
        ship = world.getPlayer().getShip();
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE) || container.getInput().isKeyPressed(Input.KEY_M)) {
            world.setCurrentRoom(world.getGalaxyMap());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        for (int i = 0; i < galaxyMap.getTilesY(); ++i) {
            for (int j = 0; j < galaxyMap.getTilesX(); ++j) {
                GalaxyMapObject obj = galaxyMap.getObjectAt(j, i);
                if (obj != null) {
                    obj.drawOnGlobalMap(container, g, myCamera, j, i);
                }
            }
        }

        g.drawImage(ResourceManager.getInstance().getImage("aurora"), myCamera.getXCoord(ship.getX()), myCamera.getYCoord(ship.getY()));
    }
}

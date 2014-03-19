/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 15:29
 */
package ru.game.aurora.world.space;


import de.lessvoid.nifty.controls.Button;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.Movable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Global map of the galaxy, all stars in single screen
 */
public class GalaxyMapScreen implements Room {
    private static final long serialVersionUID = -1602653800907830048L;
    /**
     * Renders galaxy objects in this camera, that is set up so that all galaxy will fit into render area
     */
    private Camera myCamera;

    private Ship ship;

    private GalaxyMap galaxyMap;

    private StarSystem solarSystem;

    @Override
    public void enter(World world) {
        galaxyMap = world.getGalaxyMap();
        final Camera oldCamera = world.getCamera();
        final float newTileWidth = oldCamera.getNumTilesX() * oldCamera.getTileWidth() / galaxyMap.getTilesX();
        final float newTileHeight = oldCamera.getNumTilesY() * oldCamera.getTileHeight() / galaxyMap.getTilesY();
        myCamera = new Camera(0, 0, galaxyMap.getTilesX(), galaxyMap.getTilesY(), newTileWidth, newTileHeight);
        myCamera.setTarget(new Movable(galaxyMap.getTilesX() / 2, galaxyMap.getTilesY() / 2));
        ship = world.getPlayer().getShip();
        solarSystem = (StarSystem) world.getGlobalVariables().get("solar_system");
    }

    @Override
    public void returnTo(World world) {
        enter(world);
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE) || container.getInput().isKeyPressed(Input.KEY_M)) {
            world.setCurrentRoom(world.getGalaxyMap());
            GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("starmap_button", Button.class).setText(Localization.getText("gui", "space.galaxy_map"));
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
                if (solarSystem == obj) {
                    g.setColor(Color.yellow);
                    g.drawString("Solar system", myCamera.getXCoord(j), myCamera.getYCoord(i));
                }
            }
        }

        g.drawImage(ResourceManager.getInstance().getImage("aurora"), myCamera.getXCoord(ship.getX()), myCamera.getYCoord(ship.getY()));
    }
}

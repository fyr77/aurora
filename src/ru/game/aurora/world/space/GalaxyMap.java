/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:12
 */
package ru.game.aurora.world.space;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.StarMapController;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.*;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Galaxy map.
 * Map is represented as a list of all static objects (like star systems), and a 2d array of cells, where each cell is either -1
 * (empty sector of space) or index of an element in this array.
 */
public class GalaxyMap extends BaseSpaceRoom {

    private static final Logger logger = LoggerFactory.getLogger(GalaxyMap.class);

    private static final long serialVersionUID = -2138368991952958011L;
    private final List<GalaxyMapObject> objects = new ArrayList<>();
    private transient ParallaxBackground background;
    private transient GalaxyMapScreen fullMapScreen = new GalaxyMapScreen();

    private int[][] map;

    private int tilesX;

    private int tilesY;

    private World world;

    private StarSystem playerMarkSystem;

    public GalaxyMap() {
    }

    public GalaxyMap(World world, int tilesX, int tilesY) {
        this.world = world;
        //this.myGui = new GalaxyMapController(world);
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        map = new int[tilesY][tilesX];
        for (int i = 0; i < tilesY; ++i) {
            for (int j = 0; j < tilesX; ++j) {
                map[i][j] = -1;
            }
        }
    }

    public static double getDistance(Positionable first, Positionable second) {
        return Math.sqrt(Math.pow(first.getX() - second.getX(), 2) + Math.pow(first.getY() - second.getY(), 2));
    }

    private void createBackground(Camera cam, int tilesX, int tilesY) {
        background = new ParallaxBackground(tilesX * cam.getTileWidth(), tilesY * cam.getTileHeight(), cam.getTileWidth() * tilesX / 2, cam.getTileHeight() * tilesY / 2, 15);
    }

    public ITileMap getMap() {
        return null;
    }

    @Override
    public double getTurnToDayRelation() {
        return 1;
    }

    public int[][] getInternalMap() {
        return map;
    }

    public int getTilesX() {
        return tilesX;
    }

    public int getTilesY() {
        return tilesY;
    }

    @Override
    public void enter(World world) {
        super.enter(world);
        world.getCamera().setTarget(player.getShip());
        if (world.getCurrentStarSystem() != null) {
            player.getShip().setPos(world.getCurrentStarSystem().getX(), world.getCurrentStarSystem().getY());
            world.setCurrentStarSystem(null);
        }
        final Nifty nifty = GUI.getInstance().getNifty();
        nifty.gotoScreen("galaxy_map_gui");
        nifty.setIgnoreKeyboardEvents(true);
        StarMapController.updateStarmapLabels(world);
    }

    @Override
    public void returnTo(World world) {
        enter(world);
    }

    public GalaxyMapObject getObjectAt(int x, int y) {
        if (x < 0 || x >= tilesX || y < 0 || y >= tilesY) {
            return null;
        }
        final int idx = map[y][x];
        if (idx != -1) {
            return objects.get(idx);
        }
        return null;
    }

    private void readObject(ObjectInputStream is) {
        try {
            is.defaultReadObject();
            fullMapScreen = new GalaxyMapScreen();
        } catch (Exception e) {
            logger.error("Error on GalaxyMap deserialization", e);
        }
    }

    @Override
    public void update(GameContainer container, World world) {
        final Input input = container.getInput();
        final GUI gui = GUI.getInstance();

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.MAP))) {
            gui.pushCurrentScreen();
            gui.getNifty().gotoScreen("star_map_screen");
            return;
        }

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.JOURNAL))) {
            gui.pushCurrentScreen();
            gui.getNifty().gotoScreen("journal_screen");
            return;
        }

        super.update(container, world);
        final int y = player.getShip().getY();
        final int x = player.getShip().getX();

        int idx;
        if (y >= 0 && x >= 0 && y < tilesY && x < tilesX) {
            idx = map[y][x];
        } else {
            idx = -1;
        }

        boolean hasEnterableObject = false;
        if (idx != -1) {
            if (world.isUpdatedThisFrame()) {
                objects.get(idx).processCollision(container, player);
            }

            if (objects.get(idx).canBeEntered()) {
                hasEnterableObject = true;
                if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY))) {
                    enterRoom(objects.get(idx));
                }
            }
        }

        // if user ship is at planet, show additional gui panel
        final Element scanLandPanel = GUI.getInstance().getNifty().getScreen("galaxy_map_gui").findElementByName("interactPanel");
        if (scanLandPanel != null) {
            boolean landPanelVisible = scanLandPanel.isVisible();
            if (!hasEnterableObject && landPanelVisible) {
                scanLandPanel.setVisible(false);
            } else if (hasEnterableObject && !landPanelVisible) {
                scanLandPanel.setVisible(true);
            }
        }
        if (world.isUpdatedThisFrame()) {
            StarMapController.updateStarmapLabels(world);
        }
    }

    public void enterStarsystemAtPlayerCoordinates() {
        final int y = player.getShip().getY();
        final int x = player.getShip().getX();
        int idx;
        if (isValidCoord(x, y)) {
            idx = map[y][x];
        } else {
            idx = -1;
        }
        if (idx != -1 && objects.get(idx).canBeEntered()) {
            enterRoom(objects.get(idx));
        }
    }

    private void enterRoom(GalaxyMapObject object) {
        Room r = (Room) object;
        world.setCurrentRoom(r);
        r.enter(world);
        world.setUpdatedThisFrame(true);
    }

    public StarSystem getRandomNonQuestStarsystemInRange(int x, int y, int range, StarSystemListFilter filter) {
        ProbabilitySet<StarSystem> resultSet = new ProbabilitySet<>();
        for (GalaxyMapObject obj : objects) {
            if (!StarSystem.class.isAssignableFrom(obj.getClass())) {
                continue;
            }

            StarSystem ss = (StarSystem) obj;
            if (ss.isQuestLocation()) {
                continue;
            }

            if (filter != null && !filter.filter(ss)) {
                continue;
            }

            if (BasePositionable.getDistance(x, y, obj.getX(), obj.getY()) < range) {
                resultSet.put(ss, 1.0);
            }
        }

        if (resultSet.isEmpty()) {
            return null;
        }
        return resultSet.getRandom();
    }

    public StarSystem getClosestStarSystem(StarSystem s) {
        StarSystem result = null;
        for (GalaxyMapObject obj : objects) {
            if (!StarSystem.class.isAssignableFrom(obj.getClass())) {
                continue;
            }

            if (obj == s) {
                continue;
            }

            if (result == null || getDistance(result, s) > getDistance((StarSystem) obj, s)) {
                result = (StarSystem) obj;
            }
        }

        return result;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        if (background == null) {
            createBackground(this.world.getCamera(), tilesX, tilesY);
        }
        background.draw(graphics, camera);
        for (int i = 0; i < tilesY; ++i) {
            for (int j = 0; j < tilesX; ++j) {
                if (map[i][j] != -1) {
                    GalaxyMapObject obj = objects.get(map[i][j]);
                    obj.drawOnGlobalMap(container, graphics, camera, j, i);
                }
            }
        }

        Ship ship = World.getWorld().getPlayer().getShip();
        drawPlayerMarkDirection(ship, graphics, camera);
        ship.draw(container, graphics, camera, world);
    }

    private void drawPlayerMarkDirection(Ship ship, Graphics graphics, Camera camera) {
        if(ship.getHull() <= 0){
            return;
        }
        if(playerMarkSystem == null){
            return;
        }
        if(getDistance(ship, playerMarkSystem) < 2.0){
            return;
        }

        Image img = ResourceManager.getInstance().getImage("direct_arrow");
        if(img == null){
            return;
        }

        final double x1 = playerMarkSystem.getX();
        final double y1 = playerMarkSystem.getY();
        final double x2 = ship.getX();
        final double y2 = ship.getY();

        float angle = (float)(360.0 - 180.0 / Math.PI * Math.atan2(x2 - x1, y2 - y1));
        img.setRotation(angle);
        graphics.drawImage(img, camera.getXCoord(ship.getX()) + ship.getOffsetX() - 16, camera.getYCoord(ship.getY()) + ship.getOffsetY() - 16);
    }

    @Override
    public List<GameObject> getObjects() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Effect> getEffects() {
        return Collections.emptyList();
    }

    @Override
    public Object getUserData() {
        return null;
    }

    @Override
    public void setUserData(Object o) {

    }

    @Override
    public String getStepSound(int x, int y) {
        return null;
    }

    /**
     * Never remove anything from this collection!
     * If you need to remove object from map just set its map[x][y] = -1
     * Removing object from list will lead to all indices becoming shifted by one
     */
    public List<GalaxyMapObject> getGalaxyMapObjects() {
        return objects;
    }

    public void setTileAt(int x, int y, int val) {
        map[y][x] = val;
    }

    public void addObjectAndSetTile(GalaxyMapObject object, int x, int y) {
        objects.add(object);
        while (map[y][x] != -1) {
            if (y < tilesY - 1) {
                ++y;
            } else {
                ++x;
            }
        }
        object.setPos(x, y);
        map[y][x] = objects.size() - 1;
    }

    public boolean isValidCoord(int x, int y) {
        return x >= 0 && x < tilesX && y >= 0 && y < tilesY;
    }

    public void addObjectAtDistance(GalaxyMapObject object, Positionable center, int distance) {
        int x;
        int y;
        int iterCount = 0;
        do {
            x = CommonRandom.getRandom().nextInt(2 * distance) - distance;
            y = (int) (Math.sqrt(distance * distance - x * x) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));
            if (++iterCount > 5) {
                logger.warn("Can not find suitable position for space object {}", object);
                distance--;

            }
        } while (!isValidCoord(center.getX() + x, center.getY() + y));
        addObjectAndSetTile(object, center.getX() + x, center.getY() + y);
    }

    @Override
    public int getWidthInTiles() {
        return tilesX;
    }

    @Override
    public int getHeightInTiles() {
        return tilesY;
    }


    public void setMapMark(StarSystem system){
        playerMarkSystem = system;
    }

    public StarSystem getMapMark(){
        return playerMarkSystem;
    }
}

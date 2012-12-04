/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:12
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Galaxy map.
 * Map is represented as a list of all static objects (like star systems), and a 2d array of cells, where each cell is either -1
 * (empty sector of space) or index of an element in this array.
 */
public class GalaxyMap extends BaseSpaceRoom {

    private List<GalaxyMapObject> objects = new ArrayList<GalaxyMapObject>();

    private GalaxyMapScreen fullMapScreen = new GalaxyMapScreen();

    private int[][] map;

    private int tilesX;

    private int tilesY;

    public static final int maxStars = 5;

    private Random r = new Random();

    public GalaxyMap(int tilesX, int tilesY) {
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        map = new int[tilesY][tilesX];
        for (int i = 0; i < tilesY; ++i) {
            for (int j = 0; j < tilesX; ++j) {
                map[i][j] = -1;
            }
        }

        // adding special objects that exist only in single instance, but are used frequently.
        // 0 is Nebula
        objects.add(new Nebula());

        // now generate random star systems
        for (int i = 0; i < maxStars; ++i) {
            int x;
            int y;
            do {
                x = r.nextInt(tilesX);
                y = r.nextInt(tilesY);
            } while (map[y][x] != -1);
            final int idx = objects.size();
            objects.add(generateRandomStarSystem(x, y));
            map[y][x] = idx;
        }
    }

    public int[][] getMap() {
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
    }

    private StarSystem generateRandomStarSystem(int x, int y) {
        int size = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        JGColor starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        final int planetCount = r.nextInt(5);
        Planet[] planets = new Planet[planetCount];
        StarSystem ss = new StarSystem(new StarSystem.Star(size, starColor), x, y);
        for (int i = 0; i < planetCount; ++i) {
            //todo: planet coordinate generation
            planets[i] = new Planet(ss, CollectionUtils.selectRandomElement(PlanetCategory.values()), CollectionUtils.selectRandomElement(PlanetAtmosphere.values()), r.nextInt(3) + 1, r.nextInt(25), r.nextInt(20));
        }
        ss.setPlanets(planets);
        return ss;
    }

    public GalaxyMapObject getObjectAt(int x, int y) {
        final int idx = map[y][x];
        if (idx != -1) {
            return objects.get(idx);
        }
        return null;
    }


    @Override
    public void update(JGEngine engine, World world) {
        if (engine.getLastKeyChar() == 'm') {
            world.setCurrentRoom(fullMapScreen);
            fullMapScreen.enter(world);
            engine.clearLastKey();
            return;
        }
        super.update(engine, world);
        int idx = map[player.getShip().getY()][player.getShip().getX()];
        if (idx != -1) {
            if (world.isUpdatedThisFrame()) {
                objects.get(idx).processCollision(engine, player);
            }
            if (objects.get(idx).canBeEntered() && engine.getKey(JGEngine.KeyEnter)) {
                Room r = (Room) objects.get(idx);
                world.setCurrentRoom(r);
                r.enter(world);
                world.setUpdatedThisFrame(true);
            }
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        super.draw(engine, camera);
        for (int i = 0; i < tilesY; ++i) {
            for (int j = 0; j < tilesX; ++j) {
                if (map[i][j] != -1) {
                    GalaxyMapObject obj = objects.get(map[i][j]);
                    if (j == player.getShip().getX() && i == player.getShip().getY() && obj.canBeEntered()) {
                        GameLogger.getInstance().addStatusMessage("Press <enter> to enter location");
                    }
                    obj.drawOnGlobalMap(engine, camera, j, i);
                }
            }
        }
    }
}

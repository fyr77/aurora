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
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
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

    public static final int maxStars = 15;

    private static final Random r = new Random();

    public GalaxyMap(int tilesX, int tilesY, int systemSizeX, int systemSizeY) {
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

        AlienRace gardenerRace = new AlienRace("Gardeners", 8, null, new Dialog("gardener_dialog"
                , new Dialog.Statement(0, "Greetings, human",
                        new Dialog.Reply(1, "You speak our language and know who we are? How?")
                        , new Dialog.Reply(2, "Greetings. We represent Alliance of Humanity, and who are you?"))
                , new Dialog.Statement(1, "I have met your species before. We are always curious about new civilizations, so we always try to communicate newcomers",
                        new Dialog.Reply(-1, "Ok"))
                , new Dialog.Statement(2, "Others call as Gardeners. Mostlty because we fly these tree-ships, but also our ways have some similarity with gardener work",
                        new Dialog.Reply(-1, "Ok"))
        ));

        objects.add(HomeworldGenerator.generateGardenerHomeworld(5, 5, systemSizeX, systemSizeY, gardenerRace));
        map[5][5] = objects.size() - 1;


        // now generate random star systems
        for (int i = 0; i < maxStars; ++i) {
            int x;
            int y;
            do {
                x = r.nextInt(tilesX);
                y = r.nextInt(tilesY);
            } while (map[y][x] != -1);
            final int idx = objects.size();
            objects.add(generateRandomStarSystem(x, y, systemSizeX, systemSizeY));
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

    public static StarSystem generateRandomStarSystem(int x, int y, int maxSizeX, int maxSizeY) {
        int size = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        JGColor starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        final int planetCount = r.nextInt(5);
        Planet[] planets = new Planet[planetCount];
        StarSystem ss = new StarSystem(new StarSystem.Star(size, starColor), x, y);
        for (int i = 0; i < planetCount; ++i) {
            int radius = r.nextInt(5) + 1;

            int planetX = r.nextInt(2 * radius) - radius;

            int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (r.nextBoolean() ? -1 : 1));

            planets[i] = new Planet(
                    ss
                    , CollectionUtils.selectRandomElement(PlanetCategory.values())
                    , CollectionUtils.selectRandomElement(PlanetAtmosphere.values())
                    , r.nextInt(3) + 1
                    , maxSizeX / 2 + planetX
                    , maxSizeY / 2 + planetY
                    , true);
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
        player.addGlobalStatus();
    }
}

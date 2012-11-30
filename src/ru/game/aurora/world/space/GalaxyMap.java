/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:12
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.JGPoint;
import jgame.platform.JGEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Galaxy map.
 * Map is represented as a list of all static objects (like star systems), and a 2d array of cells, where each cell is either -1
 * (empty sector of space) or index of an element in this array.
 */
public class GalaxyMap extends BaseSpaceRoom
{

    private List<GalaxyMapObject> objects = new ArrayList<GalaxyMapObject>();

    private int[][] map;

    private int tilesX;

    private int tilesY;

    public static final int maxStars = 5;

    private Random r = new Random();

    public GalaxyMap(int tilesX, int tilesY)
    {
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
            final int idx = objects.size();
            objects.add(generateRandomStarSystem());

            int x;
            int y;
            do {
                x = r.nextInt(tilesX);
                y = r.nextInt(tilesY);
            } while (map[y][x] != -1);
            map[y][x] = idx;
        }
    }

    private StarSystem generateRandomStarSystem()
    {
        int size = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        JGColor starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        return new StarSystem(new StarSystem.Star(size, starColor));
    }


    @Override
    public void update(JGEngine engine) {
        super.update(engine);
        JGPoint point = engine.getTileIndex(player.getShip().getLastX(),  player.getShip().getLastY());
        int idx = map[point.y][point.x];
        if (idx != -1) {
            objects.get(idx).processCollision(engine, player);
        }
    }

    @Override
    public void draw(JGEngine engine) {
        super.draw(engine);
        for (int i = 0; i < tilesY; ++i) {
            for (int j = 0; j < tilesX; ++j) {
                if (map[i][j] != -1) {
                    objects.get(map[i][j]).drawOnGlobalMap(engine, j, i);
                }
            }
        }
    }
}

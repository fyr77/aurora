/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:12
 */
package ru.game.aurora.world.space;

import jgame.platform.JGEngine;

import java.util.ArrayList;
import java.util.List;

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
    }


    @Override
    public void update(JGEngine engine) {
        super.update(engine);
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

/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:13
 */
package ru.game.aurora.world.planet;

import jgame.JGColor;
import jgame.platform.JGEngine;

public class SurfaceTypes {

    // constants for surface tiles
    // negative number means tile was not yet seen
    public static final byte DIRT = 1;

    public static final byte ROCKS = 2;

    public static final byte WATER = 3;

    public static final byte ICE = 4;

    public static final byte MOUNTAINS = 5;

    /**
     * Draws surface tiles in simple form - no sprites, just a square of corresponding color. Used in map screen
     */
    public static void drawSimple(byte type, int screenX, int screenY, int width, int heght, JGEngine engine)
    {
        JGColor color;
        switch (type) {
            case SurfaceTypes.DIRT:
                color = JGColor.orange;
                break;
            case SurfaceTypes.ICE:
                color = JGColor.white;
                break;
            case SurfaceTypes.ROCKS:
                color = JGColor.grey;
                break;
            case SurfaceTypes.WATER:
                color = JGColor.blue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported surface tile type " + type);
        }
        engine.setColor(color);
        engine.drawRect(screenX, screenY, width, heght, true, false);
    }

    /**
     * Draws surface tiles in simple form - no sprites, just a square of corresponding color. Used in map screen
     */
    public static void drawDetailed(byte type, int screenX, int screenY, int width, int heght, JGEngine engine)
    {
        String spriteName;
        switch (type) {
            case SurfaceTypes.DIRT:
                spriteName = "sand_tile_1";
                break;
            case SurfaceTypes.ICE:
                spriteName = "snow_tile_1";
                break;
            case SurfaceTypes.ROCKS:
                spriteName = "rock_tile_1";
                break;
            case SurfaceTypes.WATER:
                spriteName = "water_tile_1";
                break;
            default:
                throw new IllegalArgumentException("Unsupported surface tile type " + type);
        }
        engine.drawImage(spriteName, screenX, screenY);
    }
}

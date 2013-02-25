/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:13
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.planet.nature.Animal;

public class SurfaceTypes {

    // constants for surface tiles
    // negative number means tile was not yet seen
    public static final byte DIRT = 0x01;

    public static final byte ROCKS = 0x02;

    public static final byte WATER = 0x04;

    public static final byte ICE = 0x08;

    public static final byte OBSTACLE_MASK = 0x40;

    public static final byte MOUNTAINS_MASK = 0x20;

    public static final byte VISIBILITY_MASK = (byte) 0x80;

    /**
     * Draws surface tiles in simple form - no sprites, just a square of corresponding color. Used in map screen
     */
    public static void drawSimple(byte type, int screenX, int screenY, int width, int heght, Graphics graphics) {
        Color color;
        switch (type & 0x0F) {
            case SurfaceTypes.DIRT:
                color = Color.orange;
                break;
            case SurfaceTypes.ICE:
                color = Color.white;
                break;
            case SurfaceTypes.ROCKS:
                color = Color.gray;
                break;
            case SurfaceTypes.WATER:
                color = Color.blue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported surface tile type " + type);
        }
        graphics.setColor(color);
        graphics.fillRect(screenX, screenY, width, heght);
        if ((type & SurfaceTypes.MOUNTAINS_MASK) != 0) {
            graphics.setColor(Color.black);
            graphics.drawLine(screenX, screenY + heght, screenX + width / 2, screenY);
            graphics.drawLine(screenX + width, screenY + heght, screenX + width / 2, screenY);
        }
    }

    /**
     * Draws surface tiles in simple form - no sprites, just a square of corresponding color. Used in map screen
     */
    public static void drawDetailed(byte type, int screenX, int screenY, int width, int heght, Graphics graphics) {
        String spriteName;
        switch (type & 0x0F) {
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

        graphics.drawImage(ResourceManager.getInstance().getImage(spriteName), screenX, screenY);
    }

    public static boolean isMountain(byte b)
    {
        return (b & MOUNTAINS_MASK) != 0;
    }

    public static boolean isPassible(LandingParty party, byte tileType) {
        return isPassible(tileType);
    }

    public static boolean isPassible(Animal animal, byte tileType) {
        return isPassible(tileType);
    }

    public static boolean isPassible(byte tileType) {
        return (tileType & OBSTACLE_MASK) == 0;
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.07.13
 * Time: 15:06
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;

import java.util.HashMap;
import java.util.Map;

// draws tiles with beautiful borders between
public class TileDrawer {
    private final String tileName;

    private Map<Integer, String> sprites = new HashMap<Integer, String>();

    private byte mySurfaceType;

    public TileDrawer(String tileName, byte mySurfaceType) {
        this.tileName = tileName;
        this.mySurfaceType = mySurfaceType;

        sprites.put(31, tileName + "_4");
        sprites.put(11, tileName + "_5");
        sprites.put(107, tileName + "_6");
        sprites.put(127, tileName + "_7");
        sprites.put(22, tileName + "_8");
        sprites.put(214, tileName + "_9");
        sprites.put(223, tileName + "_10");
        sprites.put(251, tileName + "_11");
        sprites.put(248, tileName + "_12");
        sprites.put(208, tileName + "_13");
        sprites.put(254, tileName + "_14");
        sprites.put(104, tileName + "_15");
    }

    public void drawTile(Graphics graphics, Camera camera, int tileY, int tileX, boolean left, boolean right, boolean up, boolean down, boolean downLeft, boolean downRight, boolean upLeft, boolean upRight) {
        int number = 0;
        if (upLeft) {
            number |= 11;
        }
        if (up) {
            number |= 31;
        }

        if (upRight) {
            number |= 22;
        }

        if (left) {
            number |= 107;
        }

        if (right) {
            number |= 214;
        }

        if (downLeft) {
            number |= 104;
        }

        if (down) {
            number |= 248;
        }

        if (downRight) {
            number |= 208;
        }
        if (number == 0) {
            return;
        }
        String name = sprites.get(number);
        if (name != null) {
            graphics.drawImage(ResourceManager.getInstance().getImage(name), camera.getXCoord(tileX), camera.getYCoord(tileY));
        }
    }

    public void drawTile(Graphics graphics, Camera camera, Planet planet, int tileY, int tileX) {
        if (SurfaceTypes.sameBaseSurfaceType(planet.surface[tileY][tileX], mySurfaceType)) {
            return;
        }
        boolean left = SurfaceTypes.sameBaseSurfaceType(planet.surface[tileY][planet.wrapX(tileX - 1)], mySurfaceType);
        boolean right = SurfaceTypes.sameBaseSurfaceType(planet.surface[tileY][planet.wrapX(tileX + 1)], mySurfaceType);
        boolean up = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY - 1)][tileX], mySurfaceType);
        boolean down = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY + 1)][tileX], mySurfaceType);

        boolean downLeft = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY + 1)][planet.wrapX(tileX - 1)], mySurfaceType);
        boolean downRight = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY + 1)][planet.wrapX(tileX + 1)], mySurfaceType);
        boolean upLeft = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY - 1)][planet.wrapX(tileX - 1)], mySurfaceType);
        boolean upRight = SurfaceTypes.sameBaseSurfaceType(planet.surface[planet.wrapY(tileY - 1)][planet.wrapX(tileX + 1)], mySurfaceType);

        drawTile(graphics, camera, tileY, tileX, left, right, up, down, downLeft, downRight, upLeft, upRight);
    }
}

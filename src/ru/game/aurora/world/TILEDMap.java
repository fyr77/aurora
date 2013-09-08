package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.SurfaceTypes;

/**
 * Map in a format of a TILed EDitor
 */
public class TILEDMap implements ITileMap {
    private byte[][] flags;

    private TiledMap map;

    public TILEDMap(String mapRef) throws SlickException {
        map = new TiledMap(mapRef);
        flags = new byte[map.getHeight()][map.getWidth()];
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        for (int i = camera.getTarget().getY() - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() + camera.getNumTilesY() / 2; ++i) {
            for (int j = camera.getTarget().getX() - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() + camera.getNumTilesX() / 2; ++j) {
                if (i < 0 || j < 0 || i >= map.getHeight() || j >= map.getHeight()) {
                    continue;
                }
                final Image image = map.getTileImage(j, i, 0);
                graphics.drawImage(image, camera.getXCoord(j), camera.getYCoord(i));

            }
        }
    }

    @Override
    public boolean isTilePassable(int x, int y) {
        return SurfaceTypes.isPassible(flags[y][x]);
    }

    @Override
    public boolean isTilePassable(LandingParty landingParty, int x, int y) {
        return isTilePassable(x, y);
    }

    @Override
    public boolean isTileVisible(int x, int y) {
        return true;
    }

    @Override
    public int getWidth() {
        return map.getWidth();
    }

    @Override
    public int getHeight() {
        return map.getHeight();
    }

    @Override
    public int updateVisibility(int x, int y, int range) {
        int rz = 0;
        for (int i = y - range; i <= y + range; ++i) {
            for (int j = x - range; j <= x + range; ++j) {
                if (i < 0 || j < 0 || i >= map.getHeight() || j >= map.getHeight()) {
                    continue;
                }
                if (0 == (SurfaceTypes.VISIBILITY_MASK & flags[i][j])) {
                    flags[i][j] |= SurfaceTypes.VISIBILITY_MASK;
                    ++rz;
                }
            }
        }
        return rz;
    }

    @Override
    public void setTilePassable(int x, int y, boolean isPassable) {
        if (!isPassable) {
            flags[y][x] |= SurfaceTypes.OBSTACLE_MASK;
        } else {
            flags[y][x] &= ~SurfaceTypes.OBSTACLE_MASK;
        }
    }
}

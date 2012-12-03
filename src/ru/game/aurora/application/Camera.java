/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 03.12.12
 * Time: 12:25
 */
package ru.game.aurora.application;

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.world.Positionable;

/**
 * Camera defines coordinate transformation from in-game global coordinates to screen coordinates
 * Has position. A tile that has coordinates equal to this position is drawn in the middle of a screen
 */
public class Camera {

    /**
     * Coordinates for upper-left corner of draw area
     */
    private int viewportX;

    private int viewportY;

    /**
     * Number of tiles that are actually drawn
     */
    private int viewportTilesX;

    private int viewportTilesY;

    public static class FixedPosition implements Positionable {
        private final int x;
        private final int y;

        public FixedPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void setPos(int newX, int newY) {
            // empty
        }
    }

    public Camera(int viewportX, int viewportY, int vieportWidth, int viewportHeight, JGEngine engine) {
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.viewportTilesX = vieportWidth;
        this.viewportTilesY = viewportHeight;
        this.engine = engine;
    }

    private JGEngine engine;

    /**
     * Object camera is following
     */
    private Positionable target;

    public void setTarget(Positionable target) {
        this.target = target;
    }

    /**
     * Get absolute screen x coordinate for tile that has given x index
     *
     * @param globalTileX Tile horizontal index
     * @return Absolute screen x coordinate for this tile
     */
    public int getXCoord(int globalTileX) {
        return viewportX + engine.tileWidth() * (viewportTilesX / 2 + (globalTileX - target.getX()));
    }

    public int getYCoord(int globalTileY) {
        return viewportTilesY + engine.tileHeight() * (viewportTilesY / 2 + (globalTileY - target.getY()));
    }

    public int getNumTilesX() {
        return viewportTilesX;
    }

    public int getNumTilesY() {
        return viewportTilesY;
    }

    public Positionable getTarget() {
        return target;
    }

    public boolean isInViewport(int tileX, int tileY) {
        return Math.abs(target.getX() - tileX) <= getNumTilesX() / 2 && Math.abs(target.getY() - tileY) <= getNumTilesY() / 2;
    }

    public void drawBound() {
        engine.setColor(JGColor.blue);
        engine.drawRect(viewportX, viewportY, viewportTilesX * engine.tileWidth(), viewportTilesY * engine.tileHeight(), false, false);
    }
}

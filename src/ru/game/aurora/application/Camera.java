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

    private int tileWidth;

    private int tileHeight;

    /**
     * Number of tiles that are actually drawn
     */
    private int viewportTilesX;

    private int viewportTilesY;

    public Camera(int viewportX, int viewportY, int vieportWidth, int viewportHeight, JGEngine engine) {
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.viewportTilesX = vieportWidth;
        this.viewportTilesY = viewportHeight;
        this.engine = engine;
        this.tileHeight = engine.tileHeight();
        this.tileWidth = engine.tileWidth();
    }

    public Camera(int viewportX, int viewportY, int tileWidth, int tileHeight, int viewportTilesX, int viewportTilesY, JGEngine engine) {
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.viewportTilesX = viewportTilesX;
        this.viewportTilesY = viewportTilesY;
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
        return viewportX + tileWidth * (viewportTilesX / 2 + (globalTileX - target.getX()));
    }

    public int getYCoord(int globalTileY) {
        return viewportTilesY + tileHeight * (viewportTilesY / 2 + (globalTileY - target.getY()));
    }

    // same but for absolute coordinate (not tile)
    public int getXCoordPoint(int pointX) {
        return pointX - (target.getX() - viewportTilesX / 2) * tileWidth;
    }

    public int getYCoordPoint(int pointY) {
        return pointY - (target.getY() - viewportTilesY / 2) * tileHeight;
    }

    /**
     * Return coordinates of given tile on screen
     */
    public int getRelativeX(int tileX) {
        return viewportTilesX + tileWidth * tileX;
    }

    public int getRelativeY(int tileY) {
        return viewportTilesY + tileHeight * tileY;
    }

    public int getXCoordWrapped(int tileX, int totalTilesX) {
        final int targetX = target.getX();
        if (totalTilesX - Math.abs(targetX - tileX) > viewportTilesX / 2) {
            return getXCoord(tileX);
        }

        if (targetX < tileX) {
            return getXCoord(-totalTilesX + tileX);
        } else {
            return getXCoord(tileX + totalTilesX);
        }
    }

    public int getYCoordWrapped(int tileY, int totalTilesY) {
        final int targetY = target.getY();
        if (totalTilesY - Math.abs(targetY - tileY) > viewportTilesY / 2) {
            return getYCoord(tileY);
        }

        if (targetY < tileY) {
            return getYCoord(-totalTilesY + tileY);
        } else {
            return getYCoord(tileY + totalTilesY);
        }

    }

    public int getNumTilesX() {
        return viewportTilesX;
    }

    public int getNumTilesY() {
        return viewportTilesY;
    }

    public int getTileWidth() {
        return engine.tileWidth();
    }

    public int getTileHeight() {
        return engine.tileHeight();
    }

    public Positionable getTarget() {
        return target;
    }

    public boolean isInViewport(int tileX, int tileY) {
        return Math.abs(target.getX() - tileX) <= getNumTilesX() / 2 && Math.abs(target.getY() - tileY) <= getNumTilesY() / 2;
    }

    public void drawBound() {
        engine.setColor(JGColor.blue);
        engine.drawRect(viewportX, viewportY, viewportTilesX * tileWidth, viewportTilesY * tileHeight, false, false);
    }

    public int getViewportX() {
        return viewportX;
    }

    public int getViewportY() {
        return viewportY;
    }

    public int getViewportTilesX() {
        return viewportTilesX;
    }

    public int getViewportTilesY() {
        return viewportTilesY;
    }

    public JGEngine getEngine() {
        return engine;
    }

}

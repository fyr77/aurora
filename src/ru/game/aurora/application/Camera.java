/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 03.12.12
 * Time: 12:25
 */
package ru.game.aurora.application;

import ru.game.aurora.world.IMovable;

import java.io.Serializable;

/**
 * Camera defines coordinate transformation from in-game global coordinates to screen coordinates
 * Has position. A tile that has coordinates equal to this position is drawn in the middle of a screen
 */
public class Camera implements Serializable {
    private static final long serialVersionUID = 9034031741857618988L;
    /**
     * Coordinates for upper-left corner of draw area
     */
    private int viewportX;
    private int viewportY;

    private int initialViewportX;
    private int initialViewportY;

    private float tileWidth;

    private float tileHeight;

    /**
     * Object camera is following
     */
    private IMovable target;

    /**
     * Number of tiles that are actually drawn
     */
    private int viewportTilesX;

    private int viewportTilesY;

    public Camera(int viewportX, int viewportY, int vieportWidth, int viewportHeight, float tileWidth, float tileHeight) {
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.initialViewportX = viewportX;
        this.initialViewportY = viewportY;
        this.viewportTilesX = vieportWidth;
        this.viewportTilesY = viewportHeight;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
    }

    public void setTarget(IMovable target) {
        this.target = target;
    }

    /**
     * Get absolute screen x coordinate for tile that has given x index
     *
     * @param globalTileX Tile horizontal index
     * @return Absolute screen x coordinate for this tile
     */
    public float getXCoord(int globalTileX) {
        return viewportX + tileWidth * (viewportTilesX / 2 + (globalTileX - target.getX())) - target.getOffsetX();
    }

    public float getYCoord(int globalTileY) {
        return viewportY + tileHeight * (viewportTilesY / 2 + (globalTileY - target.getY())) - target.getOffsetY();
    }

    // same but for absolute coordinate (not tile)
    public float getXCoordPoint(int pointX) {
        return pointX - (target.getX() - viewportTilesX / 2) * tileWidth - target.getOffsetX();
    }

    public float getYCoordPoint(int pointY) {
        return pointY - (target.getY() - viewportTilesY / 2) * tileHeight - target.getOffsetY();
    }

    /**
     * Return coordinates of given tile on screen
     */
    public float getRelativeX(float tileX) {
        return viewportTilesX + tileWidth * tileX;
    }

    public float getRelativeY(float tileY) {
        return viewportTilesY + tileHeight * tileY;
    }

    public float getXCoordWrapped(int tileX, int totalTilesX) {
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

    public float getYCoordWrapped(int tileY, int totalTilesY) {
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

    public float getTileWidth() {
        return tileWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public int getPointTileX(int x) {
        if (x < viewportX || x > viewportX + viewportTilesX * tileWidth) {
            return -1;
        }
        return Math.round((x - viewportX) / tileWidth);
    }

    public int getPointTileY(int y) {
        if (y < viewportY || y > viewportY + viewportTilesY * tileHeight) {
            return -1;
        }
        return Math.round((y - viewportY) / tileHeight);
    }

    public IMovable getTarget() {
        return target;
    }

    /**
     * Check that center of given tile is now on screen
     */
    public boolean isInViewport(int tileX, int tileY) {
        return Math.abs(target.getX() - tileX) <= getNumTilesX() / 2 && Math.abs(target.getY() - tileY) <= getNumTilesY() / 2;
    }

    public boolean isInViewportAbs(float absX, float absY) {
        return isInViewport((int) (absX / tileWidth), (int) (absY / tileHeight));
    }

    public boolean isInViewportScreen(float x, float y) {
        return x >= 0 && x < getNumTilesX() * tileWidth && y >= 0 && y < getNumTilesY() * tileHeight;
    }

    public void drawBound() {
        //engine.setColor(JGColor.blue);
        //engine.drawRect(viewportX, viewportY, viewportTilesX * tileWidth, viewportTilesY * tileHeight, false, false);
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

    public void setViewportX(int viewportX) {
        this.viewportX = viewportX;
    }

    public void setViewportY(int viewportY) {
        this.viewportY = viewportY;
    }

    public void resetViewPort() {
        this.viewportX = initialViewportX;
        this.viewportY = initialViewportY;
    }
}

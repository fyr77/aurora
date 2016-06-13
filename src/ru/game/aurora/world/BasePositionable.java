/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 16:48
 */
package ru.game.aurora.world;

import ru.game.aurora.util.EngineUtils;

public class BasePositionable implements Positionable {

    private static final long serialVersionUID = -6804845897857713862L;

    protected int x;

    protected int y;

    protected int oldX = 0;
    protected int oldY = 0;

    public BasePositionable(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static double getDistance(Positionable first, Positionable second) {
        return getDistance(first.getX(), first.getY(), second.getX(), second.getY());
    }

    public static double getDistance(int x, int y, int otherX, int otherY) {
        return Math.sqrt(Math.pow(x - otherX, 2) + Math.pow(y - otherY, 2));
    }

    public static double getDistanceWrapped(int x, int y, int otherX, int otherY, int width, int height) {
        int directXDist = Math.abs(x - otherX);
        int wrapXDist = Math.abs(width - directXDist);

        int directYDist = Math.abs(y - otherY);
        int wrapYDist = Math.abs(height - directYDist);

        return Math.sqrt(Math.pow(Math.min(directXDist, wrapXDist), 2) + Math.pow(Math.min(directYDist, wrapYDist), 2));
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
        ITileMap map = World.getWorld().getCurrentRoom().getMap();
        if (map != null && map.isWrapped()) {
            newX = EngineUtils.wrap(newX, map.getWidthInTiles());
            newY = EngineUtils.wrap(newY, map.getHeightInTiles());
        }

        oldX = x;
        oldY = y;
        x = newX;
        y = newY;
    }

    public double getDistance(Positionable other) {
        return Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
    }

    public double getDistance(int otherX, int otherY) {
        return Math.sqrt(Math.pow(x - otherX, 2) + Math.pow(y - otherY, 2));
    }

    public double getDistanceWrapped(Positionable other, int width, int height) {
        return getDistanceWrapped(x, y, other.getX(), other.getY(), width, height);
    }

    public double getDistanceFromMap(ITileMap map, Positionable other) {
        if (map.isWrapped()) {
            return getDistanceWrapped(x, y, other.getX(), other.getY(), map.getWidthInTiles(), map.getHeightInTiles());
        } else {
            return getDistance(other);
        }
    }

    @Override
    public String toString() {
        return "BasePositionable{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

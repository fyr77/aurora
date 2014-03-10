/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 16:48
 */
package ru.game.aurora.world;

public class BasePositionable implements Positionable {

    private static final long serialVersionUID = -6804845897857713862L;

    protected int x;

    protected int y;

    public BasePositionable(int x, int y) {
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
        x = newX;
        y = newY;
    }

    public double getDistance(Positionable other) {
        return Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
    }

    public double getDistanceWrapped(Positionable other, int width, int height) {
        return getDistanceWrapped(x, y, other.getX(), other.getY(), width, height);
    }

    public double getDistanceWrapped(int x, int y, int otherX, int otherY, int width, int height) {
        int directXDist = Math.abs(x - otherX);
        int wrapXDist = Math.abs(width - directXDist);

        int directYDist = Math.abs(y - otherY);
        int wrapYDist = Math.abs(height - directYDist);

        return Math.sqrt(Math.pow(Math.min(directXDist, wrapXDist), 2) + Math.pow(Math.min(directYDist, wrapYDist), 2));
    }
}

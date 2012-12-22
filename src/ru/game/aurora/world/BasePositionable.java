/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 16:48
 */
package ru.game.aurora.world;

public class BasePositionable implements Positionable {
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
}

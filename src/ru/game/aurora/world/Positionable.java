package ru.game.aurora.world;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 03.12.12
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
public interface Positionable {
    public int getX();

    public int getY();

    public void setPos(int newX, int newY);
}

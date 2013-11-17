package ru.game.aurora.world;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.11.13
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */
public interface IMovable extends Positionable, GameObject {
    void moveUp();

    void moveDown();

    void moveRight();

    void moveLeft();

    float getOffsetX();

    float getOffsetY();

    boolean nowMoving();
}

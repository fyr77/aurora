package ru.game.aurora.world;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.11.13
 * Time: 18:57
 */
public interface IMovable extends Positionable {
    void moveUp();

    void moveDown();

    void moveRight();

    void moveLeft();

    float getOffsetX();

    float getOffsetY();

    boolean nowMoving();

    int getTargetX();

    int getTargetY();
}

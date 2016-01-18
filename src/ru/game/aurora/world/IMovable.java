package ru.game.aurora.world;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.11.13
 * Time: 18:57
 *
 * Base interface for objects that can move between tiles, controlled by some other entity like player or AI
 * After one of moveXXX() methods are called this objects starts moving from its current position to the next tile.
 * Generally getTargetX() should be used instead of getX() for such objects. So that e.g. shots fly to the tile this object is entering,
 * not to the tile it has left.
 */
public interface IMovable extends Positionable {
    /**
     * Start moving one tile up
     */
    void moveUp();

    /**
     * Start moving one tile down
     */
    void moveDown();

    /**
     * Start moving one tile right
     */
    void moveRight();

    /**
     * Start moving one tile left
     */
    void moveLeft();

    void moveTo(int newX, int newY);

    /**
     * Get distance in pixels between starting point (start tile X coordinate) and current position
     */
    float getOffsetX();

    /**
     * Get distance in pixels between starting point (start tile Y coordinate) and current position
     */
    float getOffsetY();

    /**
     * This object is moving now and is located somewhere between (x, y) and (targetX, targetY)
     */
    boolean nowMoving();

    /**
     * Get X of tile this object is moving to
     */
    int getTargetX();

    /**
     * Get Y of tile this object is moving to
     */
    int getTargetY();
}

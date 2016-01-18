package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.AuroraGame;

/**
 * Date: 11.11.13
 * Time: 0:05
 */

public class Movable extends BasePositionable implements IMovable, Updatable {

    private static final long serialVersionUID = 1L;

    protected boolean isMoving = false;

    private boolean canMove = true;

    private float offsetX, offsetY;

    protected int destinationX, destinationY;

    private static final int MOVE_FRAMES = 4;

    private int currentFrame = 0;

    private static final int FRAME_DURATION = 20; //ms

    private long lastCall;

    private int movementSpeed;

    public Movable(int x, int y) {
        super(x, y);
        destinationX = x;
        destinationY = y;
        movementSpeed = 1;
    }

    public void moveTo(int destinationX, int destinationY) {
        if (canMove) {
            isMoving = true;
            this.destinationX = destinationX;
            this.destinationY = destinationY;
        }
    }

    public void setMoveability(boolean m) {
        canMove = m;
    }

    @Override
    public void moveUp() {
        moveTo(x, y - movementSpeed);
    }

    @Override
    public void moveDown() {
        moveTo(x, y + movementSpeed);
    }

    @Override
    public void moveRight() {
        moveTo(x + movementSpeed, y);
    }

    @Override
    public void moveLeft() {
        moveTo(x - movementSpeed, y);
    }

    @Override
    public float getOffsetX() {
        return offsetX;
    }

    @Override
    public float getOffsetY() {
        return offsetY;
    }

    @Override
    public boolean nowMoving() {
        return isMoving;
    }

    @Override
    public void update(GameContainer container, World world) {
        doMove(container);
    }

    @Override
    public void setPos(int newX, int newY) {
        super.setPos(newX, newY);
        destinationX = newX;
        destinationY = newY;
    }

    protected void doMove(GameContainer container) {
        if (isMoving) {
            if (lastCall == 0) {
                lastCall = container.getTime();
            } else {
                if (lastCall <= (container.getTime() - FRAME_DURATION)) {
                    currentFrame++;
                    lastCall = container.getTime();
                    offsetX = (destinationX - x) * AuroraGame.tileSize * currentFrame / MOVE_FRAMES;
                    offsetY = (destinationY - y) * AuroraGame.tileSize * currentFrame / MOVE_FRAMES;
                    if (currentFrame >= MOVE_FRAMES) {
                        isMoving = false;
                        currentFrame = 0;
                        offsetX = 0;
                        offsetY = 0;
                        setPos(destinationX, destinationY);
                        lastCall = 0;
                    }
                }
            }
        }
    }

    @Override
    public int getTargetX() {
        return destinationX;
    }

    @Override
    public int getTargetY() {
        return destinationY;
    }

    public double getDistanceFromTargetPointWrapped(Positionable other, int width, int height) {
        return getDistanceWrapped(getTargetX(), getTargetY(), other.getX(), other.getY(), width, height);
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(int movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}

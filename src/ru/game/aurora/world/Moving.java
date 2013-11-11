package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;

/**
 * Date: 11.11.13
 * Time: 0:05
 */

public class Moving extends BasePositionable implements GameObject {

    private static final long serialVersionUID = 6904064070429741009L;

    private String sprite; //sometimes can be "none"

    private boolean isMoving = false;

    private float offsetX, offsetY;

    private int destinationX, destinationY;

    private static int MOVE_FRAMES = 4;

    private int currentFrame = 0;

    private static int FRAME_DURATION = 50; //ms

    private long lastCall;

    public Moving(int x, int y, String sprite) {
        super(x, y);
        this.sprite = sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    private void startMoving(int destinationX, int destinationY) {
        isMoving = true;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
    }

    public void moveUp() {
        startMoving(x,y - 1);
    }

    public void moveDown() {
        startMoving(x,y + 1);
    }

    public void moveRight() {
        startMoving(x + 1,y);
    }

    public void moveLeft() {
        startMoving(x - 1,y);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public boolean nowMoving() {
        return isMoving;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (isMoving) {
            if (lastCall == 0) {
                lastCall = container.getTime();
            } else {
                if (lastCall<=(container.getTime()- FRAME_DURATION)) {
                    currentFrame++;
                    lastCall = container.getTime();
                    offsetX = (destinationX - x)*AuroraGame.tileSize*currentFrame/MOVE_FRAMES;
                    offsetY = (destinationY - y)*AuroraGame.tileSize*currentFrame/MOVE_FRAMES;
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
    public void draw(GameContainer container, Graphics g, Camera camera) {
        if (!sprite.equals("none")) {
            g.drawImage(ResourceManager.getInstance().getImage(sprite), camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
        }
    }
}

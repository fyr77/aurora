package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;

/**
 * Date: 16.11.13
 * Time: 2:07
 */
public class MovableSprite extends Movable {
    private static final long serialVersionUID = -5465459906103379544L;

    protected String sprite;

    private boolean isFlipped = false;

    public MovableSprite(int x, int y, String sprite) {
        super(x, y);
        this.sprite = sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    @Override
    public void moveLeft() {
        super.moveLeft();
        isFlipped = true;
    }

    @Override
    public void moveRight() {
        super.moveRight();
        isFlipped = false;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(getImage(), camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
    }

    public Image getImage()
    {
        return isFlipped ? ResourceManager.getInstance().getFlippedImage(sprite) : ResourceManager.getInstance().getImage(sprite);
    }
}

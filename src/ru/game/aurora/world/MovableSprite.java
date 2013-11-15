package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;

/**
 * Date: 16.11.13
 * Time: 2:07
 */
public class MovableSprite extends Movable {
    private static final long serialVersionUID = -5465459906103379544L;

    private String sprite;

    public MovableSprite(int x, int y, String sprite) {
        super(x, y);
        this.sprite = sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(ResourceManager.getInstance().getImage(sprite), camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
    }
}

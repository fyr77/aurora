package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.common.Drawable;

/**
 * Date: 16.11.13
 * Time: 2:07
 */
public class MovableSprite extends Movable implements IDrawable {
    private static final long serialVersionUID = -1;

    protected Drawable drawable;

    private boolean isFlipped = false;

    public MovableSprite(int x, int y, Drawable d) {
        super(x, y);
        this.drawable = d;
    }

    public void setSprite(Drawable d) {
        this.drawable = d;
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
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        final ITileMap map = world.getCurrentRoom().getMap();
        if (map != null && map.isWrapped()) {
            g.drawImage(getImage(), camera.getXCoordWrapped(x, map.getWidthInTiles()) + getOffsetX(), camera.getYCoordWrapped(y, map.getHeightInTiles()) + getOffsetY());
        } else {
            g.drawImage(getImage(), camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
        }
    }

    public Image getImage() {
        return isFlipped ? drawable.getFlippedCopy() : drawable.getImage();
    }
}

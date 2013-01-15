/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 15:49
 */

package ru.game.aurora.effects;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;


public class BlasterShotEffect implements Effect {
    private Vector2f currentPos;

    private Vector2f movementDir;

    private Vector2f target;

    private int moveSpeed;

    private boolean isOver = false;

    private Image myImage;

    public BlasterShotEffect(Positionable source, Positionable target, Camera camera, int moveSpeed, String shotSprite) {
        this.currentPos = new Vector2f(camera.getXCoord(source.getX()) + camera.getTileWidth() / 2, camera.getYCoord(source.getY()) + camera.getTileHeight() / 2);
        this.target = new Vector2f(camera.getXCoord(target.getX()) + camera.getTileWidth() / 2, camera.getYCoord(target.getY()) + camera.getTileHeight() / 2);
        this.moveSpeed = moveSpeed;

        movementDir = new Vector2f(this.target.getX() - this.currentPos.getX(), this.target.getY() - this.currentPos.getY());
        movementDir.normalise();

        myImage = ResourceManager.getInstance().getImage(shotSprite);
        myImage.setRotation((float) movementDir.getTheta());
    }

    @Override
    public boolean isOver() {
        return isOver;
    }

    @Override
    public void update(GameContainer container, World world) {

        Vector2f distToTarget = new Vector2f(target.getX() - currentPos.getX(), target.getY() - currentPos.getY());
        if (distToTarget.length() < (float) moveSpeed / container.getFPS()) {
            isOver = true;
            return;
        }

        Vector2f delta = new Vector2f(movementDir.getX() * moveSpeed / container.getFPS(), movementDir.getY() * moveSpeed / container.getFPS());
        currentPos.add(delta);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(myImage, currentPos.x, currentPos.y);
    }
}

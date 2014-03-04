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
import ru.game.aurora.world.IMovable;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.space.StarSystem;


public class BlasterShotEffect extends Effect {
    private static final long serialVersionUID = 7969961076017675842L;

    protected Vector2f currentPos;

    private Vector2f movementDir;

    private Vector2f target;

    private int moveSpeed;

    private boolean isOver = false;

    private transient Image myImage;

    private String particlesAnimation;

    private String explosionAnimation;

    public BlasterShotEffect(IMovable source, IMovable target, Camera camera, int moveSpeed, StarshipWeapon weapon) {
        this(new Vector2f(camera.getXCoord(source.getX()) + source.getOffsetX() + camera.getTileWidth() / 2, camera.getYCoord(source.getY()) + camera.getTileHeight() / 2 + source.getOffsetY())
                , new Vector2f(camera.getXCoord(target.getX()) + target.getOffsetX() + camera.getTileWidth() / 2, camera.getYCoord(target.getY()) + target.getOffsetY() + camera.getTileHeight() / 2)
                ,
                moveSpeed
                , weapon);

    }

    public BlasterShotEffect(Positionable source, Positionable target, Camera camera, int moveSpeed, LandingPartyWeapon weapon) {
        this(new Vector2f(camera.getXCoord(source.getX()) + camera.getTileWidth() / 2, camera.getYCoord(source.getY()) + camera.getTileHeight() / 2)
                , new Vector2f(camera.getXCoord(target.getX()) + camera.getTileWidth() / 2, camera.getYCoord(target.getY()) + camera.getTileHeight() / 2)
                ,
                moveSpeed
                , weapon);

    }

    public BlasterShotEffect(Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, LandingPartyWeapon weapon) {
        this(new Vector2f(camera.getXCoord(source.getX()) + camera.getTileWidth() / 2, camera.getYCoord(source.getY()) + camera.getTileHeight() / 2)
                , new Vector2f(targetScreenX, targetScreenY)
                ,
                moveSpeed
                , weapon);
    }

    public BlasterShotEffect(Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, String weaponSprite) {
        this(new Vector2f(camera.getXCoord(source.getX()) + camera.getTileWidth() / 2, camera.getYCoord(source.getY()) + camera.getTileHeight() / 2)
                , new Vector2f(targetScreenX, targetScreenY)
                , moveSpeed
                ,
                weaponSprite);
    }

    public BlasterShotEffect(Vector2f source, Vector2f target, int moveSpeed, LandingPartyWeapon weapon) {
        this(source, target, moveSpeed, weapon.getShotImage());
    }

    public BlasterShotEffect(Vector2f source, Vector2f target, int moveSpeed, StarshipWeapon weapon) {
        this(source, target, moveSpeed, weapon.getWeaponDesc().shotSprite);
        particlesAnimation = weapon.getWeaponDesc().particlesAnimation;
        explosionAnimation = weapon.getWeaponDesc().explosionAnimation;
    }

    public BlasterShotEffect(Vector2f source, Vector2f target, int moveSpeed, String weaponSprite) {
        super(0, 0, LOW_PRIORITY, DrawOrder.BACK);
        this.currentPos = source;
        this.target = target;
        this.moveSpeed = moveSpeed;

        movementDir = new Vector2f(this.target.getX() - this.currentPos.getX(), this.target.getY() - this.currentPos.getY());
        movementDir.normalise();

        myImage = ResourceManager.getInstance().getImage(weaponSprite).copy();
        myImage.setRotation((float) movementDir.getTheta());
    }

    @Override
    public boolean isOver() {
        return isOver;
    }

    @Override
    public void update(GameContainer container, World world) {
        Vector2f distToTarget = new Vector2f(target.getX() - currentPos.getX(), target.getY() - currentPos.getY());
        if (distToTarget.length() < (float) moveSpeed / container.getFPS() || (!world.getCamera().isInViewportScreen(currentPos.getX(), currentPos.getY()) && !world.getCamera().isInViewportScreen(target.getX(), target.getY()))) {
            if (!isOver) {
                startHitAnimation(world);
            }
            return;
        }
        Vector2f delta = new Vector2f(movementDir.getX() * moveSpeed / container.getFPS(), movementDir.getY() * moveSpeed / container.getFPS());
        currentPos.add(delta);
        // todo: restore particle effects
        //if (world.getCurrentRoom().getClass().isAssignableFrom(StarSystem.class)) {
        //    if (!(particlesAnimation == null)) {
        //        ((StarSystem) world.getCurrentRoom()).addEffect(new ExplosionEffect((int) currentPos.x, (int) currentPos.y, particlesAnimation, true, false));
        //    }
        //}
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(myImage, currentPos.x - myImage.getWidth() / 2, currentPos.y - myImage.getHeight() / 2);
    }

    public void startHitAnimation(World world) {
        isOver = true;
        if (!world.getCamera().isInViewportScreen(currentPos.getX(), currentPos.getY())) {
            return;
        }
        if (world.getCurrentRoom().getClass().isAssignableFrom(StarSystem.class)) {
            if (!(explosionAnimation == null)) {
                ((StarSystem) world.getCurrentRoom()).addEffect(new ExplosionEffect((int) currentPos.x, (int) currentPos.y, explosionAnimation, true, false));
            }
        }

    }
}

package ru.game.aurora.effects;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GardenersShip;

/**
 * Date: 23.10.13
 * Time: 11:25
 */
public class WarpEffect extends Effect {

    private static final long serialVersionUID = 1L;

    private transient Animation anim;

    private boolean secondExplosion = false; //true when first effect is over

    private int curX;   //coordinates for animation

    private int curY;

    private GardenersShip ship;

    private float alpha;

    private long lastCall;

    private boolean isOver = false;

    public WarpEffect(GardenersShip ship) {
        super(ship.getX(), ship.getY(), HIGH_PRIORITY, DrawOrder.FRONT);
        this.ship = ship;
        anim = ResourceManager.getInstance().getAnimation("warp");
        anim.setLooping(false);
        curX = ship.getX();
        curY = ship.getY();
    }

    @Override
    public boolean isOver() {
        return isOver;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (lastCall == 0) {
            lastCall = container.getTime();
        }
        anim.update(container.getTime() - lastCall);

        alpha = (float) ((anim.getFrameCount() - anim.getFrame()) * (1.0 / anim.getFrameCount()));

        lastCall = container.getTime();

        if (anim.isStopped()) {
            world.getCurrentStarSystem().getShips().remove(ship);
            isOver = true;
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        ship.setAlpha(alpha);
        //32 - shipSize/2
        graphics.drawAnimation(anim, camera.getXCoord(curX) - (anim.getWidth() / 2) + 32, camera.getYCoord(curY) - (anim.getHeight() / 2) + 32);
    }
}

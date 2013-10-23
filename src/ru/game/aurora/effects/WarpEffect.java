package ru.game.aurora.effects;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

/**
 * Date: 23.10.13
 * Time: 11:25
 */
public class WarpEffect extends BasePositionable implements Effect {
    private transient Animation anim;

    private boolean secondExplosion = false; //true when first effect is over

    private int curX;   //coordinates for animation

    private int curY;

    private int destX;

    private int destY;

    private NPCShip ship;

    private float alpha;

    private long lastCall;

    private boolean isOver = false;

    public WarpEffect(NPCShip ship, int destX, int destY) {
        super(ship.getX(), ship.getY());
        curX = ship.getX();
        curY = ship.getY();
        this.ship = ship;
        this.destX = destX;
        this.destY = destY;
        anim = ResourceManager.getInstance().getAnimation("warp");
        anim.setLooping(false);
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
        if (!secondExplosion) {
            alpha = (float) ((anim.getFrameCount()-anim.getFrame())*(1.0/anim.getFrameCount()));
        } else {
            alpha = (float) ((anim.getFrame()+1)*(1.0/anim.getFrameCount()));
        }
        lastCall = container.getTime();
        if (anim.isStopped()){
            if (!secondExplosion) {
                curX = destX;
                curY = destY;
                secondExplosion = true;
                anim.restart();
            } else {
                isOver = true;
            }
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        ship.setAlpha(alpha);
        //32 - shipSize/2
        graphics.drawAnimation(anim, camera.getXCoord(curX)-(anim.getWidth()/2)+32, camera.getYCoord(curY)-(anim.getHeight()/2)+32);
    }
}

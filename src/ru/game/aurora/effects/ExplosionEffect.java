/**
 * User: jedi-philosopher
 * Date: 27.01.13
 * Time: 22:28
 */
package ru.game.aurora.effects;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;

public class ExplosionEffect extends Effect {
    private static final long serialVersionUID = -6865607525774879448L;

    private transient Animation anim;

    private long lastCall;

    private boolean screenCoords;

    /**
     * @param screenCoords If true, these x and y are already coordinates on screen and should not be transformed
     */
    public ExplosionEffect(int x, int y, String animName, boolean screenCoords, boolean playSound) {
        super(x, y, HIGHEST_PRIORITY, DrawOrder.FRONT);
        anim = ResourceManager.getInstance().getAnimation(animName).copy();
        anim.setLooping(false);
        this.screenCoords = screenCoords;

        if (playSound) {
            ResourceManager.getInstance().getSound("explosion_2").play();
        }
    }

    public Animation getAnim() {
        return anim;
    }

    @Override
    public boolean isOver() {
        return anim.isStopped();
    }

    @Override
    public void update(GameContainer container, World world) {
        if (lastCall == 0) {
            lastCall = container.getTime();
        }
        anim.update(container.getTime() - lastCall);
        lastCall = container.getTime();
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        float drawX = x;
        float drawY = y;
        if (!screenCoords) {
            drawX = camera.getXCoord(x);
            drawY = camera.getYCoord(y);
        }
        graphics.drawAnimation(anim, drawX, drawY);
    }
}

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
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

public class ExplosionEffect extends BasePositionable implements Effect {
    private static final long serialVersionUID = -6865607525774879448L;

    private transient Animation anim;

    private long lastCall;

    public ExplosionEffect(int x, int y, String animName) {
        super(x, y);
        anim = ResourceManager.getInstance().getAnimation(animName);
        anim.setLooping(false);
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
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawAnimation(anim, camera.getXCoord(x), camera.getYCoord(y));
    }
}

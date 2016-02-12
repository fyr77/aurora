package ru.game.aurora.effects;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.World;

/**
 * Created by User on 12.02.2016.
 * Shakes camera
 */
public class ScreenShakeEffect extends Effect {

    private static final long serialVersionUID = 1L;

    /**
     * State - number of waves. First wave move screen 32px to the right, second - 32 to the left,
     * third = 16 to the right and then back
     */
    private int state;

    private int shift;

    private long prevFrameTime;

    public ScreenShakeEffect() {
        super(0, 0, LOW_PRIORITY, DrawOrder.BACK);
        prevFrameTime = System.currentTimeMillis();
        state = 0;
        shift = 0;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void update(GameContainer container, World world) {
        long elapsed = System.currentTimeMillis() - prevFrameTime;
        while (elapsed > 0) {
            elapsed -= 5;
            if (state == 0) {
                shift += 3;
                if (shift >= 32) {
                    state = 1;
                }
            } else if (state == 1) {
                shift -= 3;
                if (shift <= -32) {
                    state = 2;
                }
            } else if (state == 2) {
                shift += 3;
                if (shift >= 16) {
                    state = 3;
                }
            } else if (state == 3) {
                shift -= 3;
                if (shift <= 0) {
                    state = 4;
                    shift = 0;
                }
            }
        }
        world.getCamera().setViewportX(shift);
        prevFrameTime = System.currentTimeMillis();
    }


    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        // nothing
    }

    @Override
    public boolean isOver() {
        return state == 4;
    }
}

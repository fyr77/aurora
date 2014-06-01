/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 15:50
 */
package ru.game.aurora.effects;

import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;

/**
 * Effects are short animations that are shown after some game events occur. E.g. blaster shot.
 * While effect is shown, all other game activity is paused
 */
public abstract class Effect extends BasePositionable implements GameObject, Comparable<Effect> {

    private static final long serialVersionUID = -4599033590718591085L;

    private int priority;

    private DrawOrder order;

    private String startSound;

    public static final int HIGHEST_PRIORITY = 10;

    public static final int HIGH_PRIORITY = 5;

    public static final int LOW_PRIORITY = 0;

    public static enum DrawOrder {
        BACK,
        FRONT
    }

    /**
     * Listener that is invoked when effect is completed
     */
    protected IStateChangeListener endListener = null;

    public Effect(int x, int y, int priority, DrawOrder order) {
        super(x, y);
        this.priority = priority;
        this.order = order;

    }

    public void setEndListener(IStateChangeListener endListener) {
        this.endListener = endListener;
    }

    public DrawOrder getOrder() {
        return order;
    }

    public abstract boolean isOver();

    public void onOver(World world) {
        if (endListener != null) {
            endListener.stateChanged(world);
        }
    }

    @Override
    public int compareTo(Effect o) {
        return o.priority - priority;
    }

    public String getStartSound() {
        return startSound;
    }

    public void setStartSound(String startSound) {
        this.startSound = startSound;
    }
}

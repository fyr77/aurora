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
public abstract class Effect extends BasePositionable implements GameObject
{

    private static final long serialVersionUID = -4599033590718591085L;

    /**
     * Listener that is invoked when effect is completed
     */
    protected IStateChangeListener endListener = null;

    public Effect(int x, int y) {
        super(x, y);
    }

    public void setEndListener(IStateChangeListener endListener) {
        this.endListener = endListener;
    }

    public abstract boolean isOver();

    public void onOver(World world)
    {
        if (endListener != null) {
            endListener.stateChanged(world);
        }
    }
}

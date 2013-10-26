/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.03.13
 * Time: 17:20
 */
package ru.game.aurora.player.earth;

import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

public abstract class EarthResearch extends GameEventListener {
    private static final long serialVersionUID = -5430246310704423995L;

    protected int targetTurn;

    protected final int length;

    protected boolean completed = false;

    protected String id;

    public EarthResearch(String id, int length) {
        this.id = id;
        this.length = length;
    }

    public void onStarted(World world) {
        targetTurn = world.getTurnCount() + length;
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (targetTurn <= world.getTurnCount()) {
            completed = true;
            onCompleted(world);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !completed;
    }

    protected abstract void onCompleted(World world);

    public String getId() {
        return id;
    }
}

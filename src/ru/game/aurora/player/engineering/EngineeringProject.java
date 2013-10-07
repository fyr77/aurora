/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 16.07.13
 * Time: 14:38
 */
package ru.game.aurora.player.engineering;


import ru.game.aurora.application.GameLogger;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.world.World;

public abstract class EngineeringProject extends ItemWithTextAndImage {
    private static final long serialVersionUID = -7083477407612327469L;

    protected int engineersAssigned;

    protected int remainingProgress;

    public EngineeringProject(String id, String icon) {
        super(id, icon);
    }

    public boolean update(World world) {
        remainingProgress -= engineersAssigned;
        if (remainingProgress <= 0) {
            GameLogger.getInstance().logMessage("Engineering project '" + getLocalizedName("engineering") + "' completed");
            onCompleted(world);
            return false;
        }
        return true;
    }

    public void changeEngineers(int amount) {
        engineersAssigned += amount;
    }

    public int getEngineersAssigned() {
        return engineersAssigned;
    }

    public abstract void onCompleted(World world);

    @Override
    public String toString() {
        return getLocalizedName("engineering");
    }
}

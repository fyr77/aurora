package ru.game.aurora.world.space.earth;

import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.05.14
 * Time: 18:09
 */
public class EarthUpgradeUnlocker extends GameEventListener {
    private static final long serialVersionUID = 3886247909156678459L;

    private int prevTechValue = 0;

    @Override
    public boolean onReturnToEarth(World world) {
        final int technologyLevel = world.getPlayer().getEarthState().getTechnologyLevel();

        if (technologyLevel > 50 && prevTechValue <= 50) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "letters.boy.sender", "letters.boy", "message"));
        }

        if (technologyLevel > 1500 && world.getGlobalVariables().containsKey("messages.scientist.received") && ((Integer) world.getGlobalVariables().get("messages.scientist.received") == 1)) {
            world.getGlobalVariables().put("messages.scientist.received", 2);
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "letters.scientist.sender", "letters.scientist_2", "message"));
        }

        prevTechValue = technologyLevel;
        return false;
    }
}

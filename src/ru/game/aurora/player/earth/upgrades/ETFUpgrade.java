package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by Егор on 29.09.2015.
 * Adds Earth Trade Fleet - civilian ships that can be encountered in friendly star systems.
 * They can help with resources.
 */
public class ETFUpgrade extends EarthUpgrade {

    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getGlobalVariables().put("earth.trade_fleet", 1);
        world.addListener(new ETFEncounterListener());
    }

    private static final class ETFEncounterListener extends GameEventListener {
        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            // check if it is either solar system, colony system or one of friendly alien homeworlds
            return false;
        }
    }
}

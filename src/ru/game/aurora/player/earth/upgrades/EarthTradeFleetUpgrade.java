package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by Егор on 28.09.2015.
 * Earth builds an interstellar trade fleet.
 * Player has a chance of meeting earth ships in alien star systems
 */
public class EarthTradeFleetUpgrade extends EarthUpgrade {
    @Override
    public void unlock(World world) {
        super.unlock(world);

        // add news
        world.getListeners().add(new ETFEncounterGenerator());
    }

    // player has a chance of encountering a ETF ship. Such ship can help with resources
    private static class ETFEncounterGenerator extends GameEventListener {

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            return false;
        }
    }
}

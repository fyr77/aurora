package ru.game.aurora.modding.sample;

import ru.game.aurora.modding.ModException;
import ru.game.aurora.modding.ModInterface;
import ru.game.aurora.world.World;

/**
 * A simple sample mod.
 * Adds a chance to find some debris in random star system, possibly containing some useful items
 */
public class SampleMod implements ModInterface {
    @Override
    public void onModLoaded() throws ModException {

    }

    @Override
    public void onNewGameStarted(World world) throws ModException {
        /**
         * Once a new game is started we want to add a new game event listener to it which will do all the job
         */
        world.addListener(new SampleModEventListener());
    }

    @Override
    public void onGameLoaded(World world) throws ModException {
        // do nothing, we modify only new worlds
    }
}

package ru.game.aurora.modding.sample;

import ru.game.aurora.modding.ModException;
import ru.game.aurora.modding.ModInterface;
import ru.game.aurora.world.GameEventListener;
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
        /**
         * This is probably a game that was created before this mod was installed.
         * Check that our listener already exists. Add it if necessary.
         */

        for (GameEventListener listener : world.getListeners()) {
            if (listener instanceof SampleModEventListener) {
                /*
                 * This world state was already modified by our mod before, do nothing
                 */
                return;
            }
        }

        /*
         * This world has no our listener, it was created before this mod was installed.
         * For this simple mod we will process such worlds and add our logic to them.
         * Complex mods may want to ignore such worlds and affect only new games.
         */
        world.addListener(new SampleModEventListener());
    }
}

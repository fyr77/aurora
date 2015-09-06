package ru.game.aurora.modding;

import ru.game.aurora.world.World;

/**
 * Base interface for all game modifications.
 * Contains main listener methods.
 * All mods that have custom logic should provide a class that implements this interface as their main entry point
 */
public interface ModInterface {
    /**
     * Called after this mod is successfully loaded by the mod manager.
     * By this call all mod classes and resources should be available
     */
    void onModLoaded() throws ModException;

    /**
     * Called after new world instance is created, after all original game content is added to the world, but before
     * player sees first game screen
     */
    void onNewGameStarted(World world) throws ModException;

    /**
     * Called after a saved game is loaded
     */
    void onGameLoaded(World world) throws ModException;
}

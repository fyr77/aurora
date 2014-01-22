package ru.game.aurora.world;

/**
 * Base interface for rooms that can be explored by landing party
 */
public interface IDungeon {

    DungeonController getController();

    ITileMap getMap();

    /**
     * If returns true, and landing party is lost, this leads to game over
     */
    boolean isCommanderInParty();
}

package ru.game.aurora.world;

import java.io.Serializable;

/**
 * Base interface for rooms that can be explored by landing party
 */
public interface IDungeon extends Serializable
{
    DungeonController getController();

    ITileMap getMap();

    /**
     * If returns true, and landing party is lost, this leads to game over
     */
    boolean isCommanderInParty();

    /**
     * @return true if this dungeon has its own playlist, so that default playlist must be restored when leaving it
     */
    boolean hasCustomMusic();
}

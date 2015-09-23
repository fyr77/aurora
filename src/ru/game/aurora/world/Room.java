/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:06
 */
package ru.game.aurora.world;

import java.io.Serializable;

public interface Room extends Updatable, Serializable, IDrawable {
    /**
     * Called when player enters this room from some other room. Should reset room state (e.g. put player on a starting position) and open gui.
     */
    void enter(World world);

    /**
     * Called when player returns to this room from internal room. Should not reset room state, only open gui; Also called when game is loaded
     */
    void returnTo(World world);

    ITileMap getMap();

    /**
     * Different rooms may have different duration of a turn. E.g. one turn in space takes one day while one turn in a dungeon
     * takes only a small fraction of a day. It is important for some time-related events like researches (which take days, not turns)
     * This method returns relation between turn duration and a day. If it returns 0 then actions in current room do not advance time at all.
     * If it returns 1 then each turn in this room is a day.
     */
    double getTurnToDayRelation();

}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 15:50
 */
package ru.game.aurora.effects;

import ru.game.aurora.world.GameObject;

/**
 * Effects are short animations that are shown after some game events occur. E.g. blaster shot.
 * While effect is shown, all other game activity is paused
 */
public interface Effect extends GameObject {
    public boolean isOver();
}

/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:06
 */
package ru.game.aurora.world;

import ru.game.aurora.player.Player;

public interface Room extends GameObject
{

    public void enter(Player player);

}

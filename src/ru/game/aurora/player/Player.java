/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.world.Ship;

public class Player
{
    private Ship ship;

    public Player() {
        ship = new Ship("Ship", true, 32, 32, 0, null);
    }

    public Ship getShip() {
        return ship;
    }
}

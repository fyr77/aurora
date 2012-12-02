/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.world.Ship;
import ru.game.aurora.world.planet.LandingParty;

public class Player
{
    private Ship ship;

    private LandingParty landingParty;

    public Player() {
        ship = new Ship("Ship", true, 32, 32, 0, null);
    }

    public Ship getShip() {
        return ship;
    }

    public LandingParty getLandingParty() {
        return landingParty;
    }

    public void setLandingParty(LandingParty landingParty) {
        this.landingParty = landingParty;
    }
}

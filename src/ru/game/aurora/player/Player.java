/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.world.Ship;
import ru.game.aurora.world.planet.LandingParty;

public class Player {
    private Ship ship;

    private LandingParty landingParty;

    public Player() {
        ship = new Ship(10, 10);
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

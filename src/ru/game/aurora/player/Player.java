/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.planet.LandingParty;

public class Player {
    private Ship ship;

    private LandingParty landingParty;

    private ResearchState researchState;

    private int resourceUnits = 0;

    public Player() {
        ship = new Ship(10, 10);
        researchState = new ResearchState(10);
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

    public ResearchState getResearchState() {
        return researchState;
    }

    public int getResourceUnits() {
        return resourceUnits;
    }

    public void setResourceUnits(int resourceUnits) {
        this.resourceUnits = resourceUnits;
    }

    public void addGlobalStatus()
    {
        GameLogger.getInstance().addStatusMessage("Ship status:");
        GameLogger.getInstance().addStatusMessage("Scientists: " + ship.getScientists());
        GameLogger.getInstance().addStatusMessage("Engineers: " + ship.getEngineers());
        GameLogger.getInstance().addStatusMessage("Military: " + ship.getMilitary());
        GameLogger.getInstance().addStatusMessage("Resources: " + resourceUnits);
    }
}

/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = -5632323774252846544L;

    private Ship ship;

    private LandingParty landingParty;

    private ResearchState researchState;

    private EngineeringState engineeringState;

    private int resourceUnits = 0;

    public Player() {
        ship = new Ship(10, 10);
        researchState = new ResearchState(ship.getScientists());
        engineeringState = new EngineeringState(ship.getEngineers());
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

    public void addGlobalStatus() {
        GameLogger.getInstance().addStatusMessage("Ship status:");
        GameLogger.getInstance().addStatusMessage("Hull: " + ship.getHull() + "/" + ship.getMaxHull());
        GameLogger.getInstance().addStatusMessage("  Scientists: " + ship.getScientists());
        GameLogger.getInstance().addStatusMessage("  Engineers: " + ship.getEngineers());
        GameLogger.getInstance().addStatusMessage("  Military: " + ship.getMilitary());
        GameLogger.getInstance().addStatusMessage("  Resources: " + resourceUnits);
        GameLogger.getInstance().addStatusMessage(String.format("Ship coordinates: [%d, %d]", ship.getX(), ship.getY()));

        GameLogger.getInstance().addStatusMessage("Weapons: ");
        int slot = 1;
        for (StarshipWeapon w : ship.getWeapons()) {
            GameLogger.getInstance().addStatusMessage(slot + ": " + w.getWeaponDesc().name + " " + (w.getReloadTimeLeft() == 0 ? " [ready]" : w.getReloadTimeLeft() + " to reload"));
            slot++;
        }
    }

    public EngineeringState getEngineeringState() {
        return engineeringState;
    }
}

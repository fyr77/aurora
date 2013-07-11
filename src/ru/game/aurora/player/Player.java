/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Player implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Ship ship;

    private LandingParty landingParty;

    private ResearchState researchState;

    private EngineeringState engineeringState;

    private EarthState earthState;

    private int resourceUnits = 5;

    private int credits = 5;

    private Map<InventoryItem, Integer> inventory = new HashMap<InventoryItem, Integer>();

    /**
     * Number of times player has returned to Earth without enough research data
     * When this count reaches some limit, game is over
     */
    private int failsCount = 0;

    public Player() {
        ship = new Ship(10, 10);
        researchState = new ResearchState(ship.getScientists());
        engineeringState = new EngineeringState(ship.getEngineers());
        earthState = new EarthState();
        final LandingPartyWeapon defaultWeapon = ResourceManager.getInstance().getLandingPartyWeapons().getEntity("assault");
        inventory.put(defaultWeapon, 1);
        landingParty = new LandingParty(0, 0, defaultWeapon, 1, 1, 1);
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

    public EarthState getEarthState() {
        return earthState;
    }

    public EngineeringState getEngineeringState() {
        return engineeringState;
    }

    public void changeCredits(int delta)
    {
        credits += delta;
    }

    public int getCredits()
    {
        return credits;
    }

    public void increaseFailCount()
    {
        failsCount++;
    }

    public int getFailCount()
    {
        return failsCount;
    }

    public Map<InventoryItem, Integer> getInventory() {
        return inventory;
    }
}

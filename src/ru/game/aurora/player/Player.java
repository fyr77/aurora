/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.player;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.engineering.projects.CylindersCraft;
import ru.game.aurora.player.engineering.projects.MedpacksCraft;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.planet.Cylinders;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.MedPack;
import ru.game.aurora.world.quest.Journal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Player implements Serializable {

    private static final long serialVersionUID = 3L;

    private Ship ship;

    private LandingParty landingParty;

    private ResearchState researchState;

    private EngineeringState engineeringState;

    private final EarthState earthState;

    private int resourceUnits = 5;

    private int credits = 0;

    private final Journal journal = new Journal();

    private final Map<InventoryItem, Integer> inventory = new HashMap<>();

    // main country in Aurora project, defines some bonuses
    private EarthCountry mainCountry = EarthCountry.AMERICA;

    /**
     * Number of times player has returned to Earth without enough research data
     * When this count reaches some limit, game is over
     */
    private int failsCount = 0;

    public Player() {
        earthState = new EarthState();
        final WeaponDesc defaultWeapon = ResourceManager.getInstance().getWeapons().getEntity("assault");
        inventory.put(defaultWeapon, 1);
        landingParty = new LandingParty(0, 0, defaultWeapon, 2, 2, 4, Configuration.getIntProperty("player.landing_party.defaultHP"));
        landingParty.pickUp(new MedPack(), 3);   //Santa's gifts
        landingParty.pickUp(new Cylinders(), 3);
    }

    public void setShip(World world, AlienRace humanity) {
        researchState = new ResearchState(Ship.BASE_SCIENTISTS);
        engineeringState = new EngineeringState(Ship.BASE_ENGINEERS);
        ship = new Ship(world, humanity.getHomeworld().getX() + 1, humanity.getHomeworld().getY());
        ship.installInitialUpgrades(world);

        engineeringState.addNewEngineeringProject(new MedpacksCraft(3));  //добавим проекты для крафта аптечек и баллонов
        engineeringState.addNewEngineeringProject(new CylindersCraft(3));
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

    public void changeCredits(World world, int delta) {
        credits += delta;
        world.getGlobalVariables().put("credits", credits);
    }

    public int getCredits() {
        return credits;
    }

    public void increaseFailCount() {
        failsCount++;
    }

    public int getFailCount() {
        return failsCount;
    }

    public Map<InventoryItem, Integer> getInventory() {
        return inventory;
    }

    public Journal getJournal() {
        return journal;
    }

    public EarthCountry getMainCountry() {
        return mainCountry;
    }

    public void setMainCountry(EarthCountry mainCountry) {
        this.mainCountry = mainCountry;
    }
}

/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:38
 */
package ru.game.aurora.world.planet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.SurfaceGUIController;
import ru.game.aurora.player.EarthCountry;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.player.research.projects.Cartography;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponDesc;

import java.util.Iterator;

public class LandingParty extends BaseGameObject {
    public static final int MAX_OXYGEN = 100;

    private static final long serialVersionUID = 2;
    private static final SellOnlyInventoryItem geodataKey = new GeodataInventoryItem();
    private final int MAX_HP;
    private int military;
    private int science;
    private int engineers;
    private int oxygen;
    private WeaponDesc weapon;
    private int collectedGeodata = 0;
    private Multiset<InventoryItem> inventory = HashMultiset.create();
    private int hp;

    public LandingParty(int maxHp) {
        super(0, 0, "awayteam");
        this.MAX_HP = maxHp;
        this.hp = maxHp;
    }

    public LandingParty(int x, int y, WeaponDesc weapon, int military, int science, int engineers, int maxHp) {
        super(x, y, "awayteam");
        this.military = military;
        this.science = science;
        this.engineers = engineers;
        this.weapon = weapon;
        this.MAX_HP = maxHp;
        this.hp = maxHp;
        oxygen = 100;
    }

    public LandingParty(LandingParty other) {
        this(other.x, other.y, other.weapon, other.military, other.science, other.engineers, other.MAX_HP);
        this.oxygen = other.oxygen;
        this.inventory = HashMultiset.create();
        for (InventoryItem i : other.inventory) {
            this.inventory.add(i);
        }
    }

    public int getMaxWeight() {
        return engineers * 2 + science;
    }

    public int getInventoryWeight() {
        int totalWeight = 0;
        for (InventoryItem i : inventory) {
            totalWeight += i.getWeight();
        }
        return totalWeight;
    }

    public boolean overWeightTest() {
        boolean overWeight;
        overWeight = getMaxWeight() < getInventoryWeight();
        if (overWeight) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.overweight"));
        }
        setMoveability(!overWeight);
        return overWeight;
    }

    public void consumeOxygen() {
        oxygen--;
        if (oxygen == 50) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.oxygen.half_empty"));
        } else if (oxygen == 20) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.oxygen.almost_empty"));
        }
    }

    public void refillOxygen() {
        oxygen = MAX_OXYGEN;
    }

    public int getOxygen() {
        return oxygen;
    }

    public WeaponDesc getWeapon() {
        return weapon;
    }

    public void setWeapon(WeaponDesc weapon) {
        this.weapon = weapon;
    }

    public int calcDamage(World world) {
        double baseValue = (weapon.getDamage() * (1.0 / 3 * (science + engineers) + military));
        if (world.getPlayer().getMainCountry() == EarthCountry.AMERICA) {
            baseValue *= Configuration.getDoubleProperty("player.america.damageMultiplier");
        }
        return (int) Math.round(baseValue);
    }

    public int calcMiningPower() {
        return Math.max(1, (int) (1.0 / 3 * (science + military) + engineers));
    }

    public int calcResearchPower() {
        return Math.max(1, (int) ((2 * engineers + military) / 3.0 + science));
    }

    public void addCollectedGeodata(int amount) {
        collectedGeodata += amount;
    }

    public int getCollectedGeodata() {
        return collectedGeodata;
    }

    public void setCollectedGeodata(int collectedGeodata) {
        this.collectedGeodata = collectedGeodata;
    }

    public void pickUp(InventoryItem o, int amount) {
        Boolean itemAlreadyInInventory = false;
        for (Multiset.Entry<InventoryItem> entry : inventory.entrySet()) {
            if (entry.getElement().getName().equals(o.getName())) {
                inventory.setCount(entry.getElement(), entry.getCount() + amount);
                itemAlreadyInInventory = true;
                break;
            }
        }
        if (!itemAlreadyInInventory) {
            inventory.add(o, amount);
        }
    }

    /**
     * Returns false if since last party configuration smth has changed and new 'Landing party screen' must be shown
     */
    public boolean canBeLaunched(World world) {
        Ship ship = world.getPlayer().getShip();
        return military <= ship.getMilitary()
                && science <= world.getPlayer().getResearchState().getIdleScientists()
                && engineers <= world.getPlayer().getEngineeringState().getIdleEngineers()
                && getTotalMembers() > 0
                && getInventoryWeight() <= getMaxWeight();
    }

    public void onLaunch(World world) {
        Ship ship = world.getPlayer().getShip();
        ship.setMilitary(ship.getMilitary() - military);
        ship.setScientists(ship.getScientists() - science);
        ship.setEngineers(ship.getEngineers() - engineers);

        world.getPlayer().getEngineeringState().removeEngineers(engineers);
        world.getPlayer().getResearchState().removeScientists(science);
    }

    public void onReturnToShip(World world) {
        if (collectedGeodata > 0) {
            final int collectedGeodata1 = (int) Math.round(Configuration.getDoubleProperty("research.geodata.multiplier") * getCollectedGeodata());
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.collect_geodata"), collectedGeodata1));
            final ResearchState researchState = world.getPlayer().getResearchState();
            if (researchState.getGeodata().getRaw() == 0) {
                researchState.addNewAvailableProject(new Cartography(researchState.getGeodata()));
            }
            researchState.getGeodata().addRawData(collectedGeodata1);
            world.getPlayer().changeResource(world, geodataKey, collectedGeodata1);
            setCollectedGeodata(0);
        }

        for (Iterator<Multiset.Entry<InventoryItem>> iter = inventory.entrySet().iterator(); iter.hasNext(); ) {
            Multiset.Entry<InventoryItem> o = iter.next();
            if (o.getElement().isDumpable()) {
                o.getElement().onReceived(world, o.getCount());
                iter.remove();
            }
        }

        Ship ship = world.getPlayer().getShip();
        ship.setMilitary(ship.getMilitary() + military);
        ship.setScientists(ship.getScientists() + science);
        ship.setEngineers(ship.getEngineers() + engineers);

        world.getPlayer().getEngineeringState().addIdleEngineers(engineers);
        world.getPlayer().getResearchState().addIdleScientists(science);

        resetHp(world);
        refillOxygen();
    }

    public int getTotalMembers() {
        return military + science + engineers;
    }

    public int getMilitary() {
        return military;
    }

    public void setMilitary(int military) {
        this.military = military;
    }

    public int getScience() {
        return science;
    }

    public void setScience(int science) {
        this.science = science;
    }

    public int getEngineers() {
        return engineers;
    }

    public void setEngineers(int engineers) {
        this.engineers = engineers;
    }

    public Multiset<InventoryItem> getInventory() {
        return inventory;
    }

    public int getMaxHP(World world) {
        int rz = MAX_HP;
        if (world.getPlayer().getMainCountry() == EarthCountry.AMERICA) {
            rz += Configuration.getIntProperty("player.america.hpBonus");
        }
        return rz;
    }

    public void resetHp(World world) {
        hp = getMaxHP(world);
        ((SurfaceGUIController) GUI.getInstance().getNifty().findScreenController(SurfaceGUIController.class.getCanonicalName())).updateStats();
    }

    public void addHP(World world, int amount) {
        hp = Math.min(getMaxHP(world), hp + amount);
        ((SurfaceGUIController) GUI.getInstance().getNifty().findScreenController(SurfaceGUIController.class.getCanonicalName())).updateStats();
    }

    public void subtractHp(World world, int amount) {
        boolean depressurization;
        while (amount > 0) {
            depressurization = false;
            int amountToSubtract = Math.min(hp, amount);
            if (amountToSubtract < 0) {
                break;
            }
            hp -= amountToSubtract;
            if ((hp != 0) && (world.getCurrentRoom() instanceof Planet)) {
                PlanetAtmosphere atmosphere = ((Planet) world.getCurrentRoom()).getAtmosphere();
                if ((atmosphere == PlanetAtmosphere.AGGRESSIVE_ATMOSPHERE) || (atmosphere == PlanetAtmosphere.NO_ATMOSPHERE)) {
                    double rnd = Math.random();
                    if (rnd <= Configuration.getDoubleProperty("world.planet.dyingChanceBecauseOfAtmosphere")) {
                        hp = 0;
                        depressurization = true;
                        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.party_member_died_due_to_depressurization"));
                    }
                }
            }
            if (hp == 0) {
                // landing party member killed
                if (!depressurization) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.party_member_killed"));
                }
                if (military > 0) {
                    military--;
                } else if (engineers > 0) {
                    engineers--;
                } else {
                    science--;
                }
                world.onCrewChanged();
                if (getTotalMembers() > 0) {
                    resetHp(world);
                } else {
                    break;
                }
                overWeightTest();   //overload test in case of loosing a crew member
            }
            amount -= amountToSubtract;
        }
        ((SurfaceGUIController) GUI.getInstance().getNifty().findScreenController(SurfaceGUIController.class.getCanonicalName())).updateStats();
    }

    public int getHp() {
        return hp;
    }

    @Override
    public void moveUp() {
        overWeightTest();
        super.moveUp();
    }

    @Override
    public void moveDown() {
        overWeightTest();
        super.moveDown();
    }

    @Override
    public void moveRight() {
        overWeightTest();
        super.moveRight();
    }

    @Override
    public void moveLeft() {
        overWeightTest();
        super.moveLeft();
    }
}

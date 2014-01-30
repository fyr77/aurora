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
import ru.game.aurora.player.EarthCountry;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.player.research.projects.Cartography;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.MovableSprite;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;

import java.util.Iterator;

public class LandingParty extends MovableSprite implements GameObject {
    public static final int MAX_OXYGEN = 100;

    private static final long serialVersionUID = 2;

    private int military;

    private int science;

    private int engineers;

    private int oxygen;

    private LandingPartyWeapon weapon;

    private int collectedGeodata = 0;

    private Multiset<InventoryItem> inventory = HashMultiset.<InventoryItem>create();

    private final int MAX_HP;

    private int hp;

    public LandingParty(int maxHp) {
        super(0, 0, "awayteam");
        this.MAX_HP = maxHp;
        this.hp = maxHp;
    }

    public LandingParty(int x, int y, LandingPartyWeapon weapon, int military, int science, int engineers, int maxHp) {
        super(x, y, "awayteam");
        this.military = military;
        this.science = science;
        this.engineers = engineers;
        this.weapon = weapon;
        this.MAX_HP = maxHp;
        this.hp = maxHp;
        oxygen = 100;
        pickUp(new MedPack(), 3);   //Santa's gifts
        pickUp(new Cylinders(), 3);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getCapacity() {
        return engineers * 2 + science; //Инженеры - крепкие ребята, учёные - не очень, солдаты таскают своё оружие
    }

    public int getInventoryWeight() {
        int totalWeight = 0;
        for (InventoryItem i:inventory) {
            totalWeight += i.getWeight();
        }
        return totalWeight;
    }

    public void overWeightTest(){
        boolean overWeight;
        overWeight = getCapacity() < getInventoryWeight();
        if (overWeight) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.overweight"));
        }
        setMoveability(!overWeight);
    }

    public void consumeOxygen() {
        //todo: depend on team size?
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

    public LandingPartyWeapon getWeapon() {
        return weapon;
    }

    public int calcDamage(World world) {
        double baseValue =  (weapon.getDamage() * (1.0 / 3 * (science + engineers) + military));
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
                inventory.setCount(entry.getElement(),entry.getCount() + amount);
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
        return military <= ship.getMilitary() && science <= ship.getScientists() && engineers <= ship.getEngineers() && getTotalMembers() > 0 && getInventoryWeight() <= getCapacity();
    }

    public void onLaunch(World world) {
        Ship ship = world.getPlayer().getShip();
        ship.setMilitary(ship.getMilitary() - military);
        ship.setScientists(ship.getScientists() - science);
        ship.setEngineers(ship.getEngineers() - engineers);
    }

    public void onReturnToShip(World world) {
        if (collectedGeodata > 0) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.collect_geodata"), getCollectedGeodata()));
            final ResearchState researchState = world.getPlayer().getResearchState();
            if (researchState.getGeodata().getRaw() == 0) {
                researchState.addNewAvailableProject(new Cartography(researchState.getGeodata()));
            }
            researchState.getGeodata().addRawData(getCollectedGeodata());
            setCollectedGeodata(0);
        }

        for (Iterator<Multiset.Entry<InventoryItem>> iter = inventory.entrySet().iterator(); iter.hasNext();) {
            Multiset.Entry<InventoryItem> o = iter.next();
            o.getElement().onReturnToShip(world, o.getCount());
            if (o.getElement().isDumpable()) {
                iter.remove();
            }
        }

        Ship ship = world.getPlayer().getShip();
        ship.setMilitary(ship.getMilitary() + military);
        ship.setScientists(ship.getScientists() + science);
        ship.setEngineers(ship.getEngineers() + engineers);
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

    public void resetHp(World world) {
        hp = MAX_HP;
        if (world.getPlayer().getMainCountry() == EarthCountry.AMERICA) {
            hp += Configuration.getIntProperty("player.america.hpBonus");
        }
    }

    public void subtractHp(World world, int amount) {
        while (amount > 0) {
            int amountToSubtract = Math.min(hp, amount);
            if (amountToSubtract < 0) {
                break;
            }
            hp -= amountToSubtract;
            if (hp == 0) {
                // landing party member killed
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.party_member_killed"));
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
                overWeightTest();   //возможен перегруз в результате потери члена команды
            }
            amount -= amountToSubtract;
        }
    }

    public int getHp() {
        return hp;
    }

    public void setWeapon(LandingPartyWeapon weapon) {
        this.weapon = weapon;
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

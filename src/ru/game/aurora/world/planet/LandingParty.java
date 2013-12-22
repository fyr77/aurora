/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:38
 */
package ru.game.aurora.world.planet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
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

    private static final long serialVersionUID = 1;

    private int military;

    private int science;

    private int engineers;

    private int oxygen;

    private LandingPartyWeapon weapon;

    private int collectedGeodata = 0;

    private Multiset<InventoryItem> inventory = HashMultiset.<InventoryItem>create();

    private int hp = 3;

    public LandingParty() {
        super(0, 0, "awayteam");
    }

    public LandingParty(int x, int y, LandingPartyWeapon weapon, int military, int science, int engineers) {
        super(x, y, "awayteam");
        this.military = military;
        this.science = science;
        this.engineers = engineers;
        this.weapon = weapon;
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

    public int calcDamage() {
        return Math.max(1, (int) (weapon.getDamage() * (1.0 / 3 * (science + engineers) + military)));
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
        inventory.add(o, amount);
    }

    /**
     * Returns false if since last party configuration smth has changed and new 'Landing party screen' must be shown
     */
    public boolean canBeLaunched(World world) {
        Ship ship = world.getPlayer().getShip();
        return military <= ship.getMilitary() && science <= ship.getScientists() && engineers <= ship.getEngineers() && getTotalMembers() > 0;
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

    public void resetHp() {
        hp = 3;
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
                    resetHp();
                } else {
                    break;
                }
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
}

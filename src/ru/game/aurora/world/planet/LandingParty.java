/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:38
 */
package ru.game.aurora.world.planet;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.player.research.projects.Cartography;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;

import java.util.HashMap;
import java.util.Map;

public class LandingParty implements GameObject, Positionable {
    private int x;

    private int y;

    private int military;

    private int science;

    private int engineers;

    private int oxygen;

    private LandingPartyWeapon weapon;

    private int collectedGeodata = 0;

    private Map<InventoryItem, Integer> inventory = new HashMap<InventoryItem, Integer>();

    private int hp = 3;

    public LandingParty(int x, int y, LandingPartyWeapon weapon, int military, int science, int engineers) {
        this.x = x;
        this.y = y;
        this.military = military;
        this.science = science;
        this.engineers = engineers;
        this.weapon = weapon;
        oxygen = 100;
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

    @Override
    public void update(JGEngine engine, World world) {

    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(camera.getXCoord(x), camera.getYCoord(y), "awayteam");

        GameLogger.getInstance().addStatusMessage("Landing team status:");
        GameLogger.getInstance().addStatusMessage("scientists: " + science);
        GameLogger.getInstance().addStatusMessage("engineers: " + engineers);
        GameLogger.getInstance().addStatusMessage("military: " + military);
        GameLogger.getInstance().addStatusMessage("Remaining oxygen: " + oxygen);
        GameLogger.getInstance().addStatusMessage(String.format("Current coordinates (%d, %d)", x, y));
        GameLogger.getInstance().addStatusMessage(String.format("Weapons: %s, %d rng, %d dmg", weapon.getName(), weapon.getRange(), weapon.getDamage()));
        GameLogger.getInstance().addStatusMessage("============= Inventory: ================");
        for (Map.Entry<InventoryItem, Integer> po : inventory.entrySet()) {
            GameLogger.getInstance().addStatusMessage(po.getKey().getName() + ": " + po.getValue());
        }
        GameLogger.getInstance().addStatusMessage("=========================================");
    }

    public void consumeOxygen() {
        //todo: depend on team size?
        oxygen--;
        if (oxygen == 50) {
            GameLogger.getInstance().logMessage("Oxygen tank is half empty");
        } else if (oxygen == 20) {
            GameLogger.getInstance().logMessage("Warning, low on oxygen");
        }
    }

    public void refillOxygen() {
        oxygen = 100;
    }

    public int getOxygen() {
        return oxygen;
    }

    public LandingPartyWeapon getWeapon() {
        return weapon;
    }

    public int calcDamage() {
        return (int) (weapon.getDamage() * (1.0 / 3 * (science + engineers) + military));
    }

    public int calcMiningPower() {
        return (int) (1.0 / 3 * (science + military) + engineers);
    }

    public int calcResearchPower() {
        return (int) ((2 * engineers + military) / 3.0 + science);
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

    public void pickUp(World world, InventoryItem o) {
        Integer i = inventory.get(o);
        if (i == null) {
            i = 0;
        }
        inventory.put(o, i + 1);
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
            GameLogger.getInstance().logMessage("Adding " + getCollectedGeodata() + " pieces of raw geodata");
            final ResearchState researchState = world.getPlayer().getResearchState();
            if (researchState.getGeodata().getRaw() == 0) {
                GameLogger.getInstance().logMessage("Cartography research is now available");
                researchState.getAvailableProjects().add(new Cartography(researchState.getGeodata()));
            }
            researchState.getGeodata().addRawData(getCollectedGeodata());
            setCollectedGeodata(0);
        }

        for (Map.Entry<InventoryItem, Integer> o : inventory.entrySet()) {
            o.getKey().onReturnToShip(world, o.getValue());
        }

        Ship ship = world.getPlayer().getShip();
        ship.setMilitary(ship.getMilitary() + military);
        ship.setMilitary(ship.getScientists() + science);
        ship.setMilitary(ship.getEngineers() + engineers);
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

    public Map<InventoryItem, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<InventoryItem, Integer> inventory) {
        this.inventory = inventory;
    }

    public void resetHp() {
        hp = 3;
    }

    public void subtractHp(int amount) {
        while (amount > 0) {
            int amountToSubtract = Math.min(hp, amount);
            hp -= amountToSubtract;
            if (hp == 0) {
                // landing party member killed
                GameLogger.getInstance().logMessage("Party member killed");
                if (military > 0) {
                    military--;
                } else if (engineers > 0) {
                    engineers--;
                } else {
                    science--;
                }
                if (getTotalMembers() > 0) {
                    resetHp();
                }
            }
            amount -= amountToSubtract;
        }
    }

    public int getHp() {
        return hp;
    }
}

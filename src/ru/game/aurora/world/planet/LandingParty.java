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
import ru.game.aurora.player.research.projects.AnimalResearch;
import ru.game.aurora.player.research.projects.Cartography;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

import java.util.LinkedList;
import java.util.List;

public class LandingParty implements GameObject, Positionable {
    private int x;

    private int y;

    private int military;

    private int science;

    private int engineers;

    private int oxygen;

    private LandingPartyWeapon weapon;

    private int collectedGeodata = 0;

    private List<PlanetObject> inventory = new LinkedList<PlanetObject>();

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
        for (PlanetObject po : inventory) {
            GameLogger.getInstance().addStatusMessage(po.getName());
        }
        GameLogger.getInstance().addStatusMessage("=========================================");
    }

    public void consumeOxygen() {
        //todo: depend on team size?
        oxygen--;
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

    public void addCollectedGeodata(int amount) {
        collectedGeodata += amount;
    }

    public int getCollectedGeodata() {
        return collectedGeodata;
    }

    public void setCollectedGeodata(int collectedGeodata) {
        this.collectedGeodata = collectedGeodata;
    }

    public void pickUp(World world, PlanetObject o)
    {
        inventory.add(o);
        o.onPickedUp(world);
    }

    public void onReturnToShip(World world)
    {
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

        for (PlanetObject o : inventory) {
            if (o instanceof Animal) {
                AnimalSpeciesDesc desc = ((Animal) o).getDesc();

                if (!desc.isOutopsyMade() && !world.getPlayer().getResearchState().containsResearchFor(desc)) {
                    // this type of alien animal has never been seen before, add new research
                    GameLogger.getInstance().logMessage("Added biology research for new alien animal species " + desc.getName());
                    world.getPlayer().getResearchState().getAvailableProjects().add(new AnimalResearch(desc));
                }
            }
        }
    }
}

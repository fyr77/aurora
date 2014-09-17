/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.engineering.upgrades.BarracksUpgrade;
import ru.game.aurora.player.engineering.upgrades.LabUpgrade;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.player.engineering.upgrades.WorkshopUpgrade;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.space.StarSystem;

import java.util.*;

public class Ship extends BaseGameObject {

    public static final int BASE_SCIENTISTS = 5;
    public static final int BASE_ENGINEERS = 5;
    public static final int BASE_MILITARY = 5;

    private static final long serialVersionUID = 3;

    private int hull;

    private final int maxHull;

    private int scientists;

    private int engineers;

    private int military;

    private int maxMilitary;

    private int maxScientists;

    private int maxEngineers;

    private final List<WeaponInstance> weapons = new ArrayList<>();

    private final Multiset<InventoryItem> storage = HashMultiset.create();

    private final List<ShipUpgrade> upgrades = new ArrayList<>();

    private Map<String, CrewMember> crewMembers = new HashMap<>();

    private int freeSpace;

    public Ship(World world, int x, int y) {
        super(x, y, new Drawable("aurora"));
        setFaction(world.getFactions().get(HumanityGenerator.NAME));
        name = "Aurora-2";
        hull = maxHull = 10;
        maxScientists = scientists = BASE_SCIENTISTS;
        maxMilitary = military = BASE_MILITARY;
        maxEngineers = engineers = BASE_ENGINEERS;

        freeSpace = Configuration.getIntProperty("upgrades.ship_free_space");

    }

    public void addCrewMember(World world, CrewMember member) {
        crewMembers.put(member.getId(), member);
        member.onAdded(world);
    }

    public void removeCrewMember(World world, String id) {
        CrewMember cm = crewMembers.remove(id);
        if (cm != null) {
            cm.onRemoved(world);
        }
    }

    public Map<String, CrewMember> getCrewMembers() {
        return crewMembers;
    }

    public void installInitialUpgrades(World world) {
        addUpgrade(world, new LabUpgrade());
        addUpgrade(world, new BarracksUpgrade());
        addUpgrade(world, new WorkshopUpgrade());
        addUpgrade(world, new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon")));

        addCrewMember(world, new CrewMember("henry", "marine_dialog", Dialog.loadFromFile("dialogs/tutorials/marine_intro.json")));
        addCrewMember(world, new CrewMember("gordon", "scientist_dialog", Dialog.loadFromFile("dialogs/tutorials/scientist_intro.json")));
        addCrewMember(world, new CrewMember("sarah", "engineer_dialog", Dialog.loadFromFile("dialogs/tutorials/engineer_intro.json")));

        refillCrew(world);
    }

    public void addUpgrade(World world, ShipUpgrade upgrade) {
        if (freeSpace < upgrade.getSpace()) {
            throw new IllegalArgumentException("Upgrade can not be installed because thiere is not enough space");
        }
        freeSpace -= upgrade.getSpace();
        upgrade.onInstalled(world, this);
        upgrades.add(upgrade);
    }

    public void removeUpgrade(World world, ShipUpgrade upgrade) {
        for (Iterator<ShipUpgrade> iterator = upgrades.iterator(); iterator.hasNext(); ) {
            ShipUpgrade u = iterator.next();
            if (u.equals(upgrade)) {
                iterator.remove();
                u.onRemoved(world, this);
                freeSpace += u.getSpace();
                break;
            }
        }
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (world.isUpdatedThisFrame()) {
            for (WeaponInstance weapon : weapons) {
                weapon.reload();
            }
        }
    }

    public void setHull(int hull) {
        this.hull = hull;
    }

    public int getHull() {
        return hull;
    }

    public int getMaxHull() {
        return maxHull;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        if (hull > 0) {
            super.draw(container, g, camera, world);
        }
    }

    public int getScientists() {
        return scientists;
    }

    public void setScientists(int scientists) {
        this.scientists = scientists;
    }

    public int getEngineers() {
        return engineers;
    }

    public void setEngineers(int engineers) {
        this.engineers = engineers;
    }

    public int getMilitary() {
        return military;
    }

    public void setMilitary(int military) {
        this.military = military;
    }

    public int getTotalCrew() {
        return scientists + engineers + military;
    }

    public List<WeaponInstance> getWeapons() {
        return weapons;
    }


    @Override
    public void onAttack(World world, GameObject attacker, int dmg) {
        if (Configuration.getBooleanProperty("cheat.invulnerability")) {
            return;
        }
        hull -= dmg;
        world.onPlayerShipDamaged();
        if (hull <= 0) {
            ExplosionEffect ship_explosion = new ExplosionEffect(x, y, "ship_explosion", false, true);
            ship_explosion.getAnim().setSpeed(0.5f);
            ((StarSystem) world.getCurrentRoom()).addEffect(ship_explosion);
            ship_explosion.setEndListener(new GameOverEffectListener());

        }
    }

    @Override
    public boolean isAlive() {
        return hull > 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canBeAttacked() {
        return true;
    }

    public int getLostCrewMembers() {
        return maxEngineers + maxMilitary + maxScientists - getTotalCrew();
    }

    public void refillCrew(World world) {

        scientists = maxScientists;
        engineers = maxEngineers;
        military = maxMilitary;

        world.onCrewChanged();
    }

    public Multiset<InventoryItem> getStorage() {
        return storage;
    }

    public void addItem(InventoryItem o, int amount) {
        Boolean itemAlreadyInStorage = false;
        for (Multiset.Entry<InventoryItem> entry : storage.entrySet()) {
            if (entry.getElement().getName().equals(o.getName())) {
                storage.setCount(entry.getElement(), entry.getCount() + amount);
                itemAlreadyInStorage = true;
                break;
            }
        }
        if (!itemAlreadyInStorage) {
            storage.add(o, amount);
        }
    }

    public void fullRepair(World world) {
        hull = maxHull;
        world.getPlayer().getEngineeringState().getHullRepairs().cancel(world);

    }

    public List<ShipUpgrade> getUpgrades() {
        return upgrades;
    }

    public int getMaxMilitary() {
        return maxMilitary;
    }

    public void setMaxMilitary(int maxMilitary) {
        this.maxMilitary = maxMilitary;
    }

    public int getMaxScientists() {
        return maxScientists;
    }

    public void setMaxScientists(int maxScientists) {
        this.maxScientists = maxScientists;
    }

    public int getMaxEngineers() {
        return maxEngineers;
    }

    public void setMaxEngineers(int maxEngineers) {
        this.maxEngineers = maxEngineers;
    }

    public int getFreeSpace() {
        return freeSpace;
    }

}

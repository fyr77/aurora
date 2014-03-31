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
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.List;

public class Ship extends MovableSprite implements SpaceObject {

    public static final int MAX_SCIENTISTS = 20;
    public static final int MAX_ENGINEERS = 10;
    public static final int MAX_MILITARY = 15;

    private static final long serialVersionUID = 1;

    private int hull;

    private final String name;

    private int maxHull;

    private int scientists = MAX_SCIENTISTS;

    private int engineers = MAX_ENGINEERS;

    private int military = MAX_MILITARY;

    private List<StarshipWeapon> weapons = new ArrayList<StarshipWeapon>();

    private AlienRace humanity;

    private Multiset<InventoryItem> storage = HashMultiset.<InventoryItem>create();

    public Ship(AlienRace humanity, int x, int y) {
        super(x, y, "aurora");
        this.humanity = humanity;
        name = "Hawking";
        hull = maxHull = 10;
        weapons.add(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"), StarshipWeapon.MOUNT_ALL));
    }


    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (world.isUpdatedThisFrame()) {
            for (StarshipWeapon weapon : weapons) {
                if (weapon.getReloadTimeLeft() > 0) {
                    weapon.setReloadTimeLeft(weapon.getReloadTimeLeft() - 1);
                }
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
    public void draw(GameContainer container, Graphics g, Camera camera) {
        if (hull > 0) {
            super.draw(container, g, camera);
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

    public List<StarshipWeapon> getWeapons() {
        return weapons;
    }

    @Override
    public void onContact(World world) {
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        if (Configuration.getBooleanProperty("cheat.invulnerability")) {
            return;
        }
        hull -= dmg;
        world.onPlayerShipDamaged();
        if (hull <= 0) {
            ExplosionEffect ship_explosion = new ExplosionEffect(x, y, "ship_explosion", false, true);
            ship_explosion.getAnim().setSpeed(0.5f);
            ((StarSystem) world.getCurrentRoom()).addEffect(ship_explosion);
            ship_explosion.setEndListener(new IStateChangeListener() {

                private static final long serialVersionUID = -5155503207553019512L;

                @Override
                public void stateChanged(World world) {
                    GUI.getInstance().getNifty().gotoScreen("fail_screen");
                    FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
                    controller.set("ship_destroyed_gameover", "ship_destroyed");
                }
            });

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
    public String getScanDescription(World world) {
        return null;
    }

    @Override
    public AlienRace getRace() {
        return humanity;
    }

    @Override
    public boolean canBeShotAt() {
        return true;
    }

    public int getLostCrewMembers() {
        return Ship.MAX_ENGINEERS + Ship.MAX_MILITARY + Ship.MAX_SCIENTISTS - getTotalCrew();
    }

    public void refillCrew(World world) {
        scientists = Ship.MAX_SCIENTISTS;
        engineers = Ship.MAX_ENGINEERS;
        military = Ship.MAX_MILITARY;
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
}

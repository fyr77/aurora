/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.FailScreen;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.equip.StarshipWeaponDesc;
import ru.game.aurora.world.space.SpaceObject;

import java.util.ArrayList;
import java.util.List;

public class Ship extends BasePositionable implements SpaceObject {

    private static final long serialVersionUID = 4691881150467099814L;

    private int hull;

    private int maxHull;

    private int scientists = 10;

    private int engineers = 5;

    private int military = 5;

    private List<StarshipWeapon> weapons = new ArrayList<StarshipWeapon>();

    public Ship(int x, int y) {
        super(x, y);
        hull = maxHull = 3;
        weapons.add(new StarshipWeapon(new StarshipWeaponDesc(1, "Laser cannons", "Simple middle-range laser cannons", "blaster_shot", 5, 3), StarshipWeapon.MOUNT_ALL));
    }


    @Override
    public void update(GameContainer container, World world) {
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
        g.setColor(Color.white);
        g.drawImage(ResourceManager.getInstance().getImage("aurora"), camera.getXCoord(x), camera.getYCoord(y), null);
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
    public void setPos(int newX, int newY) {
        this.x = newX;
        this.y = newY;
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
        hull -= dmg;
        if (hull <= 0) {
            world.setCurrentRoom(FailScreen.createShipDestroyedFailScreen());
        }
    }

    @Override
    public boolean isAlive() {
        return hull > 0;
    }

    @Override
    public String getName() {
        return "Enterprise"; //todo: player set name
    }
}

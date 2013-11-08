/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.space.SpaceObject;

import java.util.ArrayList;
import java.util.List;

public class Ship extends BasePositionable implements SpaceObject {

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

    private boolean isMoving = false;

    private float drawX, drawY;

    private float kX, kY;

    private int destinationX, destinationY;

    private static int MOVE_FRAMES = 8;

    public Ship(AlienRace humanity, int x, int y) {
        super(x, y);
        this.humanity = humanity;
        name = "Hawking";
        hull = maxHull = 10;
        weapons.add(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"), StarshipWeapon.MOUNT_ALL));

        drawX = 0.0f;
        drawY = 0.0f;
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
        if (isMoving) {
            drawY+=kY;
            drawX+=kX;
            world.getCamera().offsetX = -drawX;
            world.getCamera().offsetY = -drawY;
            if ((Math.abs(drawY)>=AuroraGame.tileSize)||(Math.abs(drawX)>=AuroraGame.tileSize)) {
                isMoving = false;
                setPos(destinationX, destinationY);
                drawY = 0.0f;
                drawX = 0.0f;
                world.getCamera().offsetX = 0.0f;
                world.getCamera().offsetY = 0.0f;
                world.setUpdatedNextFrame(true);
            }
        }
    }

    public void moveUp() {
        isMoving = true;
        kY = -(AuroraGame.tileSize/MOVE_FRAMES);
        kX = 0.0f;
        destinationX = x;
        destinationY = (y - 1);
    }

    public void moveDown() {
        isMoving = true;
        kY = AuroraGame.tileSize/MOVE_FRAMES;
        kX = 0.0f;
        destinationX = x;
        destinationY = (y + 1);
    }

    public void moveRight() {
        isMoving = true;
        kY = 0.0f;
        kX = AuroraGame.tileSize/MOVE_FRAMES;
        destinationX = (x + 1);
        destinationY = y;
    }

    public void moveLeft() {
        isMoving = true;
        kY = 0.0f;
        kX = -(AuroraGame.tileSize/MOVE_FRAMES);
        destinationX = (x - 1);
        destinationY = y;
    }

    public boolean nowMoving() {
        return isMoving;
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
        g.drawImage(ResourceManager.getInstance().getImage("aurora"), camera.getXCoord(x) + drawX, camera.getYCoord(y) + drawY, null);
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
        world.onPlayerShipDamaged();
        if (hull <= 0) {
            GUI.getInstance().getNifty().gotoScreen("fail_screen");
            FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
            controller.set("ship_destroyed_gameover", "ship_destroyed");
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

    public int getLostCrewMembers() {
        return Ship.MAX_ENGINEERS + Ship.MAX_MILITARY + Ship.MAX_SCIENTISTS - getTotalCrew();
    }

    public void refillCrew(World world) {
        scientists = Ship.MAX_SCIENTISTS;
        engineers = Ship.MAX_ENGINEERS;
        military = Ship.MAX_MILITARY;
        world.onCrewChanged();
    }
}

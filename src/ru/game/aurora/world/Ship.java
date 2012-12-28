/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;

public class Ship implements GameObject, Positionable {

    private int x;

    private int y;

    private int hull;

    private int maxHull;

    private int scientists = 10;

    private int engineers = 5;

    private int military = 5;

    public Ship(int x, int y) {
        this.x = x;
        this.y = y;
        hull = maxHull = 100;
    }

    @Override
    public void update(GameContainer container, World world) {
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
        g.drawImage(ResourceManager.getImage("aurora"), camera.getXCoord(x), camera.getYCoord(y), null);
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
}

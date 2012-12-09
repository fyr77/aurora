/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;

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
    public void update(JGEngine engine, World world) {
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
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(camera.getXCoord(x), camera.getYCoord(y), "aurora");
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
}

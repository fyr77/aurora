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
}

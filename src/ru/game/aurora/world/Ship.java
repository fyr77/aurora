/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import jgame.JGObject;
import jgame.platform.JGEngine;

public class Ship extends JGObject implements GameObject
{

    private int hull;

    private int maxHull;

    public Ship(String name, boolean unique_id, double x, double y, int collisionid, String gfxname) {
        super(name, unique_id, x, y, collisionid, gfxname);
        hull = maxHull = 100;
    }

    @Override
    public void update(JGEngine engine) {
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
    public void draw(JGEngine engine) {
        engine.drawImage(x, y, "aurora");
    }
}

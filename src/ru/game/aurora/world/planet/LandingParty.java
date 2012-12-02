/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:38
 */
package ru.game.aurora.world.planet;

import jgame.platform.JGEngine;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

public class LandingParty implements GameObject
{
    private int x;

    private int y;

    private int military;

    private int science;

    private int engineers;

    public LandingParty(int x, int y, int military, int science, int engineers) {
        this.x = x;
        this.y = y;
        this.military = military;
        this.science = science;
        this.engineers = engineers;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(JGEngine engine, World world) {

    }

    @Override
    public void draw(JGEngine engine) {

    }
}

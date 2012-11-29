/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import jgame.JGColor;
import jgame.JGObject;
import jgame.platform.JGEngine;

public class Ship extends JGObject implements GameObject
{
    public Ship(String name, boolean unique_id, double x, double y, int collisionid, String gfxname) {
        super(name, unique_id, x, y, collisionid, gfxname);
    }

    @Override
    public void update(JGEngine engine) {

    }

    @Override
    public void draw(JGEngine engine) {
        engine.setColor(JGColor.yellow);
        engine.drawOval(x,y,16,16,true,true);
    }
}

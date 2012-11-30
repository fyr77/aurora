/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:21
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.Player;

import java.util.Random;

/**
 * Nebula blocks view and damages player ship
 */
public class Nebula implements GalaxyMapObject
{
    private Random r = new Random();

    @Override
    public void drawOnGlobalMap(JGEngine engine, int tileX, int tileY) {
        engine.setColor(JGColor.gray);
        engine.drawRect(tileX * engine.tileWidth(), tileY * engine.tileHeight(), engine.tileWidth(), engine.tileHeight(), true, false);
    }

    @Override
    public boolean canBeEntered() {
        return false;
    }

    @Override
    public void processCollision(JGEngine engine, Player player) {
        if (r.nextInt(3) == 0) {
            player.getShip().setHull(player.getShip().getHull() - 1);
            GameLogger.getInstance().logMessage("Your ship was damaged while navigating through the nebula");
        }
    }
}

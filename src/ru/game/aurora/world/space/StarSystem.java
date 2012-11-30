/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.Player;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject
{
    public static final JGColor[] possibleColors = {JGColor.red, JGColor.white, JGColor.yellow, JGColor.blue};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    public static class Star
    {
        // 1 is largest star, 4 is smallest
        public final int size;
        public final JGColor color;

        public Star(int size, JGColor color) {
            this.size = size;
            this.color = color;
        }
    }

    private Star star;

    public StarSystem(Star star) {
        this.star = star;
    }

    @Override
    public void drawOnGlobalMap(JGEngine engine, int tileX, int tileY) {
        engine.setColor(star.color);
        engine.drawOval(tileX * engine.tileWidth() + (engine.tileWidth() / 2), tileY * engine.tileHeight() + engine.tileWidth() / 2, engine.tileWidth() / star.size, engine.tileHeight() / star.size, true, true);
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(JGEngine engine, Player player) {
        GameLogger.getInstance().logMessage("Approaching star system. Press <enter> to enter.");
    }
}

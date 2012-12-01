/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import jgame.JGColor;
import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.World;

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

    private int globalMapX;

    private int globalMapY;

    public StarSystem(Star star, int globalMapX, int globalMapY) {
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
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

    @Override
    public void update(JGEngine engine, World world) {
        super.update(engine, world);

        double y = world.getPlayer().getShip().getLastY();
        double x = world.getPlayer().getShip().getLastX();

        if ((engine.getKey(JGEngineInterface.KeyUp) && y ==0)
                ||(engine.getKey(JGEngineInterface.KeyDown) && y == engine.pfTilesY() - 1)
                ||(engine.getKey(JGEngineInterface.KeyLeft) && x == 0)
                || (engine.getKey(JGEngineInterface.KeyRight) && x == engine.pfTilesX() - 1)) {
            GameLogger.getInstance().logMessage("Leaving star system...");
            world.setCurrentRoom(world.getGalaxyMap());
            player.getShip().setPos(globalMapX, globalMapY);
        }
    }

    @Override
    public void enter(Player player) {
        super.enter(player);
        player.getShip().setPos(0, 0);
    }

    @Override
    public void draw(JGEngine engine) {
        player.getShip().draw(engine);
        engine.setColor(star.color);

        engine.drawOval(engine.pfTilesX() / 2 * engine.tileWidth() + (engine.tileWidth() / 2), engine.pfTilesY() / 2* engine.tileHeight() + engine.tileWidth() / 2, engine.tileWidth() / star.size, engine.tileHeight() / star.size, true, true);

    }
}

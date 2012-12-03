/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 03.12.12
 * Time: 12:25
 */
package ru.game.aurora.application;

import jgame.platform.JGEngine;
import ru.game.aurora.world.Positionable;

/**
 * Camera defines coordinate transformation from in-game global coordinates to screen coordinates
 * Has position. A tile that has coordinates equal to this position is drawn in the middle of a screen
 */
public class Camera {

    public static class FixedPosition implements Positionable {
        private final int x;
        private final int y;

        public FixedPosition(int x, int y) {
            this.x = x;
            this.y = y;
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
            // empty
        }
    }

    public Camera(JGEngine engine) {
        this.engine = engine;
    }

    private JGEngine engine;

    /**
     * Object camera is following
     */
    private Positionable target;

    public void setTarget(Positionable target) {
        this.target = target;
    }

    /**
     * Get absolute screen x coordinate for tile that has given x index
     *
     * @param globalTileX Tile horizontal index
     * @return Absolute screen x coordinate for this tile
     */
    public int getXCoord(int globalTileX) {
        return engine.tileWidth() * (engine.pfTilesX() / 2 + (globalTileX - target.getX()));
    }

    public int getYCoord(int globalTileY) {
        return engine.tileHeight() * (engine.pfTilesY() / 2 + (globalTileY - target.getY()));
    }

    public int getNumTilesX() {
        return engine.pfTilesX();
    }

    public int getNumTilesY() {
        return engine.pfTilesY();
    }

}

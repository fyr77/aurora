package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;

/**
 * Dungeon is a location with a fixed tiled map, which can be explored by player landing party
 */
public class Dungeon implements Room {
    private ITileMap map;

    public Dungeon(ITileMap map) {
        this.map = map;
    }

    @Override
    public void enter(World world) {
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
    }
}

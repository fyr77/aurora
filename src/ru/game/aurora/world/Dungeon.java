package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.gui.GUI;

/**
 * Dungeon is a location with a fixed tiled map, which can be explored by player landing party
 */
public class Dungeon implements Room
{
    private static final long serialVersionUID = -6449421434646859444L;

    private ITileMap map;

    private DungeonController controller;

    public Dungeon(World world, ITileMap map, Room prevRoom) {
        this.map = map;
        this.controller = new DungeonController(world, prevRoom, map, false);
    }

    @Override
    public void enter(World world) {
        GUI.getInstance().getNifty().gotoScreen("surface_gui");
        world.getCamera().setTarget(world.getPlayer().getLandingParty()); //todo: what if null?
    }

    @Override
    public void update(GameContainer container, World world) {
        controller.update(container, world);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        map.draw(container, graphics, camera);
        controller.draw(container, graphics, camera);
    }
}

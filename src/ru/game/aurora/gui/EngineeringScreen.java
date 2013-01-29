/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 16:09
 */

package ru.game.aurora.gui;

import de.matthiasmann.twl.Widget;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;


public class EngineeringScreen implements Room {
    private static final long serialVersionUID = 2492522745773237295L;

    private Rectangle titleRect = new Rectangle(1, 1, 12, 1);

    private Rectangle contentRect = new Rectangle(1, 3, 12, 4);

    private int selectedIdx = 0;

    private World world;

    private Room prevRoom;

    @Override
    public void enter(World world) {
        this.world = world;
        prevRoom = world.getCurrentRoom();
    }

    @Override
    public Widget getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            world.setCurrentRoom(prevRoom);
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_UP) || container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            selectedIdx = 1 - selectedIdx;
            return;
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        final EngineeringState state = world.getPlayer().getEngineeringState();

        EngineUtils.drawRectWithBorderAndText(graphics, titleRect, camera, "Hull repairs");
        EngineUtils.drawRectWithBorder(graphics, contentRect, camera, Color.yellow, GUIConstants.backgroundColor);


        EngineUtils.drawString(graphics, "< Hull points to repair: " + state.getHullRepairs().remainingPoints + " >", 2 * camera.getTileWidth(), 4 * camera.getTileHeight(), 12 * camera.getTileWidth(), GUIConstants.dialogFont, selectedIdx == 0 ? Color.green : Color.white);
        EngineUtils.drawString(graphics, "< Assigned engineers: " + state.getHullRepairs().engineersAssigned + " >", 2 * camera.getTileWidth(), 5 * camera.getTileHeight(), 12 * camera.getTileWidth(), GUIConstants.dialogFont, selectedIdx == 1 ? Color.green : Color.white);

        EngineUtils.drawString(graphics, "Requires " + state.getHullRepairs().calcResCost() + " out of " + world.getPlayer().getResourceUnits() + " resource units", 2 * camera.getTileWidth(), 6 * camera.getTileHeight(), 12 * camera.getTileWidth());

    }
}

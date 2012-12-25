/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.12
 * Time: 12:57
 */
package ru.game.aurora.ui;

import jgame.JGColor;
import jgame.JGRectangle;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.util.JGEngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Standart in-game screen for lists of items with icon and description
 */
public abstract class ListWithIconAndDescrScreen implements Room {

    protected List<String> strings = new ArrayList<String>(24);

    protected static final JGRectangle captionRect = new JGRectangle(1, 1, 12, 1);

    protected static final JGRectangle listRect = new JGRectangle(1, 3, 7, 10);

    protected static final JGRectangle imageRect = new JGRectangle(9, 3, 4, 4);

    protected static final JGRectangle descriptionRect = new JGRectangle(9, 8, 4, 5);

    protected static final JGColor backgroundColor = new JGColor(4, 7, 125);

    protected int currentIdx;

    protected Room previousRoom;

    protected World world;

    protected int maxIdx;

    @Override
    public void enter(World world) {
        this.world = world;
        this.previousRoom = world.getCurrentRoom();
    }

    public void draw(JGEngine engine, Camera camera, String caption, String image, String description) {
        JGEngineUtils.drawRectWithBorder(engine, captionRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, listRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, imageRect, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, descriptionRect, camera, JGColor.yellow, backgroundColor);

        engine.drawString(caption, camera.getRelativeX(captionRect.x) + 200, camera.getRelativeY(captionRect.y) + 20, -1, GUIConstants.dialogFont, JGColor.yellow);

        if (image != null) {
            engine.drawImage(image, camera.getRelativeX(imageRect.x), camera.getRelativeY(imageRect.y));
        }

        if (description == null) {
            description = "<Select a research project>";
        }
        JGEngineUtils.drawString(engine, description, camera.getRelativeX(descriptionRect.x) + 10, camera.getRelativeY(descriptionRect.y) + 10, camera.getTileWidth() * descriptionRect.width - 20, GUIConstants.dialogFont, JGColor.yellow);

        if (strings.isEmpty()) {
            engine.drawString("<No projects in this category>", camera.getRelativeX(listRect.x) + 10, camera.getRelativeY(listRect.y) + 10, -1, GUIConstants.dialogFont, JGColor.yellow);
        } else {
            for (int i = 0; i < strings.size(); ++i) {
                engine.drawString(strings.get(i), camera.getRelativeX(listRect.x) + 20, camera.getRelativeY(listRect.y + i) + 20, -1, GUIConstants.dialogFont, i == currentIdx ? JGColor.green : JGColor.yellow);
            }
        }
    }

    @Override
    public void update(JGEngine engine, World world) {
        if (engine.getKey(JGEngine.KeyUp)) {
            currentIdx--;
            if (currentIdx < 0) {
                currentIdx = maxIdx - 1;
            }
        }

        if (engine.getKey(JGEngine.KeyDown)) {
            currentIdx++;
            if (currentIdx == maxIdx) {
                currentIdx = 0;
            }
        }

        if (engine.getKey(JGEngine.KeyEsc)) {
            world.setCurrentRoom(previousRoom);
        }
    }
}

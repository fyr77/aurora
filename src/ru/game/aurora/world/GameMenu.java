package ru.game.aurora.world;

import de.matthiasmann.twl.Widget;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.SaveGameManager;
import ru.game.aurora.util.EngineUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 22.01.13
 * Time: 17:19
 */

public class GameMenu implements Room {
    private static final long serialVersionUID = -6044928392351679484L;

    private int selectedIndex = 0;

    private static final Rectangle continueRectangle = new Rectangle(5, 2, 5, 2);

    private static final Rectangle saveRectangle = new Rectangle(5, 5, 5, 2);

    private static final Rectangle quitRectangle = new Rectangle(5, 8, 5, 2);

    private Room prevRoom;

    @Override
    public void enter(World world) {
        this.prevRoom = world.getCurrentRoom();
    }

    @Override
    public Widget getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_DOWN) && selectedIndex < 2) {
            selectedIndex++;
        }

        if (container.getInput().isKeyPressed(Input.KEY_UP) && selectedIndex > 0) {
            selectedIndex--;
        }

        boolean enterPressed = container.getInput().isKeyPressed(Input.KEY_ENTER);

        if ((enterPressed && selectedIndex == 0) || EngineUtils.checkRectanglePressed(container, world.getCamera(), continueRectangle)) {
            world.setCurrentRoom(prevRoom);
            return;
        }

        if ((enterPressed && selectedIndex == 1) || EngineUtils.checkRectanglePressed(container, world.getCamera(), saveRectangle)) {
            world.setCurrentRoom(prevRoom);
            SaveGameManager.saveGame(world);
            GameLogger.getInstance().logMessage("Game saved");
            return;
        }
        if ((enterPressed && selectedIndex == 2) || EngineUtils.checkRectanglePressed(container, world.getCamera(), quitRectangle)) {
            container.exit();
        }

    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        EngineUtils.drawRectWithBorderAndText(graphics, continueRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Continue", GUIConstants.dialogFont, selectedIndex == 0 ? Color.green : Color.white);
        EngineUtils.drawRectWithBorderAndText(graphics, saveRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Save", GUIConstants.dialogFont, selectedIndex == 1 ? Color.green : Color.white);
        EngineUtils.drawRectWithBorderAndText(graphics, quitRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Exit", GUIConstants.dialogFont, selectedIndex == 2 ? Color.green : Color.white);
    }
}

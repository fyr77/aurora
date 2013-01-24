package ru.game.aurora.application;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.WorldGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.01.13
 * Time: 16:23
 */
public class MainMenu {
    private static final long serialVersionUID = 916916126787160191L;

    private int selectedIndex;

    private static final Rectangle continueRectangle = new Rectangle(2, 9, 3, 1);

    private static final Rectangle newGameRectangle = new Rectangle(2, 11, 3, 1);

    private static final Rectangle quitRectangle = new Rectangle(2, 13, 3, 1);

    private static final Rectangle worldGenerateMessageRectangle = new Rectangle(5, 5, 10, 2);

    private final boolean saveAvailable;

    private WorldGenerator generator;

    public MainMenu() {
        saveAvailable = SaveGameManager.isSaveAvailable();
        selectedIndex = saveAvailable ? 0 : 1;
    }

    public World update(GameContainer container) {
        if (generator != null) {
            if (generator.isGenerated()) {
                return generator.getWorld();
            }
            return null;
        }


        if (container.getInput().isKeyPressed(Input.KEY_DOWN) && selectedIndex < 2) {
            selectedIndex++;
        }

        if (container.getInput().isKeyPressed(Input.KEY_UP) && selectedIndex > 0 && (saveAvailable || selectedIndex > 1)) {
            selectedIndex--;
        }

        if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
            switch (selectedIndex) {
                case 0:
                    return SaveGameManager.loadGame();
                case 1:
                    createNewGame();
                case 2:
                    container.exit();
                    break;
            }
        }

        if (container.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
            int mouseX = container.getInput().getMouseX() / 64;
            int mouseY = container.getInput().getMouseY() / 64;
            if (saveAvailable && continueRectangle.includes(mouseX, mouseY)) {
                return SaveGameManager.loadGame();
            }

            if (newGameRectangle.includes(mouseX, mouseY)) {
                createNewGame();
            }

            if (quitRectangle.includes(mouseX, mouseY)) {
                container.exit();
            }
        }
        return null;
    }

    private void createNewGame() {
        generator = new WorldGenerator();
        new Thread(generator).start();
    }

    public void draw(Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage("menu_background"), 0, 0);
        if (generator == null) {
            EngineUtils.drawRectWithBorderAndText(graphics, continueRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Continue", GUIConstants.dialogFont, selectedIndex == 0 ? Color.green : (saveAvailable ? Color.white : Color.gray));
            EngineUtils.drawRectWithBorderAndText(graphics, newGameRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "New Game", GUIConstants.dialogFont, selectedIndex == 1 ? Color.green : Color.white);
            EngineUtils.drawRectWithBorderAndText(graphics, quitRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Exit", GUIConstants.dialogFont, selectedIndex == 2 ? Color.green : Color.white);
        } else {
            EngineUtils.drawRectWithBorderAndText(graphics, worldGenerateMessageRectangle, camera, Color.yellow, GUIConstants.backgroundColor, "Generating world: " + generator.getCurrentStatus(), GUIConstants.dialogFont, Color.white);
        }
        graphics.drawString(Version.VERSION, camera.getTileWidth() * camera.getNumTilesX() - 100, camera.getTileHeight() * camera.getNumTilesY() - 40);
    }
}

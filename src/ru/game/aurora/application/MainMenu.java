package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.StoryScreen;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.01.13
 * Time: 16:23
 */
public class MainMenu {
    private static final long serialVersionUID = 2L;

    private static final Rectangle worldGenerateMessageRectangle = new Rectangle(5, 5, 10, 2);

    private WorldGenerator generator;

    private World loadedState = null;

    // used for changing number of dots in message while generating world
    private int dotsCount = 0;

    private long lastTimeChecked = 0;

    private GameContainer container;

    public static final class MainMenuController implements ScreenController {

        private MainMenu menu;

        private GameContainer container;

        public void setMenu(GameContainer con, MainMenu menu) {
            this.container = con;
            this.menu = menu;
        }

        @Override
        public void bind(Nifty nifty, Screen screen) {

        }

        @Override
        public void onStartScreen() {

        }

        @Override
        public void onEndScreen() {

        }

        // these methods are specified in screen xml description and called using reflection
        public void loadGame() {
            menu.loadedState = SaveGameManager.loadGame();
            GUI.getInstance().onWorldLoaded(container, menu.loadedState);
            GUI.getInstance().getNifty().gotoScreen("empty_screen");
            menu.loadedState.getCurrentRoom().enter(menu.loadedState);

        }

        public void newGame() {
            menu.generator = new WorldGenerator();
            new Thread(menu.generator).start();
            GUI.getInstance().getNifty().gotoScreen("empty_screen");
        }

        public void exitGame() {
            menu.container.exit();
        }
    }

    public MainMenu(GameContainer container) {
        boolean saveAvailable = SaveGameManager.isSaveAvailable();
        this.container = container;

        final Nifty nifty = GUI.getInstance().getNifty();
        nifty.gotoScreen("main_menu");
        MainMenuController con = (MainMenuController) nifty.getCurrentScreen().getScreenController();
        con.setMenu(container, this);


        if (!saveAvailable) {
            nifty.getCurrentScreen().findElementByName("panel").findElementByName("continue_game_button").disable();
        }
    }

    public World update(Camera camera, GameContainer container) {
        if (generator != null) {
            if (generator.isGenerated()) {
                final World world = generator.getWorld();
                GUI.getInstance().onWorldLoaded(container, world);
                world.setCamera(camera);
                world.getCurrentRoom().enter(world);
                // add them here and not in world generator, as gui must be created first
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/tutorials/game_start_tutorial.json"));
                world.addOverlayWindow(new StoryScreen("story/beginning.json"));
                return world;
            }
            if (container.getTime() - lastTimeChecked > 500) {
                if (dotsCount++ > 5) {
                    dotsCount = 0;
                }
                lastTimeChecked = container.getTime();
            }
            return null;
        }

        return loadedState;
    }


    public void draw(Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage("menu_background"), 0, 0);
        if (generator != null) {
            StringBuilder sb = new StringBuilder("Generating world: ");
            sb.append(generator.getCurrentStatus());
            for (int i = 0; i < dotsCount; ++i) {
                sb.append(".");
            }
            EngineUtils.drawRectWithBorderAndText(graphics, worldGenerateMessageRectangle, camera, Color.yellow, GUIConstants.backgroundColor, sb.toString(), GUIConstants.dialogFont, Color.white, true);
        }
        graphics.drawString(Version.VERSION, camera.getTileWidth() * camera.getNumTilesX() + GameLogger.getInstance().getStatusMessagesRect().getWidth() - 100, camera.getTileHeight() * camera.getNumTilesY() - 40);
    }


}

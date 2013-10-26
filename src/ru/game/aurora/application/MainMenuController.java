package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
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
public class MainMenuController implements ScreenController {

    private WorldGenerator generator;

    private World loadedState = null;

    // used for changing number of dots in message while generating world
    private int dotsCount = 0;

    private long lastTimeChecked = 0;

    private GameContainer container;

    private Animation shuttle_landing;


    // these methods are specified in screen xml description and called using reflection
    public void loadGame() {
        final Nifty nifty = GUI.getInstance().getNifty();
        loadedState = SaveGameManager.loadGame();
        if (loadedState == null) {
            Element popup = nifty.createPopup("load_failed");
            nifty.showPopup(nifty.getScreen("main_menu"), popup.getId(), null);
        }
        GUI.getInstance().onWorldLoaded(container, loadedState);
        nifty.gotoScreen("empty_screen");
        loadedState.getCurrentRoom().enter(loadedState);

    }

    public void newGame() {
        shuttle_landing = ResourceManager.getInstance().getAnimation("shuttle_landing");
        shuttle_landing.setAutoUpdate(false);
        shuttle_landing.setLooping(true);
        shuttle_landing.start();
        generator = new WorldGenerator();
        new Thread(generator).start();
        Element elem = GUI.getInstance().getNifty().createPopup("generation");
        GUI.getInstance().getNifty().showPopup(GUI.getInstance().getNifty().getCurrentScreen(), elem.getId(), null);
        EngineUtils.setTextForGUIElement(elem.findElementByName("generation_text"), "Initializing...");
    }

    public void exitGame() {
        container.exit();
    }

    public MainMenuController(GameContainer container) {
        this.container = container;
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
        if (generator != null) {
            StringBuilder sb = new StringBuilder(Localization.getText("gui", "generation.prefix")).append(" ");
            sb.append(generator.getCurrentStatus());
            for (int i = 0; i < dotsCount; ++i) {
                sb.append(".");
            }
            final Element topMostPopup = GUI.getInstance().getNifty().getTopMostPopup();
            EngineUtils.setTextForGUIElement(topMostPopup.findElementByName("generation_text"), sb.toString());
            final Element shuttle_image = topMostPopup.findElementByName("shuttle_image");
            final long delta = container.getTime() - AuroraGame.getLastFrameTime();
            shuttle_landing.update(delta);
            EngineUtils.setImageForGUIElement(shuttle_image, shuttle_landing.getCurrentFrame());
        }
    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        EngineUtils.setTextForGUIElement(screen.findElementByName("version_text"), Version.VERSION);
    }

    @Override
    public void onStartScreen() {
        boolean saveAvailable = SaveGameManager.isSaveAvailable();

        if (!saveAvailable) {
            GUI.getInstance().getNifty().getCurrentScreen().findElementByName("panel").findElementByName("continue_game_button").disable();
        }
    }

    @Override
    public void onEndScreen() {
    }

    public void reset() {
        generator = null;
        loadedState = null;
        dotsCount = 0;
        lastTimeChecked = 0;
    }

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
    }
}

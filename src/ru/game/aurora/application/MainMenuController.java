package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.HelpPopupControl;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.gui.LoadingScreenController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.01.13
 * Time: 16:23
 */
public class MainMenuController implements ScreenController, ResolutionChangeListener {

    private WorldGenerator generator;

    private final GameContainer container;

    private final Animation upperEngine;

    private final Animation lowerEngine;

    private MainMenuBackground background;

    private static final Logger logger = LoggerFactory.getLogger(MainMenuController.class);

    public MainMenuController(GameContainer container) {
        this.container = container;
        upperEngine = ResourceManager.getInstance().getAnimation("menu_engine_top");
        upperEngine.setAutoUpdate(true);
        upperEngine.setLooping(true);
        upperEngine.start();
        lowerEngine = ResourceManager.getInstance().getAnimation("menu_engine_bottom");
        lowerEngine.setAutoUpdate(true);
        lowerEngine.setLooping(true);
        lowerEngine.start();
    }

    // these methods are specified in screen xml description and called using reflection
    public void loadGame() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("saveload_screen");
    }

    public void newGame() {
        generator = new WorldGenerator();
        World world = generator.initWorld();
        world.checkCheats();

        GUI.getInstance().onWorldLoaded(container, world);
        new Thread(generator).start();
        ((LoadingScreenController) GUI.getInstance().getNifty().findScreenController(LoadingScreenController.class.getCanonicalName())).setGenerator(generator);

        if (Configuration.getBooleanProperty("debug.skipIntro")) {
            logger.warn("Skipping intro because debug.skipIntro property is set");
            world.getGlobalVariables().put("player.country", "america");
            GUI.getInstance().getNifty().gotoScreen("loading_screen");
        } else {
            world.addOverlayWindow(createInitialDialog());
            GUI.getInstance().popScreen();
            GUI.getInstance().pushScreen("loading_screen");

            IntroDialog dialog = IntroDialog.load("story/intro_start.json");
            GUI.getInstance().pushScreen("dialog_screen");
            IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getName());
            introDialogController.pushDialog(dialog);
            introDialogController.pushDialog(IntroDialog.load("story/intro_1.json"));
            introDialogController.setEndListener(new IStateChangeListener<World>() {

                private static final long serialVersionUID = -167349630557155374L;

                @Override
                public void stateChanged(World world) {
                    GUI.getInstance().pushCurrentScreen();
                    GUI.getInstance().getNifty().gotoScreen("country_select_screen");
                }
            });
            GUI.getInstance().getNifty().gotoScreen("intro_dialog");
        }

    }

    public void exitGame() {
        AuroraGame.showExitConfirmation(false);
    }

    public void openBlog() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create("http://auroraroguelike.com"));
            } catch (IOException e) {
                logger.error("Failed to open blog uri", e);
            }
        }
    }

    public void openTracker() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create("https://bitbucket.org/e_smirnov/aurora/issues"));
            } catch (IOException e) {
                logger.error("Failed to open bugtracker uri", e);
            }
        }
    }


    public void openSettings() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("settings_screen");
    }

    private Dialog createInitialDialog() {
        final Dialog gameStartDialog = Dialog.loadFromFile("dialogs/game_start_tutorial.json");
        gameStartDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 3479062521122587288L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (returnCode != 0) {
                    GUI.getInstance().pushScreen("loading_screen");
                    GUI.getInstance().getNifty().gotoScreen("ship_screen");
                }
            }
        });

        return gameStartDialog;
    }


    public World update(Camera camera, GameContainer container) {
        if (background != null) {
            background.update(container);
        }
        if (generator != null) {
            if (generator.isGenerated() && GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("loading_screen")) {
                final World world = generator.getWorld();
                world.setCamera(camera);
                world.getCurrentRoom().enter(world);
                // add them here and not in world generator, as gui must be created first
                HelpPopupControl.setHelpIds("start", "galaxy_map", "galaxy_map_2", "galaxy_map_3");
                HelpPopupControl.showHelp();
                return world;
            }

            return null;
        }

        return null;
    }


    public void draw(Graphics graphics) {
        if (background != null) {
            background.draw(graphics);
            Image shipImage = ResourceManager.getInstance().getImage("menu_ship");
            int shipX = (int) (AuroraGame.tilesX * AuroraGame.tileSize * 0.1);
            int shipY = AuroraGame.tilesY * AuroraGame.tileSize - shipImage.getHeight() - 100;
            graphics.drawAnimation(upperEngine, shipX - 195, shipY + 110);
            graphics.drawAnimation(lowerEngine, shipX - 195, shipY + 200);
            graphics.drawImage(shipImage, shipX, shipY);
        }

    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        EngineUtils.setTextForGUIElement(screen.findElementByName("version_text"), Version.VERSION);
    }

    @Override
    public void onStartScreen() {
        background = new MainMenuBackground(AuroraGame.tilesX * AuroraGame.tileSize, AuroraGame.tilesY * AuroraGame.tileSize);

        final Element loadGameButton = GUI.getInstance().getNifty().getCurrentScreen().findElementByName("panel").findElementByName("continue_game_button");
        if (SaveGameManager.hasSaves()) {
            loadGameButton.enable();
        } else {
            loadGameButton.disable();
        }
    }

    @Override
    public void onEndScreen() {
    }

    public void reset() {
        generator = null;
    }

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
    }

    @Override
    public void onResolutionChanged(int tilesX, int tilesY, boolean fullscreen) {
        background = new MainMenuBackground(tilesX * AuroraGame.tileSize, tilesY * AuroraGame.tileSize);
    }
}

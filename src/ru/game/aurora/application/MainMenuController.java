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

    private World loadedState = null;


    private GameContainer container;

    private Animation upperEngine;

    private Animation lowerEngine;

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
        final Nifty nifty = GUI.getInstance().getNifty();
        loadedState = SaveGameManager.loadGame();
        if (loadedState == null) {
            Element popup = nifty.createPopup("load_failed");
            nifty.showPopup(nifty.getScreen("main_menu"), popup.getId(), null);
            return;
        }
        loadedState.gameLoaded();
        GUI.getInstance().onWorldLoaded(container, loadedState);
        loadedState.getCurrentRoom().returnTo(loadedState);
    }

    public void newGame() {
        generator = new WorldGenerator();
        World world = generator.initWorld();

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
                Desktop.getDesktop().browse(URI.create("http://auroraroguelike.wordpress.com"));
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
        final Dialog gameStartDialog = Dialog.loadFromFile("dialogs/tutorials/game_start_tutorial.json");
        gameStartDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 3479062521122587288L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                Dialog d = null;
                switch (returnCode) {
                    case 3:
                        d = Dialog.loadFromFile("dialogs/tutorials/marine_intro.json");
                        world.getGlobalVariables().put("crew.military", 1);
                        d.addListener(new DialogListener() {
                            private static final long serialVersionUID = -7928559144883640398L;

                            @Override
                            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                                if (returnCode == -1) {
                                    // player has made a mistake, military chief will not be friendly with him
                                    world.getGlobalVariables().put("crew.military", -1);
                                }
                                // return to initial dialog, if player will want to visit other crewmembers
                                world.addOverlayWindow(gameStartDialog);
                            }
                        });
                        break;
                    case 2:
                        d = Dialog.loadFromFile("dialogs/tutorials/engineer_intro.json");
                        world.getGlobalVariables().put("crew.engineer", 1);
                        d.addListener(new DialogListener() {

                            private static final long serialVersionUID = -5149956426932570110L;

                            @Override
                            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                                if (returnCode == -1) {
                                    // player has made a mistake, engineer chief will not be friendly with him
                                    world.getGlobalVariables().put("crew.engineer", -1);
                                }
                                if (flags.containsKey("engineer_dinner")) {
                                    world.getGlobalVariables().put("crew.engineer", 1);
                                }
                                // return to initial dialog, if player will want to visit other crewmembers
                                world.addOverlayWindow(gameStartDialog);
                            }
                        });
                        break;

                    case 1:
                        d = Dialog.loadFromFile("dialogs/tutorials/scientist_intro.json");
                        world.getGlobalVariables().put("crew.scientist", 1);
                        d.addListener(new DialogListener() {
                            private static final long serialVersionUID = 3028202497230253046L;

                            @Override
                            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                                if (returnCode == -1) {
                                    // player has made a mistake, engineer chief will not be friendly with him
                                    world.getGlobalVariables().put("crew.scientist", -1);
                                }
                                // return to initial dialog, if player will want to visit other crewmembers
                                world.addOverlayWindow(gameStartDialog);
                            }
                        });
                        break;
                    case 0:
                        // set remaining crew relationships
                        if (!world.getGlobalVariables().containsKey("crew.scientist")) {
                            world.getGlobalVariables().put("crew.scientist", 0);
                        }
                        if (!world.getGlobalVariables().containsKey("crew.engineer")) {
                            world.getGlobalVariables().put("crew.engineer", 0);
                        }
                        if (!world.getGlobalVariables().containsKey("crew.military")) {
                            world.getGlobalVariables().put("crew.military", 0);
                        }
                }

                if (d != null) {
                    // player decided to visit one of his crew mates
                    world.addOverlayWindow(d);
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
                return world;
            }

            return null;
        }

        return loadedState;
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
    }

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
    }

    @Override
    public void onResolutionChanged(int tilesX, int tilesY, boolean fullscreen) {
        background = new MainMenuBackground(tilesX * AuroraGame.tileSize, tilesY * AuroraGame.tileSize);
    }
}

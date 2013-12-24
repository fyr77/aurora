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
import ru.game.aurora.gui.StoryScreen;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;

import java.util.Map;

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
        GUI.getInstance().onWorldLoaded(container, loadedState);
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
        Configuration.saveSystemProperties();
        container.exit();
    }

    public void openSettings() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("settings_screen");
    }

    private Dialog createInitialDialog(final World world)
    {
        final Dialog gameStartDialog = Dialog.loadFromFile("dialogs/tutorials/game_start_tutorial.json");
        gameStartDialog.setListener(new DialogListener() {
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                Dialog d = null;
                switch (returnCode) {
                    case 3:
                        d = Dialog.loadFromFile("dialogs/tutorials/marine_intro.json");
                        world.getGlobalVariables().put("crew.military", 1);
                        d.setListener(new DialogListener() {
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
                        d.setListener(new DialogListener() {
                            @Override
                            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                                if (returnCode == -1) {
                                    // player has made a mistake, engineer chief will not be friendly with him
                                    world.getGlobalVariables().put("crew.engineer", -1);
                                }
                                // return to initial dialog, if player will want to visit other crewmembers
                                world.addOverlayWindow(gameStartDialog);
                            }
                        });
                        break;

                    case 1:
                        d = Dialog.loadFromFile("dialogs/tutorials/scientist_intro.json");
                        world.getGlobalVariables().put("crew.scientist", 1);
                        d.setListener(new DialogListener() {
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
        background.update(container);
        if (generator != null) {
            if (generator.isGenerated()) {
                final World world = generator.getWorld();
                GUI.getInstance().onWorldLoaded(container, world);
                world.setCamera(camera);
                world.getCurrentRoom().enter(world);
                // add them here and not in world generator, as gui must be created first

                if (Configuration.getBooleanProperty("debug.skipIntro")) {
                    logger.warn("Skipping intro because debug.skipIntro property is set");
                    world.getGlobalVariables().put("player.country", "america");
                    return world;
                }

                world.addOverlayWindow(createInitialDialog(world));

                world.addOverlayWindow(new StoryScreen("story/beginning.json"));
                IntroDialog dialog = IntroDialog.load("story/intro_1.json");
                GUI.getInstance().pushCurrentScreen();
                IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getName());
                introDialogController.setIntroDialog(dialog);
                introDialogController.setEndListener(new IStateChangeListener() {
                    @Override
                    public void stateChanged(World world) {
                        GUI.getInstance().pushCurrentScreen();
                        GUI.getInstance().getNifty().gotoScreen("country_select_screen");
                    }
                });
                GUI.getInstance().getNifty().gotoScreen("intro_dialog");
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
        background.draw(graphics);
        Image shipImage = ResourceManager.getInstance().getImage("menu_ship");
        int shipX = (int) (AuroraGame.tilesX * AuroraGame.tileSize * 0.1);
        int shipY = (int) (AuroraGame.tilesY * AuroraGame.tileSize - shipImage.getHeight() - 100);
        graphics.drawAnimation(upperEngine, shipX - 195, shipY + 110);
        graphics.drawAnimation(lowerEngine, shipX - 195, shipY + 200);
        graphics.drawImage(shipImage, shipX, shipY);


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
        dotsCount = 0;
        lastTimeChecked = 0;
    }

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
    }
}

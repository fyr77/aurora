/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.slick2d.NiftyOverlayGame;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.AnimalGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;


public class AuroraGame extends NiftyOverlayGame {

    private World world;

    private MainMenuController mainMenu;

    public static final int tileSize = 64;

    public static final int tilesX = 20;

    public static final int tilesY = 15;

    final Camera camera = new Camera(0, 0, tilesX, tilesY, tileSize, tileSize);

    private static long lastFrameTime;

    public AuroraGame() {

    }


    @Override
    protected void initGameAndGUI(GameContainer gameContainer) throws SlickException {
        ResourceManager.getInstance().loadResources(AuroraGame.class.getClassLoader().getResourceAsStream("resources.xml"));
        gameContainer.getInput().enableKeyRepeat();
        gameContainer.setTargetFrameRate(60);

        initNifty(gameContainer);
        GUI.init(gameContainer, getNifty());
        GUI.getInstance().getNifty().gotoScreen("main_menu");
        mainMenu = (MainMenuController) GUI.getInstance().getNifty().findScreenController(MainMenuController.class.getCanonicalName());
        try {
            AnimalGenerator.init();
        } catch (FileNotFoundException e) {
            throw new SlickException("Failed to initialize Monster Generator", e);
        }
        lastFrameTime = gameContainer.getTime();

    }

    @Override
    protected void prepareNifty(Nifty nifty) {
        nifty.loadStyleFile("gui/style/aurora-style.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
    }

    @Override
    protected void updateGame(GameContainer gameContainer, int i) throws SlickException {
        if (mainMenu != null) {
            world = mainMenu.update(camera, gameContainer);
            if (world != null) {
                mainMenu = null;
            }
        } else {
            world.update(gameContainer);
            if (world.isGameOver()) {
                mainMenu = (MainMenuController) GUI.getInstance().getNifty().findScreenController(MainMenuController.class.getCanonicalName());
                mainMenu.reset();
                world = null;
                GUI.getInstance().getNifty().gotoScreen("main_menu");
            }
        }
        gameContainer.getInput().clearKeyPressedRecord();
    }

    @Override
    public boolean closeRequested() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Aurora " + Version.VERSION;
    }

    @Override
    protected void renderGame(GameContainer gameContainer, Graphics graphics) throws SlickException {
        if (mainMenu != null) {
            mainMenu.draw(graphics, camera);
        } else {
            world.draw(gameContainer, graphics);
            world.getCamera().drawBound();
        }

        lastFrameTime = gameContainer.getTime();
    }

    private static void addDir(String s) throws IOException {
        try {
            // This enables the java.library.path to be modified at runtime
            // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (String path : paths) {
                if (s.equals(path)) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);
            System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get field handle to set library path");
        }
    }

    public static void main(String[] args) throws SlickException, IOException {

        System.out.println("Aurora game version " + Version.VERSION + " started");
        final String osName = System.getProperty("os.name");
        String nativePath;
        if (osName.contains("Windows")) {
            nativePath = "native/windows";
        } else if (osName.contains("Linux")) {
            nativePath = "native/linux";
        } else if (osName.contains("Mac")) {
            nativePath = "native/macosx";
        } else {
            System.err.println("Unsupported os " + osName + ", lwjgl has no native libraries for it");
            throw new RuntimeException("Unsupported os " + osName + ", lwjgl has no native libraries for it");
        }
        System.out.println("Setting native lib dir to " + nativePath);
        addDir(nativePath);

        if (args.length != 0) {
            Localization.init(Locale.forLanguageTag(args[0]));
        } else {
            Localization.init(Locale.getDefault());
        }
        AppGameContainer app = new AppGameContainer(new AuroraGame());
        app.setDisplayMode(tilesX * tileSize, tilesY * tileSize, false);
        app.start();
    }

    public static long getLastFrameTime() {
        return lastFrameTime;
    }
}

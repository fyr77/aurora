/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;


public class AuroraGame extends BasicGame {

    private World world;

    private static final int tileSize = 64;

    private static final int tilesX = 20;

    private static final int tilesY = 15;

    private SaveGameManager saveGameManager = new SaveGameManager();

    public AuroraGame() {
        super("Aurora");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        world = new World(new Camera(0, 0, tilesX, tilesY, tileSize, tileSize), 100, 100);
        GameLogger.init(new Rectangle((tilesX - 5) * tileSize, 0, 5 * tileSize, 10 * tileSize), new Rectangle((tilesX - 5) * tileSize, 10 * tileSize, 5 * tileSize, 5 * tileSize));
        ResourceManager.getInstance().loadResources(AuroraGame.class.getClassLoader().getResourceAsStream("resources.xml"));
        gameContainer.getInput().enableKeyRepeat();
        gameContainer.setTargetFrameRate(60);

        GUI.init(gameContainer, new Rectangle((tilesX - 5) * tileSize, 0, 5 * tileSize, 15 * tileSize));
        //GUI.getInstance().setCurrentScreen(new GalaxyMapWidget(world));
    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        world.update(gameContainer);
        GUI.getInstance().update(gameContainer);
        gameContainer.getInput().clearKeyPressedRecord();
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        world.draw(gameContainer, graphics);
        world.getCamera().drawBound();
        GameLogger.getInstance().draw(graphics);
        GUI.getInstance().draw(gameContainer, graphics);
        GameLogger.getInstance().clearStatusMessages();
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

        AppGameContainer app = new AppGameContainer(new AuroraGame());
        app.setDisplayMode(tilesX * tileSize, tilesY * tileSize, false);
        app.start();
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import com.codedisaster.steamworks.SteamAPI;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.slick2d.NiftyOverlayGame;
import org.apache.commons.io.IOUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.*;
import org.newdawn.slick.openal.SoundStore;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ru.game.aurora.gui.ExitConfirmationScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.HelpPopupControl;
import ru.game.aurora.modding.ModManager;
import ru.game.aurora.steam.AchievementManager;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.AnimalGenerator;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.LogManager;


public class AuroraGame extends NiftyOverlayGame {

    public static final int tileSize = 64;
    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
    public static final String SUN_JAVA_COMMAND = "sun.java.command";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AuroraGame.class);
    private static final Set<Updatable> updatables = new HashSet<>();
    private static final List<ResolutionChangeListener> resolutionChangeListeners = new ArrayList<>();
    public static int tilesX;
    public static int tilesY;
    /*
    Used for debugging purposes, this set of global variables is added to world state when it is loaded/created
     */
    public static Properties debugWorldVariables = null;
    private static World world;
    private static MainMenuController mainMenu;
    private static Camera camera;
    private static long lastFrameTime;
    private static AppGameContainer app;

    public AuroraGame() {
    }

    // returns a name of the directory where the game can store its data like saves
    // writing to installation directory is a bad idea if game is installed on UAC-protected disk
    public static File getOutDir() {
        File homeDir = new File(System.getProperty("user.home"));
        return new File(homeDir, "AuroraRL");
    }

    public static List<Resolution> getAvailableResolutions() {
        Set<Long> resolutionset = new HashSet<>();
        List<Resolution> result = new ArrayList<>();


        try {
            final DisplayMode[] availableDisplayModes = Display.getAvailableDisplayModes();
            logger.info("Available display modes: {}", Arrays.toString(availableDisplayModes));
            for (DisplayMode mode : availableDisplayModes) {
                if (!mode.isFullscreenCapable() || mode.getBitsPerPixel() < 24) {
                    continue;
                }
                if (mode.getWidth() < 1024) {
                    continue;
                }
                if (resolutionset.contains((long) mode.getWidth() * mode.getHeight())) {
                    continue;
                }


                resolutionset.add((long) mode.getWidth() * mode.getHeight());
                result.add(new Resolution(mode.getWidth(), mode.getHeight()));
            }
        } catch (LWJGLException e) {
            logger.error("Failed to get list of display modes", e);
            throw new RuntimeException(e);
        }
        if (result.isEmpty()) {
            logger.error("No suitable resolutions found");
        }
        Collections.sort(result);
        return result;
    }

    public static void changeResolution(Resolution res, boolean fullScreen) {
        tilesX = res.getTilesX();
        tilesY = res.getTilesY();
        Camera oldCam = camera;
        camera = new Camera(0, 0, tilesX, tilesY, tileSize, tileSize);
        camera.setTarget(oldCam.getTarget());

        try {
            app.setDisplayMode(res.getWidth(), res.getHeight(), fullScreen);
            GUI.getInstance().getNifty().resolutionChanged();
        } catch (SlickException e) {
            logger.error("Failed to change display mode", e);
        }
        for (ResolutionChangeListener listener : resolutionChangeListeners) {
            listener.onResolutionChanged(tilesX, tilesY, fullScreen);
        }
    }

    public static boolean isFullScreen() {
        return app.isFullscreen();
    }

    public static void setFullScreen(boolean fullScreen) {
        if (fullScreen == app.isFullscreen()) {
            return;
        }

        try {
            app.setFullscreen(fullScreen);
        } catch (SlickException e) {
            logger.error("Failed to set fullscreen to " + fullScreen, e);
        }
    }

    public static Set<Updatable> getUpdatables() {
        return updatables;
    }

    public static void onGameLoaded(World loaded) {
        if (world != null) {
            resolutionChangeListeners.remove(world);
        }
        world = loaded;
        resolutionChangeListeners.add(world);
        world.onResolutionChanged(tilesX, tilesY, isFullScreen());
        mainMenu = null;
        if (debugWorldVariables != null) {
            logger.warn("Adding debug variables");
            for (Map.Entry<Object, Object> e : debugWorldVariables.entrySet()) {
                world.getGlobalVariables().put((String) e.getKey(), (Serializable) e.getValue());
            }

        }

        GUI.getInstance().onWorldLoaded(app, world);
        world.getCurrentRoom().returnTo(world);
        world.setPaused(false);
    }

    public static boolean isGameRunning() {
        return world != null;
    }

    public static void goToMainMenu() {
        mainMenu = (MainMenuController) GUI.getInstance().getNifty().findScreenController(MainMenuController.class.getCanonicalName());
        mainMenu.reset();
        world = null;
        GUI.getInstance().setWorldInstance(null);
        GUI.getInstance().getNifty().gotoScreen("main_menu");
    }

    public static void showExitConfirmation(boolean goToMainMenu) {
        GUI.getInstance().pushCurrentScreen();
        ((ExitConfirmationScreenController) GUI.getInstance().getNifty().findScreenController(ExitConfirmationScreenController.class.getCanonicalName())).setGoToMainMenu(goToMainMenu);
        GUI.getInstance().getNifty().gotoScreen("exit_confirmation_screen");
    }

    public static void exitGame() {
        Configuration.saveSystemProperties();
        if (SteamAPI.isSteamRunning()) {
            AchievementManager.term();
            SteamAPI.shutdown();
        }
        app.exit();
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
            throw new IOException("Failed to get permissions to set library path", e);
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get field handle to set library path", e);
        }
    }

    public static void main(String[] args) throws SlickException, IOException {
        try {
            logger.info("Using Java " + System.getProperty("java.version") + " at " + System.getProperty("java.home"));
            logger.info("Aurora game version " + Version.VERSION + " started");
            // allow software rendering for support of older generation cards
            System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");

            final String osName = System.getProperty("os.name");

            if (args.length > 0) {
                if (args[0].equals("-debugVariables")) {
                    debugWorldVariables = new Properties();
                    debugWorldVariables.load(new FileInputStream(args[1]));
                }
            }

            if (args.length == 0 || !args[0].equals("-noSteam")) {
                if (!SteamAPI.init()) {
                    logger.error("Failed to initialize steam api");
                    return;
                } else {
                    logger.info("SteamAPI successfully initialized");
                }
            } else {
                logger.warn("This is a non-Steam build");
            }

            // read the steam id file
            InputStream is = AuroraGame.class.getClassLoader().getResourceAsStream("steam_appid.txt");
            if (is == null) {
                logger.error("Failed to find steam_appid.txt file");
                return;
            }
            String appId = IOUtils.toString(is);
            is.close();
            logger.info("Loaded steam appid " + app);
            AchievementManager.init(Integer.parseInt(appId.trim()));

            String nativePath;
            if (osName.contains("Windows")) {
                nativePath = "native/windows";
            } else if (osName.contains("Linux")) {
                nativePath = "native/linux";
            } else if (osName.contains("Mac")) {
                nativePath = "native/macosx";
            } else {
                logger.error("Unsupported os " + osName + ", lwjgl has no native libraries for it");
                throw new RuntimeException("Unsupported os " + osName + ", lwjgl has no native libraries for it");
            }
            File f = new File(nativePath);
            logger.info("Setting native lib dir to " + f.getAbsolutePath());
            addDir(nativePath);

            logger.info("Application class path is: " + System.getProperty("java.class.path"));

            SoundStore.get().init();
            final String locale = Configuration.getSystemProperties().getProperty("locale");
            if (locale != null) {
                Localization.init(Locale.forLanguageTag(locale));
            } else {
                Localization.init(Locale.getDefault());
            }
            Configuration.getSystemProperties().put("locale", Localization.getCurrentLocaleTag());

            // read input binding
            String savedInputBinding = Configuration.getSystemProperties().getProperty(InputBinding.key);
            if (savedInputBinding == null) {
                logger.info("No saved keyboard layout found, using default");
                InputBinding.setDefault();
            } else {
                logger.info("Loading keyboard layout");
                InputBinding.loadFromString(savedInputBinding);
            }
            app = new AppGameContainer(new AuroraGame());
            app.setUpdateOnlyWhenVisible(false);
            app.setClearEachFrame(true);
            Resolution res;
            String resolutionString = Configuration.getSystemProperties().getProperty("screen.resolution");
            if (resolutionString != null) {
                res = new Resolution(resolutionString);
            } else {
                DisplayMode desktopMode = Display.getDesktopDisplayMode();
                res = new Resolution(desktopMode.getWidth(), desktopMode.getHeight());
                if (Math.abs(1.0f - Display.getPixelScaleFactor()) > 0.00001) {
                    logger.warn("This display has a pixel scale factor of {}, resolution may be incorrect", Display.getPixelScaleFactor());
                }
            }

            final boolean fullScreen = Boolean.parseBoolean(Configuration.getSystemProperties().getProperty("screen.full_screen", "true"));
            app.setDisplayMode(res.getWidth(), res.getHeight(), fullScreen);
            camera = new Camera(0, 0, res.getTilesX(), res.getTilesY(), tileSize, tileSize);
            app.setIcons(new String[]{"sprites/icons/icon_32.png", "sprites/icons/icon_16.png"});
            tilesX = res.getTilesX();
            tilesY = res.getTilesY();

            SaveGameManager.init();
            ModManager.init();
            // make sure that system.properties appears even if game later crashes on startup
            Configuration.saveSystemProperties();

            app.start();
        } catch (Exception ex) {
            logger.error("Failed to init game: ", ex);
            throw ex;
        }
    }

    public static long getLastFrameTime() {
        return lastFrameTime;
    }

    public static Image takeScreenshot() throws SlickException, IOException {
        Image img = new Image(app.getWidth(), app.getHeight());
        world.draw(app, img.getGraphics());
        return img;
    }

    /**
     * Restart the current Java application
     *
     * @param runBeforeRestart some custom code to be run before restarting
     * @throws IOException
     */
    public static void restartApplication(Runnable runBeforeRestart) throws IOException {
        try {
            // java binary
            String java = System.getProperty("java.home") + "/bin/java";
            // vm arguments
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            StringBuffer vmArgsOneLine = new StringBuffer();
            for (String arg : vmArguments) {
                // if it's the agent argument : we ignore it otherwise the
                // address of the old application and the new one will be in conflict
                if (!arg.contains("-agentlib")) {
                    vmArgsOneLine.append(arg);
                    vmArgsOneLine.append(" ");
                }
            }
            // init the command to execute, add the vm args
            final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

            // program main and program arguments
            String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
            // program main is a jar
            if (mainCommand[0].endsWith(".jar")) {
                // if it's a jar, add -jar mainJar
                cmd.append("-jar " + new File(mainCommand[0]).getPath());
            } else {
                // else it's a .class, add the classpath and mainClass
                cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
            }
            // finally add program arguments
            for (int i = 1; i < mainCommand.length; i++) {
                cmd.append(" ");
                cmd.append(mainCommand[i]);
            }
            // execute the command in a shutdown hook, to be sure that all the
            // resources have been disposed before restarting the application
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec(cmd.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // execute some custom code before restarting
            if (runBeforeRestart != null) {
                runBeforeRestart.run();
            }
            if (SteamAPI.isSteamRunning()) {
                SteamAPI.shutdown();
            }
            // exit
            System.exit(0);
        } catch (Exception e) {
            // something went wrong
            throw new IOException("Error while trying to restart the application", e);
        }
    }

    public static InputStream getResourceAsStream(String resource) {
        InputStream rz = AuroraGame.class.getClassLoader().getResourceAsStream(resource);
        if (rz == null) {
            logger.info("Resource {} not found in main game, searching in mods", resource);
            rz = ModManager.getInstance().getResourceAsStream(resource);
            if (rz == null) {
                logger.warn("Resource {} not found in mods", resource);
            }
        }
        return rz;
    }

    @Override
    protected void initGameAndGUI(GameContainer gameContainer) throws SlickException {

        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        ResourceManager.getInstance().loadResources(AuroraGame.class.getClassLoader().getResourceAsStream("resources.xml"));
        gameContainer.getInput().enableKeyRepeat();
        gameContainer.setTargetFrameRate(60);

        initNifty(gameContainer);
        GUI.init(gameContainer, getNifty());
        GUI.getInstance().getNifty().gotoScreen("main_menu");
        mainMenu = (MainMenuController) GUI.getInstance().getNifty().findScreenController(MainMenuController.class.getCanonicalName());
        resolutionChangeListeners.add(mainMenu);
        try {
            AnimalGenerator.init();
        } catch (FileNotFoundException e) {
            throw new SlickException("Failed to initialize Monster Generator", e);
        }
        lastFrameTime = gameContainer.getTime();
        String musicVolumeString = Configuration.getSystemProperties().getProperty("music.volume");
        if (musicVolumeString != null) {
            float volume = Float.parseFloat(musicVolumeString);
            SoundStore.get().setMusicVolume(volume);
            SoundStore.get().setCurrentMusicVolume(volume);
        }
        String soundVolumeString = Configuration.getSystemProperties().getProperty("sound.volume");
        if (soundVolumeString != null) {
            SoundStore.get().setSoundVolume(Float.parseFloat(soundVolumeString));
        }
        ResourceManager.getInstance().getPlaylist("background").play();

    }

    @Override
    protected void prepareNifty(Nifty nifty) {
        nifty.loadStyleFile("gui/style/aurora-style.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
    }

    @Override
    protected void updateGame(GameContainer gameContainer, int i) throws SlickException {
        try {
            if (gameContainer.getInput().isKeyPressed(Input.KEY_F11)) {
                flipVisbility();
            }
            if (mainMenu != null) {
                World loadedWorld = mainMenu.update(camera, gameContainer);
                if (loadedWorld != null) {
                    onGameLoaded(loadedWorld);
                    world.onNewGameStarted();
                    // hack: show initial help on new game start
                    if (!world.getGlobalVariables().containsKey("tutorial.started")) {
                        HelpPopupControl.showHelp();
                    }
                }
            } else {
                world.update(gameContainer);
                if (world.isGameOver()) {
                    goToMainMenu();
                }
            }
            final List<Updatable> updatables1 = new ArrayList<>(updatables);
            for (Updatable up : updatables1) {
                up.update(gameContainer, world);
            }
            gameContainer.getInput().clearKeyPressedRecord();

            if (SteamAPI.isSteamRunning()) {
                SteamAPI.runCallbacks();
            }
        } catch (Exception ex) {
            logger.error("Exception in updateGame()", ex);
            throw ex;
        }
    }

    @Override
    public boolean closeRequested() {
        showExitConfirmation(false);
        return false;
    }

    @Override
    public String getTitle() {
        return "Aurora " + Version.VERSION;
    }

    @Override
    protected void renderGame(GameContainer gameContainer, Graphics graphics) throws SlickException {
        try {

            if (mainMenu != null) {
                mainMenu.draw(graphics);
            } else {
                world.draw(gameContainer, graphics);
            }

            lastFrameTime = gameContainer.getTime();
        } catch (Exception ex) {
            logger.error("Exception in renderGame()", ex);
            throw ex;
        }
    }
}

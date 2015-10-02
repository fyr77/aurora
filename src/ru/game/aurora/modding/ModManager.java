package ru.game.aurora.modding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.UTF8Control;
import ru.game.aurora.world.World;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Mod collection
 */
public class ModManager {

    private static final String MODS_DIR = "mods";

    private static final Logger logger = LoggerFactory.getLogger(ModManager.class);

    private static ModManager instance;

    private List<Mod> mods = new ArrayList<>();

    private ClassLoader rootModClassLoader;

    public static void init() {
        instance = new ModManager();
        instance.rootModClassLoader = new URLClassLoader(new URL[]{});
        File modsRoot = new File(MODS_DIR);
        if (!modsRoot.exists() || !modsRoot.isDirectory()) {
            logger.info("Mods directory does not exist, skipping mods loading");
            return;
        }
        logger.info("Loading mods...");
        for (File f : modsRoot.listFiles()) {
            if (f.isDirectory()) {
                logger.info("Loading mod from " + f);
                final Mod e = new Mod(f, instance.rootModClassLoader);
                instance.mods.add(e);
                if (!e.isLoaded()) {
                    logger.warn("Mod at {} failed to load", f);
                }
            }
        }
        logger.info("Done loading mods, {} mods total ", instance.mods.size());
    }

    public static ModManager getInstance() {
        return instance;
    }

    /**
     * If object was loaded by some mod - will return corresponding Mod
     *
     * @param object Any object that was loaded from this mod classpath
     */
    public static Mod getModForObject(Object object) {
        for (Mod m : instance.mods) {
            if (m.isLoaded() && m.loadedByThisMod(object)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Query mod configuration
     *
     * @param object Any object loaded from the mod classpath
     * @return Contents of mod.properties file of current mod, or null if object does not belong to a mod or this
     * mod has no default configuration file
     */
    public static Properties getModConfiguration(Object object) {
        Mod m = getModForObject(object);
        return m != null ? m.getModConfiguration() : null;
    }

    public InputStream getResourceAsStream(String resource) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                InputStream rz = m.getClassLoader().getResourceAsStream(resource);
                if (rz != null) {
                    return rz;
                }
            }
        }
        return null;
    }


    public void onNewGameStarted(World world) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                try {
                    m.getModClass().onNewGameStarted(world);
                } catch (Exception e) {
                    logger.error("Mod " + m.getManifest().modName + " raised Exception in onNewGameStarted()", e);
                    m.unload();
                }
            }
        }
    }

    public void onGameLoaded(World world) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                try {
                    m.getModClass().onGameLoaded(world);
                } catch (Exception e) {
                    logger.error("Mod " + m.getManifest().modName + " raised Exception in onGameLoaded()", e);
                    m.unload();
                }
            }
        }
    }

    public Class resolveClass(String className) {
        for (Mod m : mods) {
            if (m.isLoaded()) {
                try {
                    return m.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    // nothing
                }
            }
        }
        return null;
    }

    public ClassLoader getRootModClassLoader() {
        return rootModClassLoader;
    }

    public List<ResourceBundle> getResourceBundles(String bundleName, Locale currentLocale, UTF8Control utf8Control) {
        List<ResourceBundle> bundleList = new ArrayList<>(mods.size());
        for (Mod m : mods) {
            if (!m.isLoaded()) {
                continue;
            }
            ResourceBundle b = null;
            try {
                b = ResourceBundle.getBundle(bundleName, currentLocale, m.getClassLoader(), utf8Control);
            } catch (MissingResourceException ignore) {

            }
            if (b != null) {
                bundleList.add(b);
            }
        }
        return bundleList;
    }
}

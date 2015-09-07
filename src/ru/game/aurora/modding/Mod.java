package ru.game.aurora.modding;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Performs mod loading
 */
public class Mod {
    private static final String MANIFEST_NAME = "manifest.json";

    /**
     * Directory containing jar files with custom code
     * Slash at the end is required (?) by URLClassLoader
     */
    private static final String LIB_DIR = "lib/";

    /**
     * Directory that contains files that should override original game resources.
     * Should have same structure as original resources dir
     */
    private static final String OVERRIDES_DIR = "overrides/";

    /**
     * Directory with custom mod resources
     */
    private static final String RESOURCES_DIR = "resources/";

    private static final String CONFIGURATION_NAME = "mod.properties";

    private static final Logger logger = LoggerFactory.getLogger(Mod.class);
    /**
     * Mod information loaded from json
     */
    private ModManifest manifest;
    /**
     * Main mod object
     */
    private ModInterface modClass;
    /**
     * Class loader used to load mod jars
     */
    private ClassLoader classLoader;
    /**
     * Mod root directory
     */
    private File myRoot;

    private Properties modConfiguration;
    /**
     * Mod status
     */
    private boolean isLoaded = false;

    public Mod(File directory, ClassLoader rootModClassLoader) {
        load(directory, rootModClassLoader);
    }

    public ModManifest getManifest() {
        return manifest;
    }

    public ModInterface getModClass() {
        return modClass;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Loads a directory with a mod.
     */
    private void load(File directory, ClassLoader rootModClassLoader) {
        myRoot = directory;
        if (!myRoot.exists() || !myRoot.isDirectory()) {
            logger.error("Failed to load mod from {}, dir does not exist", directory);
            return;
        }

        File manifestFile = new File(myRoot, MANIFEST_NAME);
        if (!manifestFile.exists() || !manifestFile.isFile()) {
            logger.error("Manifest file does not exist at " + directory);
            return;
        }

        try (FileReader fr = new FileReader(manifestFile)) {
            manifest = new Gson().fromJson(fr, ModManifest.class);
        } catch (IOException e) {
            logger.error("Failed to read manifest file at " + directory, e);
            return;
        }

        List<URL> modClassPath = new ArrayList<>();
        try {
            // build a list of jars
            final File libDir = new File(myRoot, LIB_DIR);
            if (libDir.exists() && libDir.isDirectory()) {
                for (File f : libDir.listFiles()) {
                    if (f.isFile() && f.getName().endsWith(".jar")) {
                        modClassPath.add(f.toURI().toURL());
                    }
                }
            }
            modClassPath.add(new File(myRoot, OVERRIDES_DIR).toURL());
            modClassPath.add(new File(myRoot, RESOURCES_DIR).toURL());


            classLoader = new URLClassLoader(
                    modClassPath.toArray(new URL[modClassPath.size()])
                    , rootModClassLoader
            );
        } catch (MalformedURLException e) {
            logger.error("Failed to create classloader ", e);
            return;
        }

        // load mod.properties if it exists
        InputStream confStream = classLoader.getResourceAsStream(CONFIGURATION_NAME);
        if (confStream != null) {
            try {
                modConfiguration = new Properties();
                modConfiguration.load(confStream);
                confStream.close();
            } catch (IOException ex) {
                logger.error("Failed to load mod.properties file for mod at " + myRoot, ex);
                isLoaded = false;
                return;
            }
        }

        // load resource overrides
        File overridesDir = new File(myRoot, OVERRIDES_DIR);
        if (overridesDir.exists() && overridesDir.isDirectory()) {
            logger.info("Mod at {} has some resources to override", myRoot);
            //todo: add resource overriding
        }

        if (manifest.mainClassName == null) {
            logger.info("Mod at {} does not have a mainClassName property in manifest, probably a resource-only mod", myRoot);
            isLoaded = true;
            return;
        }

        try {
            Class mainClass = Class.forName(manifest.mainClassName, true, classLoader);
            if (!ModInterface.class.isAssignableFrom(mainClass)) {
                logger.error("Main class {} specified in mod at {} is not a subclass of ModInterface", manifest.mainClassName, myRoot);
                return;
            }

            modClass = (ModInterface) mainClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Failed to find main class for mod at " + myRoot, e);
            return;
        } catch (InstantiationException e) {
            logger.error("Failed to create main object for mod at " + myRoot, e);
            return;
        } catch (Exception e) {
            logger.error("Error initializing mod at " + myRoot, e);
            return;
        }

        try {
            modClass.onModLoaded();
        } catch (ModException e) {
            logger.error("Exception in onModLoaded() for mod at " + myRoot);
            return;
        }
        logger.info("Mod {} version {} successfully loaded", manifest.modName, manifest.modVersion);
        isLoaded = true;
    }

    public void unload() {
        isLoaded = false;
        classLoader = null;
    }

    /**
     * Checks if given object belongs to this mod (was loaded from this mod classpath)
     */
    public boolean loadedByThisMod(Object object) {
        return object.getClass().getClassLoader().equals(classLoader);
    }

    public Properties getModConfiguration() {
        return modConfiguration;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}

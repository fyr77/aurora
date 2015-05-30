package ru.game.aurora.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.10.13
 * Time: 15:11
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    /**
     * Properties of game world like galaxy size
     */
    private static Properties worldProperties;

    /**
     * System properties like screen resolution
     */
    private static Properties systemProperties;
    
    static {
        try {
            worldProperties = new Properties();
            InputStream is = new FileInputStream("resources/game.properties");
            worldProperties.load(is);
        } catch (IOException e) {
            logger.error("Failed to load game properties:", e);
            System.exit(0);
        }
        
        //the game can run without system.properties, 
            //so we won't terminate it if smth went wrong, but log an error
        
        systemProperties = new Properties();
        try {
            File systemPropsFile = new File("system.properties");
            if (systemPropsFile.exists()) {
                InputStream is = new FileInputStream(systemPropsFile);
                systemProperties.load(is);
            }
        } catch (IOException e) {
            logger.error("Failed to load game properties:", e);
        }
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(worldProperties.getProperty(key));
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(worldProperties.getProperty(key));
    }

    public static double getDoubleProperty(String key) {
        return Double.parseDouble(worldProperties.getProperty(key));
    }
    
    public static String getProperty(String key) {
        return worldProperties.getProperty(key);
    }

    public static Properties getSystemProperties() {
        return systemProperties;
    }

    public static void saveSystemProperties() {
        try (FileOutputStream fos = new FileOutputStream("system.properties")) {
            systemProperties.store(fos, null);
        } catch (IOException e) {
            logger.error("Failed to save system.properties", e);
        }
    }
}

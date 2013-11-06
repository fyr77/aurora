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

    public static void init() throws IOException {
        worldProperties = new Properties();
        try (InputStream is = new FileInputStream("resources/game.properties")) {
            worldProperties.load(is);
        }

        systemProperties = new Properties();
        File systemPropsFile = new File("system.properties");
        if (systemPropsFile.exists()) {
            try (InputStream is = new FileInputStream(systemPropsFile)) {
                systemProperties.load(is);
            }
        }
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(worldProperties.getProperty(key));
    }

    public static double getDoubleProperty(String key) {
        return Double.parseDouble(worldProperties.getProperty(key));
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

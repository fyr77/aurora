package ru.game.aurora.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.10.13
 * Time: 15:11
 */
public class Configuration
{
    private static Properties properties;

    public static void init() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream("resources/game.properties"));
    }

    public static int getIntProperty(String key)
    {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static double getDoubleProperty(String key)
    {
        return Double.parseDouble(properties.getProperty(key));
    }

    public static Properties getProperties()
    {
        return properties;
    }
}

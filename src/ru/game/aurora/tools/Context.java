package ru.game.aurora.tools;

import java.util.Properties;

/**
* Used in tools that parse CSV files
*/
public final class Context {
    public String id;

    public int lineNumber = 0;

    public Properties text = new Properties();

    public Context(String id) {
        this.id = id;
    }
}

package ru.game.aurora.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
* Used in tools that parse CSV files
*/
public final class Context<E>
{
    public final String id;

    public int lineNumber = 0;

    public final Properties text = new Properties();

    public final List<E> statements = new LinkedList<>();

    public Context(String id) {
        this.id = id;
    }
}

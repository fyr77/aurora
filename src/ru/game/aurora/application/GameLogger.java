/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import jgame.platform.JGEngine;

public class GameLogger
{

    private JGEngine engine;

    public GameLogger(JGEngine engine) {
        this.engine = engine;
    }

    private static GameLogger instance = null;

    public static void init(JGEngine engine)
    {
        instance = new GameLogger(engine);
    }

    public static GameLogger getInstance()
    {
        return instance;
    }

    public void logMessage(String message)
    {
        throw new UnsupportedOperationException();
    }
}

/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import jgame.JGColor;
import jgame.platform.JGEngine;

import java.util.LinkedList;
import java.util.List;

public class GameLogger
{

    private JGEngine engine;

    private static final int maxLogSize = 10;

    private List<String> messages = new LinkedList<String>();

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
        messages.add(message);
        if (messages.size() > maxLogSize) {
            messages.remove(0);
        }

    }

    public void draw(JGEngine engine)
    {
        engine.setColor(JGColor.white);
        int i = 0;
        for (String m : messages) {
            engine.drawString(m, 10, engine.getHeight() - 10 - 20 * i, -1);
            ++i;
        }
    }


}

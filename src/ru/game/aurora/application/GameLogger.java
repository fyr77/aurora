/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import jgame.JGColor;
import jgame.JGFont;
import jgame.JGRectangle;
import jgame.platform.JGEngine;

import java.util.LinkedList;
import java.util.List;

public class GameLogger {

    private static final int maxLogSize = 10;

    private List<String> messages = new LinkedList<String>();

    private List<String> statusMessages = new LinkedList<String>();

    private JGRectangle statusMessagesRect;

    private JGRectangle logRect;

    private static GameLogger instance = null;

    private JGFont font;

    public GameLogger(JGRectangle statusMessagesRect, JGRectangle logRect) {
        this.statusMessagesRect = statusMessagesRect;
        this.logRect = logRect;
        font = new JGFont("arial", 0, 15);
    }

    public static void init(JGRectangle statusMessagesRect, JGRectangle logRect) {
        instance = new GameLogger(statusMessagesRect, logRect);
    }

    public static GameLogger getInstance() {
        return instance;
    }

    public void logMessage(String message) {
        messages.add(message);
        if (messages.size() > maxLogSize) {
            messages.remove(0);
        }

    }

    public void addStatusMessage(String message) {
        statusMessages.add(message);
    }

    public void draw(JGEngine engine) {
        engine.setColor(JGColor.white);
        int i = 0;
        for (String m : messages) {
            engine.drawString(m, logRect.x, logRect.y + i * font.getSize(), -1, font, JGColor.white);
            ++i;
        }

        i = 0;
        for (String m : statusMessages) {
            engine.drawString(m, statusMessagesRect.x, statusMessagesRect.y + i * font.getSize(), -1, font, JGColor.white);
            ++i;
        }
    }

    public JGRectangle getStatusMessagesRect() {
        return statusMessagesRect;
    }

    public JGFont getFont() {
        return font;
    }

    public void clearStatusMessages() {
        statusMessages.clear();
    }
}

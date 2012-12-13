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

    public static final byte TEXT_OFFSET = 10;

    private static final JGColor backgroundColor = new JGColor(4, 7, 125);

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
        engine.setColor(backgroundColor);
        engine.drawRect(statusMessagesRect.x, statusMessagesRect.y, statusMessagesRect.width, statusMessagesRect.height, true, false);
        engine.drawRect(logRect.x, logRect.y, logRect.width, logRect.height, true, false);
        engine.setColor(JGColor.yellow);

        engine.drawRect(statusMessagesRect.x, statusMessagesRect.y, statusMessagesRect.width, statusMessagesRect.height, false, false);
        engine.drawRect(logRect.x, logRect.y, logRect.width, logRect.height, false, false);

        engine.setColor(JGColor.white);
        int i = 0;
        for (String m : messages) {
            engine.drawString(m, logRect.x + TEXT_OFFSET, logRect.y + TEXT_OFFSET + i * font.getSize(), -1, font, JGColor.white);
            ++i;
        }

        i = 0;
        for (String m : statusMessages) {
            engine.drawString(m, statusMessagesRect.x + TEXT_OFFSET, statusMessagesRect.y + TEXT_OFFSET + i * font.getSize(), -1, font, JGColor.white);
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

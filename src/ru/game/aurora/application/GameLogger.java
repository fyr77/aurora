/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 23:24
 */
package ru.game.aurora.application;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.util.EngineUtils;

import java.util.LinkedList;
import java.util.List;

public class GameLogger {

    private static final int maxLogSize = 10;

    private List<String> messages = new LinkedList<String>();

    private List<String> statusMessages = new LinkedList<String>();

    private Rectangle statusMessagesRect;

    private Rectangle logRect;

    private static GameLogger instance = null;

    private Font font;

    public static final byte TEXT_OFFSET = 10;

    public GameLogger(Rectangle statusMessagesRect, Rectangle logRect) {
        this.statusMessagesRect = statusMessagesRect;
        this.logRect = logRect;
        java.awt.Font f = new java.awt.Font("Arial", java.awt.Font.PLAIN, 18);
        font = new org.newdawn.slick.TrueTypeFont(f, false);
    }

    public static void init(Rectangle statusMessagesRect, Rectangle logRect) {
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

    public void draw(Graphics graphics) {
        graphics.setColor(GUIConstants.backgroundColor);
        graphics.fillRect(statusMessagesRect.getX(), statusMessagesRect.getY(), statusMessagesRect.getWidth(), statusMessagesRect.getHeight());
        graphics.fillRect(logRect.getX(), logRect.getY(), logRect.getWidth(), logRect.getHeight());
        graphics.setColor(Color.yellow);

        graphics.drawRect(statusMessagesRect.getX(), statusMessagesRect.getY(), statusMessagesRect.getWidth(), statusMessagesRect.getHeight());
        graphics.drawRect(logRect.getX(), logRect.getY(), logRect.getWidth(), logRect.getHeight());

        graphics.setColor(Color.white);
        graphics.setFont(font);
        int i = 0;
        for (String m : messages) {
            //graphics.drawString(m, logRect.getX() + TEXT_OFFSET, logRect.getY() + TEXT_OFFSET + i * font.getLineHeight());
            i += EngineUtils.drawString(graphics, m, (int)(logRect.getX() + TEXT_OFFSET), (int)(logRect.getY() + TEXT_OFFSET + i * font.getLineHeight()), (int) logRect.getWidth() - 2 * TEXT_OFFSET);
        }

        i = 0;
        for (String m : statusMessages) {
            i += EngineUtils.drawString(graphics, m, (int)(statusMessagesRect.getX() + TEXT_OFFSET), (int)(statusMessagesRect.getY() + TEXT_OFFSET + i * font.getLineHeight()), (int) statusMessagesRect.getWidth() - 2 * TEXT_OFFSET);
        }
    }

    public Rectangle getStatusMessagesRect() {
        return statusMessagesRect;
    }

    public Font getFont() {
        return font;
    }

    public void clearStatusMessages() {
        statusMessages.clear();
    }
}

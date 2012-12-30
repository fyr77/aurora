/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;


public class EngineUtils {

    public static void drawCircleCentered(Graphics graphics, int x, int y, int radius, Color c, boolean filled) {
        graphics.setColor(c);
        if (filled) {
            graphics.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        } else {
            graphics.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }
    }

    public static void drawRectWithBorder(Graphics graphics, Rectangle rectangle, Camera camera, Color borderColor, Color fillColor) {
        graphics.setColor(fillColor);
        drawRect(graphics, rectangle, camera, true);
        graphics.setColor(borderColor);
        drawRect(graphics, rectangle, camera, false);
    }

    /**
     * Draws a rect specified by tile coordinates
     */
    public static void drawRect(Graphics engine, Rectangle rectangle, Camera camera, boolean filled) {
        if (filled) {
            engine.fillRect(camera.getRelativeX((int) rectangle.getX()), camera.getRelativeY((int) rectangle.getY()), rectangle.getWidth() * camera.getTileWidth(), rectangle.getHeight() * camera.getTileHeight());
        } else {
            engine.drawRect(camera.getRelativeX((int) rectangle.getX()), camera.getRelativeY((int) rectangle.getY()), rectangle.getWidth() * camera.getTileWidth(), rectangle.getHeight() * camera.getTileHeight());
        }
    }

    public static int drawString(Graphics g, String s, int x, int y, int width) {
        return drawString(g, s, x, y, width, GameLogger.getInstance().getFont(), Color.white);
    }

    /**
     * Draws string at given coord, wrapping it if necessary (if it exceeds width pixels)
     * Returns nubmer of lines actually printed
     */
    public static int drawString(Graphics g, String s, int x, int y, int width, Font font, Color color) {

        int lineHeight = font.getLineHeight();

        int curX = x;
        int curY = y;

        String[] words = s.split(" ");
        int lines = 1;

        g.setFont(font);
        g.setColor(color);
        for (String word : words) {
            // Find out thw width of the word.
            int wordWidth = (word.length() + 1) * lineHeight / 2;

            // If text exceeds the width, then move to next line.
            if (word.contains("\n") || curX + wordWidth >= x + width) {
                curY += lineHeight;
                curX = x;
                lines++;
            }

            g.drawString(word, curX, curY);

            // Move over to the right for next word.
            curX += wordWidth + 1;
        }
        return lines;
    }

}

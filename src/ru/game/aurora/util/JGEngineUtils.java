/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import jgame.JGColor;
import jgame.JGFont;
import jgame.JGRectangle;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;


public class JGEngineUtils {

    public static void drawRectWithBorder(JGEngine engine, JGRectangle rectangle, Camera camera, JGColor borderColor, JGColor fillColor) {
        engine.setColor(fillColor);
        drawRect(engine, rectangle, camera, true);
        engine.setColor(borderColor);
        drawRect(engine, rectangle, camera, false);
    }

    /**
     * Draws a rect specified by tile coordinates
     */
    public static void drawRect(JGEngine engine, JGRectangle rectangle, Camera camera, boolean filled) {
        engine.drawRect(camera.getRelativeX(rectangle.x), camera.getRelativeY(rectangle.y), rectangle.width * camera.getTileWidth(), rectangle.height * camera.getTileHeight(), filled, false);
    }

    public static void drawString(JGEngine g, String s, int x, int y, int width) {
        drawString(g, s, x, y, width, GameLogger.getInstance().getFont(), JGColor.white);
    }

    /**
     * Draws string at given coord, wrapping it if necessary (if it exceeds width pixels)
     */
    public static void drawString(JGEngine g, String s, int x, int y, int width, JGFont font, JGColor color) {

        int lineHeight = font.getSize();

        int curX = x;
        int curY = y;

        String[] words = s.split(" ");

        for (String word : words) {
            // Find out thw width of the word.
            int wordWidth = (word.length() + 1) * lineHeight / 2;

            // If text exceeds the width, then move to next line.
            if (curX + wordWidth >= x + width) {
                curY += lineHeight;
                curX = x;
            }

            g.drawString(word, curX, curY, -1, font, color);

            // Move over to the right for next word.
            curX += wordWidth;
        }
    }

}

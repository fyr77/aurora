/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.application.GameLogger;


public class JGEngineUtils {

    /**
     * Draws string at given coord, wrapping it if necessary (if it exceeds width pixels)
     */
    public static void drawString(JGEngine g, String s, int x, int y, int width) {

        int lineHeight = GameLogger.getInstance().getFont().getSize();

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

            g.drawString(word, curX, curY, -1, GameLogger.getInstance().getFont(), JGColor.white);

            // Move over to the right for next word.
            curX += wordWidth;
        }
    }

}

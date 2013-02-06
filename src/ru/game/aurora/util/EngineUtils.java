/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
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

    public static void drawRectWithBorderAndText(Graphics graphics, Rectangle rectangle, Camera camera, String text) {
        drawRectWithBorderAndText(graphics, rectangle, camera, Color.yellow, GUIConstants.backgroundColor, text, GUIConstants.dialogFont, Color.white);
    }

    public static void drawRectWithBorderAndText(Graphics graphics, Rectangle rectangle, Camera camera, Color borderColor, Color fillColor, String text, Font font, Color textColor) {
        drawRectWithBorder(graphics, rectangle, camera, borderColor, fillColor);
        drawString(graphics, text, (int) ((rectangle.getX() + 1) * camera.getTileWidth()), (int) ((rectangle.getY() + 0.5) * camera.getTileHeight()), (int) (rectangle.getWidth() - 2) * camera.getTileHeight(), font, textColor);
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
        final int spaceWidth = font.getWidth(" ");
        for (String word : words) {
            // Find out thw width of the word.
            int wordWidth = font.getWidth(word);

            // If text exceeds the width, then move to next line.
            if (curX + wordWidth >= x + width) {
                curY += lineHeight;
                curX = x;
                lines++;
            }

            g.drawString(word, curX, curY);

            // Move over to the right for next word.
            curX += wordWidth + spaceWidth;

            if (word.contains("\n")) {
                curY += lineHeight;
                curX = x;
                lines++;
            }
        }
        return lines;
    }

    public static boolean checkRectanglePressed(GameContainer container, Camera camera, Rectangle rect)
    {
        if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            final int mouseX = container.getInput().getMouseX() / camera.getTileWidth();
            final int mouseY = container.getInput().getMouseY() / camera.getTileHeight();
            return rect.contains(mouseX, mouseY);
        }
        return false;
    }

}

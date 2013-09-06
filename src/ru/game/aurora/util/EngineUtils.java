/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.GUI;

import java.awt.image.BufferedImage;
import java.util.Map;


public class EngineUtils {

    public static void setImageForGUIElement(Element element, String imageName)
    {
        setImageForGUIElement(element, ResourceManager.getInstance().getImage(imageName));
    }

    public static void setImageForGUIElement(Element element, Image image)
    {
        element.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(image)));
    }

    public static void setTextForGUIElement(Element element, String text)
    {
        element.getRenderer(TextRenderer.class).setText(text);
    }

    /**
     * Returns a lighter version of a color
     */
    public static Color lightenColor(Color color) {
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[1] *= 0.3;
        hsb[2] = (float) Math.min(1.0, hsb[2] * 1.25);
        int rgb = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((rgb & 0x00ff0000) >> 16, (rgb & 0x0000ff00) >> 8, (rgb & 0x000000ff));
    }

    public static Color darkenColor(Color color, float coeff) {
        return new Color(color.r * coeff, color.g * coeff, color.b * coeff);
    }

    public static void drawCircleCentered(Graphics graphics, float x, float y, int radius, Color c, boolean filled) {
        graphics.setColor(c);
        if (filled) {
            graphics.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        } else {
            graphics.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }
    }

    public static void drawDashedCircleCentered(Graphics graphics, float x, float y, int radius, Color c) {
        graphics.setColor(c);
        final int segments = radius / 10;
        double angle = Math.PI / (2 * segments);
        boolean b = true;
        for (int i = 0; i <= segments; ++i) {

            int x1 = (int) (radius * Math.cos(angle * i));
            int x2 = (int)(radius *  Math.cos(angle * (i + 1)));

            int y1 = (int)(radius *  Math.sin(angle * i));
            int y2 = (int)(radius *  Math.sin(angle * (i + 1)));

            if (b) {
                graphics.drawLine(x + x1, y + y1, x + x2, y + y2);
                graphics.drawLine(x - x1, y - y1, x - x2, y - y2);
            } else {
                graphics.drawLine(x - x1, y + y1, x - x2, y + y2);
                graphics.drawLine(x + x1, y - y1, x + x2, y - y2);
            }

            b = !b;
        }

    }

    public static Image replaceColors(Image original, Map<Color, Color> colorMap)
    {
        ImageBuffer ib = new ImageBuffer(original.getWidth(), original.getHeight());
        Color searchKey = new Color(255, 255, 255, 255);
        for (int x = 0; x < original.getWidth(); ++x) {
            for (int y = 0; y < original.getHeight(); ++y) {
                org.newdawn.slick.Color c = original.getColor(x, y);
                searchKey.r = c.r;
                searchKey.g = c.g;
                searchKey.b = c.b;

                Color newColor = colorMap.get(searchKey);
                if (newColor != null) {
                    ib.setRGBA(x, y, newColor.getRed(), newColor.getGreen(), newColor.getBlue(), c.getAlpha());
                } else {
                    ib.setRGBA(x, y, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
                }

            }
        }
        return new Image(ib);
    }

    public static void drawRectWithBorder(Graphics graphics, Rectangle rectangle, Camera camera, Color borderColor, Color fillColor) {
        graphics.setColor(fillColor);
        drawRect(graphics, rectangle, camera, true);
        graphics.setColor(borderColor);
        drawRect(graphics, rectangle, camera, false);
    }

    public static void drawRectWithBorderAndText(Graphics graphics, Rectangle rectangle, Camera camera, String text) {
        drawRectWithBorderAndText(graphics, rectangle, camera, Color.yellow, GUIConstants.backgroundColor, text, GUIConstants.dialogFont, Color.white, false);
    }

    public static void drawRectWithBorderAndText(Graphics graphics, Rectangle rectangle, Camera camera, Color borderColor, Color fillColor, String text, Font font, Color textColor, boolean aligned) {
        drawRectWithBorder(graphics, rectangle, camera, borderColor, fillColor);
        if (!aligned) {
            drawString(graphics, text, ((rectangle.getX() + 1) * camera.getTileWidth()),  ((rectangle.getY() + 0.5f) * camera.getTileHeight()), ((rectangle.getWidth() - 2) * camera.getTileHeight()), font, textColor);
        } else {
            drawSingleStringAligned(graphics, text, font, textColor, (int)((rectangle.getX()) * camera.getTileWidth()), (int)((rectangle.getY()) * camera.getTileHeight()), (int) ((int)(rectangle.getWidth()) * camera.getTileWidth()), (int)(rectangle.getHeight() * camera.getTileHeight()));
        }
    }

    /**
     * Draws a rect specified by tile coordinates
     */
    public static void drawRect(Graphics engine, Rectangle rectangle, Camera camera, boolean filled) {
        if (filled) {
            engine.fillRect(camera.getRelativeX(rectangle.getX()), camera.getRelativeY(rectangle.getY()), rectangle.getWidth() * camera.getTileWidth(), rectangle.getHeight() * camera.getTileHeight());
        } else {
            engine.drawRect(camera.getRelativeX(rectangle.getX()), camera.getRelativeY(rectangle.getY()), rectangle.getWidth() * camera.getTileWidth(), rectangle.getHeight() * camera.getTileHeight());
        }
    }


    /**
     * Draws string at given coord, wrapping it if necessary (if it exceeds width pixels)
     * Returns nubmer of lines actually printed
     */
    public static int drawString(Graphics g, String s, float x, float y, float width, Font font, Color color) {

        int lineHeight = font.getLineHeight();

        float curX = x;
        float curY = y;

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

    public static void drawSingleStringAligned(Graphics g, String s, Font font, Color color, int x, int y, int width, int height) {
        final int lineHeight = font.getLineHeight();
        final int lineWidth = font.getWidth(s);

        final int textStartX = x + (width - lineWidth) / 2;
        final int textStartY = y + (height + lineHeight) / 2;

        g.setFont(font);
        g.setColor(color);
        g.drawString(s, textStartX, textStartY);
    }

    public static boolean checkRectanglePressed(GameContainer container, Camera camera, Rectangle rect) {
        if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            final float mouseX = container.getInput().getMouseX() / camera.getTileWidth();
            final float mouseY = container.getInput().getMouseY() / camera.getTileHeight();
            return rect.contains(mouseX, mouseY);
        }
        return false;
    }

    public static Image createImage(BufferedImage source) {
        ImageBuffer ib = new ImageBuffer(source.getWidth(), source.getHeight());
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                int rgb = source.getRGB(x, y);
                ib.setRGBA(x, y, 0x000000ff & (rgb >> 16), 0x000000ff & (rgb >> 8), 0x000000ff & rgb, 0x000000ff & (rgb >> 24));
            }
        }
        return new Image(ib);
    }

}

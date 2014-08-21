/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 19:11
 */
package ru.game.aurora.util;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.scrollbar.ScrollbarControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import org.mozilla.universalchardet.UniversalDetector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.GUI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class EngineUtils {

    public static void setImageForGUIElement(Element element, String imageName) {
        setImageForGUIElement(element, ResourceManager.getInstance().getImage(imageName));
    }

    public static void setImageForGUIElement(Element element, Image image) {
        element.getRenderer(ImageRenderer.class).setImage(image != null ? new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(image)) : null);
    }

    public static void setTextForGUIElement(Element element, String text) {
        element.getRenderer(TextRenderer.class).setText(text);
    }

    public static int wrap(int coord, int limit) {
        while (coord < 0 || coord >= limit) {
            if (coord < 0) {
                coord = limit + coord;
            } else if (coord >= limit) {
                coord = coord - limit;
            }
        }
        return coord;
    }

    public static String detectEncoding(String file) throws IOException {
        return detectEncoding(new File(file));
    }

    public static String detectEncoding(File file) throws IOException {
        // try detect file encoding first
        UniversalDetector detector = new UniversalDetector(null);

        InputStream fis = new FileInputStream(file);
        byte[] buf = new byte[4096];

        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }
        fis.close();
        return encoding;
    }


    /**
     * Returns a lighter version of a color
     */
    public static Color lightenColor(Color color) {
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[0] += 0.05 - (Math.random() / 10);
        if (hsb[0] > 1) {
            hsb[0] -= 1;
        } else if (hsb[0] < 0) {
            hsb[0] += 1;
        }
        hsb[2] = (float) Math.min(1.0, hsb[2] * 1.25);
        int rgb = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color(rgb);
    }

    public static Color darkenColor(Color color, float coeff) {
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[1] *= (coeff * 1.1);
        hsb[2] *= coeff;
        int rgb = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color(rgb);
    }

    public static void drawTileCircleCentered(Graphics g, Camera camera, int range) {
        float w = camera.getTileWidth();
        float h = camera.getTileHeight();
        int vx = camera.getViewportX();
        int vy = camera.getViewportY();
        float cx = camera.getNumTilesX() / 2;
        float cy = camera.getNumTilesY() / 2;
        for (int y = -range; y < range + 1; y++) {
            for (int x = -range; x < range + 1; x++) {
                if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) <= range) {
                    if (Math.sqrt(Math.pow(x - 1, 2) + Math.pow(y, 2)) > range) {
                        g.drawLine((cx + x) * w + vx,
                                (cy + y) * h + vy,
                                (cx + x) * w + vx,
                                (cy + y + 1) * h + vy);
                    }
                    if (Math.sqrt(Math.pow(x + 1, 2) + Math.pow(y, 2)) > range) {
                        g.drawLine((cx + x + 1) * w + vx,
                                (cy + y) * h + vy,
                                (cx + x + 1) * w + vx,
                                (cy + y + 1) * h + vy);
                    }
                    if (Math.sqrt(Math.pow(x, 2) + Math.pow(y - 1, 2)) > range) {
                        g.drawLine((cx + x) * w + vx,
                                (cy + y) * h + vy,
                                (cx + x + 1) * w + vx,
                                (cy + y) * h + vy);
                    }
                    if (Math.sqrt(Math.pow(x, 2) + Math.pow(y + 1, 2)) > range) {
                        g.drawLine((cx + x) * w + vx,
                                (cy + y + 1) * h + vy,
                                (cx + x + 1) * w + vx,
                                (cy + y + 1) * h + vy);
                    }
                }
            }
        }
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
        int segments = radius / 5;
        if (segments % 2 == 1) {
            segments++;
        }
        drawDashedCircleCentered(graphics, x, y, radius, c, segments);
    }

    public static void drawDashedCircleCentered(Graphics graphics, float x, float y, int radius, Color c, int segments) {
        graphics.setColor(c);
        float angle = 360 / (float) segments;
        boolean b = true;
        for (int i = 0; i < segments; ++i) {
            if (b) {
                graphics.drawArc(x - radius, y - radius, radius * 2, radius * 2, angle * i, angle * (i + 1));
            }
            b = !b;
        }
    }

    public static Image replaceColors(Image original, Map<Color, Color> colorMap) {
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

    public static BufferedImage convertToBufferedImage(Image im) {
        final int width = im.getWidth();
        final int height = im.getHeight();

        BufferedImage rz = new BufferedImage(width, im.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Color c = im.getColor(x, y);

                rz.setRGB(x, y, (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue());
            }
        }

        return rz;
    }

    public static void resetScrollbarX(ListBox listBox) {
        ScrollbarControl sc = listBox.getElement().findControl("#horizontal-scrollbar", ScrollbarControl.class);
        if (sc != null) {
            sc.setValue(0);
        }
    }
}

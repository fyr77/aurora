package ru.game.aurora.world.planet.nature;

import com.google.gson.Gson;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates random alien animals
 */
public class AnimalGenerator {
    // three main colors used in animal part sprites, will be replaced with specific color after sprite generation
    private static final Color LIGHT_COLOR = new Color(0x00bdd064);
    private static final Color MAIN_COLOR = new Color(0x00697436);
    private static final Color SHADOW_COLOR = new Color(0x00474e24);
    private static final Color DARK_SHADOW_COLOR = new Color(0x0033381a);

    private final Color[] allowedColors = {MAIN_COLOR, new Color(0x00a12e00), new Color(0x00ad5400), new Color(0x005f4d96)};

    private Map<AnimalPart.PartType, Collection<AnimalPart>> parts = new HashMap<>();

    // image where all assembling of monster is made
    private Image canvas;

    private final int CANVAS_SIZE = 400;

    private Graphics canvasGraphics;

    // how many parts of type BODY animal can contain, to prevent infinite generation
    private static final int BODY_LIMIT = 3;


    public AnimalGenerator() throws SlickException {
        canvas = new Image(CANVAS_SIZE, CANVAS_SIZE);
        canvasGraphics = canvas.getGraphics();
        readAllParts(new File("resources/animal_parts"));
    }

    private void readAllParts(File rootDir) {
        Gson gson = new Gson();
        for (AnimalPart.PartType p : AnimalPart.PartType.values()) {
            parts.put(p, new ArrayList<AnimalPart>());
        }
        for (File f : rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        })) {
            try (FileReader fr = new FileReader(f)) {
                AnimalPart part = gson.fromJson(fr, AnimalPart.class);
                part.loadImage();
                parts.get(part.partType).add(part);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPartToCanvas(Rectangle cropRect, BasePositionable anchor, AnimalPart.AttachmentPoint point, AnimalPart part) {
        int x = anchor.getX() + point.x - part.centerX;
        int y = anchor.getY() + point.y - part.centerY;

        part.image.setRotation(point.angle);

        canvasGraphics.drawImage(part.image, x, y);

        Rectangle AABB = getRotatedRectangleAABB(x + part.centerX, y + part.centerY, x, y, x + part.image.getWidth(), y + part.image.getHeight(), (float) Math.toRadians(point.angle));


        float cropX = Math.min(cropRect.getX(), AABB.getX());
        float cropY = Math.min(cropRect.getY(), AABB.getY());
        cropRect.setBounds(cropX, cropY, Math.max(cropRect.getX() + cropRect.getWidth(), AABB.getX() + AABB.getWidth()) - cropX, Math.max(cropRect.getY() + cropRect.getHeight(), AABB.getY() + AABB.getHeight()) - cropY);
    }

    private Rectangle getRotatedRectangleAABB(int centerX, int centerY, int x1, int y1, int x2, int y2, float angleRadians) {
        double x1prim = (x1 - centerX) * Math.cos(angleRadians) - (y1 - centerY) * Math.sin(angleRadians);
        double y1prim = (x1 - centerX) * Math.sin(angleRadians) + (y1 - centerY) * Math.cos(angleRadians);

        double x12prim = (x1 - centerX) * Math.cos(angleRadians) - (y2 - centerY) * Math.sin(angleRadians);
        double y12prim = (x1 - centerX) * Math.sin(angleRadians) + (y2 - centerY) * Math.cos(angleRadians);

        double x2prim = (x2 - centerX) * Math.cos(angleRadians) - (y2 - centerY) * Math.sin(angleRadians);
        double y2prim = (x2 - centerX) * Math.sin(angleRadians) + (y2 - centerY) * Math.cos(angleRadians);

        double x21prim = (x2 - centerX) * Math.cos(angleRadians) - (y1 - centerY) * Math.sin(angleRadians);
        double y21prim = (x2 - centerX) * Math.sin(angleRadians) + (y1 - centerY) * Math.cos(angleRadians);

        double rx1 = centerX + Math.min(Math.min(x1prim, x2prim), Math.min(x12prim, x21prim));
        double ry1 = centerY + Math.min(Math.min(y1prim, y2prim), Math.min(y12prim, y21prim));

        double rx2 = centerX + Math.max(Math.max(x1prim, x2prim), Math.max(x12prim, x21prim));
        double ry2 = centerY + Math.max(Math.max(y1prim, y2prim), Math.max(y12prim, y21prim));

        return new Rectangle((float) rx1, (float) ry1, (float) (rx2 - rx1), (float) (ry2 - ry1));
    }

    private int processPart(Rectangle cropRect, BasePositionable root, AnimalPart part, int bodyCount) {
        for (AnimalPart.AttachmentPoint ap : part.attachmentPoints) {
            AnimalPart.PartType type = CollectionUtils.selectRandomElement(ap.availableParts);
            if (type == AnimalPart.PartType.BODY && bodyCount >= BODY_LIMIT) {
                for (AnimalPart.PartType pt : ap.availableParts) {
                    if (pt != AnimalPart.PartType.BODY) {
                        type = pt;
                        break;
                    }
                }
            }

            AnimalPart newPart = CollectionUtils.selectRandomElement(parts.get(type));

            addPartToCanvas(cropRect, root, ap, newPart);

            bodyCount += processPart(cropRect, new BasePositionable(ap.x, ap.y), newPart, bodyCount);
        }
        return bodyCount;
    }

    private Image colorise(Image source, Color newMainColor) {
        Color newShadowColor = EngineUtils.darkenColor(newMainColor, 0.8f);
        Color newDarkShadowColor = EngineUtils.darkenColor(newMainColor, 0.5f);
        Color newBrightColor = EngineUtils.lightenColor(newMainColor);

        ImageBuffer ib = new ImageBuffer(source.getWidth(), source.getHeight());
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                Color c = source.getColor(x, y);
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                if (c.equals(MAIN_COLOR)) {
                    r = newMainColor.getRed();
                    g = newMainColor.getGreen();
                    b = newMainColor.getBlue();
                } else if (c.equals(SHADOW_COLOR)) {
                    r = newShadowColor.getRed();
                    g = newShadowColor.getGreen();
                    b = newShadowColor.getBlue();
                } else if (c.equals(LIGHT_COLOR)) {
                    r = newBrightColor.getRed();
                    g = newBrightColor.getGreen();
                    b = newBrightColor.getBlue();
                } else if (c.equals(DARK_SHADOW_COLOR)) {
                    r = newDarkShadowColor.getRed();
                    g = newDarkShadowColor.getGreen();
                    b = newDarkShadowColor.getBlue();
                }

                ib.setRGBA(x, y, r, g, b, c.getAlpha());
            }
        }
        return new Image(ib);
    }


    private Image createImageForAnimal(AnimalSpeciesDesc desc) {
        canvasGraphics.clear();

        // first select main body
        AnimalPart part = CollectionUtils.selectRandomElement(parts.get(AnimalPart.PartType.BODY));
        canvasGraphics.drawImage(part.image, CANVAS_SIZE / 2, CANVAS_SIZE / 2);
        Rectangle cropRect = new Rectangle(CANVAS_SIZE / 2, CANVAS_SIZE / 2, part.image.getWidth(), part.image.getHeight());
        // now select limbs and other parts
        processPart(cropRect, new BasePositionable(CANVAS_SIZE / 2, CANVAS_SIZE / 2), part, 1);
        return colorise(canvas.getSubImage((int) cropRect.getX(), (int) cropRect.getY(), (int) cropRect.getWidth(), (int) cropRect.getHeight()), CollectionUtils.selectRandomElement(allowedColors));
    }

    public Image getImageForAnimal(AnimalSpeciesDesc desc) {

        return createImageForAnimal(desc);
    }

}

package ru.game.aurora.world.planet.nature;

import com.google.gson.Gson;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.util.CollectionUtils;
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
        int cropX = (int) Math.min(cropRect.getX(), x);
        int cropY = (int) Math.min(cropRect.getY(), y);

        int maxWidthHeight = Math.max(part.image.getHeight(), part.image.getWidth());
        int cropWidth = (int) Math.max(cropRect.getWidth(), x + part.centerX + maxWidthHeight - cropX);
        int cropHeight = (int) Math.max(cropRect.getHeight(), y + part.centerY + maxWidthHeight - cropY);

        cropRect.setBounds(cropX, cropY, cropWidth, cropHeight);
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

    private Image createImageForAnimal(AnimalSpeciesDesc desc) {
        canvasGraphics.clear();

        // first select main body
        AnimalPart part = CollectionUtils.selectRandomElement(parts.get(AnimalPart.PartType.BODY));
        canvasGraphics.drawImage(part.image, CANVAS_SIZE / 2, CANVAS_SIZE / 2);
        Rectangle cropRect = new Rectangle(CANVAS_SIZE / 2, CANVAS_SIZE / 2, part.image.getWidth(), part.image.getHeight());
        // now select limbs and other parts
        processPart(cropRect, new BasePositionable(CANVAS_SIZE / 2, CANVAS_SIZE / 2), part, 1);

        return canvas.getSubImage((int) cropRect.getX(), (int) cropRect.getY(), (int) cropRect.getWidth(), (int) cropRect.getHeight());
    }

    public Image getImageForAnimal(AnimalSpeciesDesc desc) {

        return createImageForAnimal(desc);
    }

}

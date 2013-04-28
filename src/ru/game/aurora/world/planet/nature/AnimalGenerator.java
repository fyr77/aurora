package ru.game.aurora.world.planet.nature;

import com.google.gson.Gson;
import org.newdawn.slick.Image;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.BasePositionable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
    private BufferedImage canvas;

    private final int CANVAS_SIZE = 400;

    private Graphics2D canvasGraphics;

    // how many parts of type BODY animal can contain, to prevent infinite generation
    private static final int BODY_LIMIT = 3;


    public AnimalGenerator() {
        canvas = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        canvasGraphics = canvas.createGraphics();
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
                parts.get(part.partType).add(part);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPartToCanvas(BasePositionable anchor, AnimalPart.AttachmentPoint point, AnimalPart part) {
        int x = anchor.getX() + point.x;
        int y = anchor.getY() + point.y;

        AffineTransform tForm = new AffineTransform();
        tForm.rotate(Math.toRadians(point.angle));
        tForm.translate(x, y);
        canvasGraphics.drawImage(part.image, tForm, null);
    }

    private int processPart(BasePositionable root, AnimalPart part, int bodyCount) {
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

            addPartToCanvas(root, ap, newPart);

            bodyCount += processPart(new BasePositionable(ap.x, ap.y), newPart, bodyCount);
        }
        return bodyCount;
    }

    public Image getImageForAnimal(AnimalSpeciesDesc desc) {
        canvasGraphics.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        // first select main body
        AnimalPart part = CollectionUtils.selectRandomElement(parts.get(AnimalPart.PartType.BODY));
        canvasGraphics.drawImage(part.image, CANVAS_SIZE / 2, CANVAS_SIZE / 2, null);

        // now select limbs and other parts
        processPart(new BasePositionable(CANVAS_SIZE / 2, CANVAS_SIZE / 2), part, 1);


        return null;
    }

}

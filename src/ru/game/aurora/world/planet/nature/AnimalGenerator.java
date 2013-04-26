package ru.game.aurora.world.planet.nature;

import com.google.gson.Gson;
import org.newdawn.slick.Image;
import ru.game.aurora.world.BasePositionable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates random alien animals
 */
public class AnimalGenerator
{
    private Map<AnimalPart.PartType, AnimalPart> parts = new HashMap<>();

    // image where all assembling of monster is made
    private BufferedImage canvas;

    private Graphics canvasGraphics;

    // how many parts of type BODY animal can contain, to prevent infinite generation
    private static final int BODY_LIMIT = 3;


    public AnimalGenerator()
    {
        canvas = new BufferedImage(800, 800, BufferedImage.TYPE_4BYTE_ABGR);
        canvasGraphics = canvas.createGraphics();
        readAllParts(new File("resources/animal_parts"));
    }

    private void readAllParts(File rootDir)
    {
        Gson gson = new Gson();

        for (File f : rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        })) {
            try (FileReader fr = new FileReader(f)) {
                AnimalPart part = gson.fromJson(fr, AnimalPart.class);
                parts.put(part.partType, part);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPartToCanvas(BasePositionable anchor, AnimalPart.AttachmentPoint point, AnimalPart part)
    {

    }

    public Image getImageForAnimal(AnimalSpeciesDesc desc)
    {

        // first select main body
        return null;
    }

}

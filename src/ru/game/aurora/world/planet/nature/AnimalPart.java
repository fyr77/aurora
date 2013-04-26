package ru.game.aurora.world.planet.nature;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Part of alien animals. Animals are created combining randomly these parts
 */
public class AnimalPart
{
    public static enum PartType
    {
        BODY,
        LIMB,
        DECORATION
    }

    /**
     * Point of attach for limb parts. Each body part has several attachement points for connection with other body parts
     */
    public static class AttachmentPoint
    {
        public final int x;
        public final int y;
        public final int angle;
        public final PartType[] availableParts;

        public AttachmentPoint(int x, int y, int angle, PartType[] availableParts) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.availableParts = availableParts;
        }
    }

    public PartType partType;

    public AttachmentPoint[] attachmentPoints;

    public String imagePath;

    public transient BufferedImage image;

    public AnimalPart(PartType partType, AttachmentPoint[] attachmentPoints, String imagePath) {
        this.partType = partType;
        this.attachmentPoints = attachmentPoints;
        this.imagePath = imagePath;
        try {
            this.image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

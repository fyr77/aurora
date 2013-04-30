package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

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

    public int centerX;

    public int centerY;

    public transient Image image;

    public AnimalPart(PartType partType, AttachmentPoint[] attachmentPoints, String imagePath) {
        this.partType = partType;
        this.attachmentPoints = attachmentPoints;
        this.imagePath = imagePath;
        loadImage();
    }

    public void loadImage()
    {
        if (image == null) {
            try {
                this.image = new Image(imagePath);
            } catch (SlickException e) {
                e.printStackTrace();
            }
        }
    }
}

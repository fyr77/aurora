package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Part of alien animals. Animals are created combining randomly these parts
 */
public class AnimalPart {
    public static enum PartType {
        BODY,
        LIMB,
        DECORATION
    }

    /**
     * Point of attach for limb parts. Each body part has several attachement points for connection with other body parts
     */
    public static class AttachmentPoint {
        public final int x;
        public final int y;
        public final int angle;
        public final PartType[] availableParts;
        // Same parts will be attached to all points sharing same groupId
        public final String groupId;
        public final boolean flipHorizontal;
        public final boolean flipVertical;

        public AttachmentPoint(int angle, int x, int y, PartType[] availableParts, String groupId, boolean flipHorizontal, boolean flipVertical) {
            this.angle = angle;
            this.x = x;
            this.y = y;
            this.availableParts = availableParts;
            this.groupId = groupId;
            this.flipHorizontal = flipHorizontal;
            this.flipVertical = flipVertical;
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

    public void loadImage() {
        if (image == null) {
            try {
                this.image = new Image(imagePath, Image.FILTER_NEAREST);
                this.image.setCenterOfRotation(centerX, centerY);
            } catch (SlickException e) {
                e.printStackTrace();
            }
        }
    }
}

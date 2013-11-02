/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 09.08.13
 * Time: 17:07
 */
package ru.game.aurora.frankenstein;


import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.frankenstein.FrankensteinImage;
import ru.game.frankenstein.util.Rectangle;
import ru.game.frankenstein.util.Size;

import java.awt.*;
import java.util.Map;

public class Slick2DFrankensteinImage implements FrankensteinImage {
    private Image myImage;

    private static final Logger logger = LoggerFactory.getLogger(Slick2DFrankensteinImage.class);

    public Slick2DFrankensteinImage(Image myImage) {
        this.myImage = myImage;
    }

    @Override
    public int getWidth() {
        return myImage.getWidth();
    }

    @Override
    public int getHeight() {
        return myImage.getHeight();
    }

    @Override
    public FrankensteinImage flip(boolean b, boolean b1) {
        return new Slick2DFrankensteinImage(myImage.getFlippedCopy(b, b1));
    }

    @Override
    public void draw(FrankensteinImage frankensteinImage, int i, int i1, int i2, int i3, int i4) {
        if (!(frankensteinImage instanceof Slick2DFrankensteinImage)) {
            throw new IllegalArgumentException("Invalid class: " + frankensteinImage.getClass());
        }
        Slick2DFrankensteinImage slick2DFrankensteinImage = (Slick2DFrankensteinImage) frankensteinImage;

        float oldCenterX = slick2DFrankensteinImage.myImage.getCenterOfRotationX();
        float oldCenterY = slick2DFrankensteinImage.myImage.getCenterOfRotationY();
        float oldRotation = slick2DFrankensteinImage.myImage.getRotation();

        if (i4 != 0) {
            slick2DFrankensteinImage.myImage.setCenterOfRotation(i2, i3);
            slick2DFrankensteinImage.myImage.setRotation(i4);
        }

        try {
            myImage.getGraphics().drawImage(slick2DFrankensteinImage.myImage, i, i1);
        } catch (SlickException e) {
            throw new RuntimeException("Failed to draw image", e);
        } finally {
            slick2DFrankensteinImage.myImage.setCenterOfRotation(oldCenterX, oldCenterY);
            slick2DFrankensteinImage.myImage.setRotation(oldRotation);
        }
    }

    @Override
    public FrankensteinImage getSubImage(Rectangle rectangle) {
        return new Slick2DFrankensteinImage(myImage.getSubImage(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight()));
    }

    @Override
    public FrankensteinImage replaceColors(Map<Color, Integer> colorIntegerMap, Map<Integer, Color> integerColorMap) {
        if (colorIntegerMap == null || integerColorMap == null) {
            return this;
        }
        ImageBuffer ib = new ImageBuffer(myImage.getWidth(), myImage.getHeight());
        for (int x = 0; x < myImage.getWidth(); ++x) {
            for (int y = 0; y < myImage.getHeight(); ++y) {
                org.newdawn.slick.Color c = myImage.getColor(x, y);
                Color awtColor = new Color(c.r, c.g, c.b);

                Integer id = colorIntegerMap.get(awtColor);
                if (id != null) {
                    Color newColor = integerColorMap.get(id);
                    if (newColor != null) {
                        ib.setRGBA(x, y, newColor.getRed(), newColor.getGreen(), newColor.getBlue(), c.getAlpha());
                        continue;
                    } else {
                        logger.warn("No mapping for base color " + id);
                    }
                }
                ib.setRGBA(x, y, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            }
        }
        return new Slick2DFrankensteinImage(new Image(ib));
    }

    @Override
    public FrankensteinImage resize(Size targetSize, boolean constrainProportions) {
        throw new UnsupportedOperationException();
    }

    public Image getImpl() {
        return myImage;
    }
}

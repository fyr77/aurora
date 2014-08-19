/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 09.08.13
 * Time: 17:07
 */
package ru.game.aurora.frankenstein;


import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.frankenstein.FrankensteinColor;
import ru.game.frankenstein.FrankensteinImage;
import ru.game.frankenstein.util.GeometryUtils;
import ru.game.frankenstein.util.Rectangle;
import ru.game.frankenstein.util.Size;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Map;

public class Slick2DFrankensteinImage implements FrankensteinImage {
    private final Image myImage;

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
        try {
            Image copy = new Image(myImage.getWidth(), myImage.getHeight());
            copy.getGraphics().drawImage(myImage.getFlippedCopy(b, b1), 0, 0);
            return new Slick2DFrankensteinImage(copy);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
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
    public FrankensteinImage rotate(int angle) {
        Rectangle rect = GeometryUtils.getRotatedRectangleAABB(myImage.getWidth() / 2, myImage.getHeight() / 2, 0, 0, myImage.getWidth(), myImage.getHeight(), (float) Math.toRadians(angle));
        Image newImage;
        try {
            float oldRot = myImage.getRotation();
            myImage.setCenterOfRotation(myImage.getWidth() / 2, myImage.getHeight() / 2);
            myImage.rotate(angle);
            newImage = new Image(rect.getWidth(), rect.getHeight());
            newImage.getGraphics().drawImage(myImage, (myImage.getHeight() - myImage.getWidth())/ 2, -(myImage.getHeight() - myImage.getWidth()) / 2);
            myImage.setRotation(oldRot);

        } catch (SlickException e) {
            throw new RuntimeException(e);
        }

        return new Slick2DFrankensteinImage(newImage);
    }

    @Override
    public FrankensteinImage getSubImage(Rectangle rectangle) {
        try {
            Image newImage = new Image(rectangle.getWidth(), rectangle.getHeight());
            newImage.getGraphics().drawImage(myImage.getSubImage(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight()), 0, 0);
            return new Slick2DFrankensteinImage(newImage);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public FrankensteinImage replaceColors(Map<? extends FrankensteinColor, Integer> colorIntegerMap, Map<Integer, ? extends FrankensteinColor> integerColorMap) {
        if (colorIntegerMap == null || integerColorMap == null) {
            return this;
        }
        ImageBuffer ib = new ImageBuffer(myImage.getWidth(), myImage.getHeight());
        for (int x = 0; x < myImage.getWidth(); ++x) {
            for (int y = 0; y < myImage.getHeight(); ++y) {
                org.newdawn.slick.Color c = myImage.getColor(x, y);
                Slick2DColor slickColor = new Slick2DColor(c);

                Integer id = colorIntegerMap.get(slickColor);
                if (id != null) {
                    FrankensteinColor newColor = integerColorMap.get(id);
                    if (newColor != null) {
                        ib.setRGBA(x, y, newColor.getR(), newColor.getG(), newColor.getB(), c.getAlpha());
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
        int targetX, targetY;
        if (constrainProportions) {
            final double scaleX = (double)targetSize.width / myImage.getWidth();
            final double scaleY = (double)targetSize.height / myImage.getHeight();

            targetX = (int) (myImage.getWidth() * Math.min(scaleX, scaleY));
            targetY = (int) (myImage.getHeight() * Math.min(scaleX, scaleY));
        } else {
            targetX = targetSize.width;
            targetY = targetSize.height;
        }
        try {
            Image image = new Image(targetX, targetY);
            image.getGraphics().drawImage(myImage.getScaledCopy(targetX, targetY), 0, 0);
            return new Slick2DFrankensteinImage(image);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FrankensteinImage getShadow() {
        final int width = myImage.getWidth();
        final int height = myImage.getHeight();

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final Color c = myImage.getColor(x, y);
                if (c.getAlpha() != 0) {
                    bi.setRGB(x, y, 0xa0000000);
                }
            }
        }


        AffineTransform transform = new AffineTransform();
        transform.shear(-0.5, 0);
        transform.translate(0.25 * myImage.getWidth(), 0);
        transform.scale(1.0, 0.5);
        AffineTransformOp shearOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage resultBufferedImage = shearOp.filter(bi, null);

        ImageBuffer buffer = new ImageBuffer(resultBufferedImage.getWidth(), resultBufferedImage.getHeight());
        final int resultWidth = buffer.getWidth();
        final int resultHeight = buffer.getHeight();
        for (int y = 0; y < resultHeight; ++y) {
            for (int x = 0; x < resultWidth; ++x) {
                int argb = resultBufferedImage.getRGB(x, y);
                buffer.setRGBA(x, y, (argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, (argb >> 24) & 0xff);
            }
        }
        final Image shadowImage = new Image(buffer);
        return new Slick2DFrankensteinImage(shadowImage);
    }

    @Override
    public FrankensteinImage cropImage() {
        int leftX = getWidth();
        int rightX = 0;

        final int height = getHeight();
        final int width = getWidth();
        int topY = height;
        int bottomY = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (myImage.getColor(x, y).getAlpha() > 0) {
                    leftX = Math.min(x, leftX);
                    rightX = Math.max(x, rightX);

                    topY = Math.min(y, topY);
                    bottomY = Math.max(y, bottomY);
                }
            }
        }

        if (rightX <= leftX + 1 || bottomY <= topY + 1) {
            try {
                return new Slick2DFrankensteinImage(new Image(1, 1));
            } catch (SlickException e) {
                logger.error("Error while creating image", e);
            }
        }

        return getSubImage(new Rectangle(leftX, topY, rightX - leftX + 1, bottomY - topY + 1));

    }

    public Image getImpl() {
        return myImage;
    }
}

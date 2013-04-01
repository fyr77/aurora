/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.04.13
 * Time: 17:26
 */
package ru.game.aurora.world.planet;

import libnoiseforjava.exception.ExceptionInvalidParam;
import libnoiseforjava.module.Perlin;
import libnoiseforjava.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

/**
 * Generates planetary sprite for use on star system view.
 * Uses Perlin noise and libnoise library
 */
public class PlanetSpriteGenerator
{
    private Perlin p = new Perlin();

    /**
     * Create mask with a shadow.
     * Mask is a black circle with an excluded smaller circle inside it
     * @param radius circle radius
     * @param solX center of excluded circle
     * @param solY center of excluded circle
     * @param solRadius radius of excluded circle
     * @return
     */
    private BufferedImage createMask(int radius, int solX, int solY, int solRadius)
    {
        BufferedImage result = new BufferedImage(2 * radius, 2 * radius, BufferedImage.TYPE_4BYTE_ABGR);
        final int diameter = 2 * radius;
        for (int i = 0; i < diameter; ++i) {
            for (int j = 0; j < diameter; ++j) {

                if (Math.pow(radius - i, 2) + Math.pow(radius - j, 2) > radius * radius) {
                    continue;
                }

                if (Math.pow(solX - i, 2) + Math.pow(solY- j, 2) < solRadius * solRadius){
                    continue;
                }

                result.setRGB(i, j, 0xFF000000);
            }
        }

        return result;
    }

    private BufferedImage createPlanetSprite(PlanetCategory category, int size)
    {
        try {
            int width = 100;
            int height = 100;


            NoiseMap heightMap = new NoiseMap(width, height);
            NoiseMapBuilderPlane heightMapBuilder = new NoiseMapBuilderPlane();
            heightMapBuilder.setSourceModule(p);
            heightMapBuilder.setDestNoiseMap(heightMap);
            heightMapBuilder.setDestSize(width, height);
            heightMapBuilder.setBounds(2.0, 6.0, 1.0, 5.0);
            heightMapBuilder.build();

            RendererImage renderer = new RendererImage();
            ImageCafe image = new ImageCafe(width, height);
            renderer.setSourceNoiseMap(heightMap);
            renderer.setDestImage(image);

            renderer.clearGradient();
            renderer.addGradientPoint(-1.0000, new ColorCafe(0, 0, 128, 255)); // deeps
            renderer.addGradientPoint(-0.2500, new ColorCafe(  0,   0, 255, 255)); // shallow
            renderer.addGradientPoint(0.0000, new ColorCafe(  0, 128, 255, 255)); // shore
            renderer.addGradientPoint(0.0625, new ColorCafe(240, 240,  64, 255)); // sand
            renderer.addGradientPoint(0.1250, new ColorCafe( 32, 160,   0, 255)); // grass
            renderer.addGradientPoint(0.3750, new ColorCafe(224, 224,   0, 255)); // dirt
            renderer.addGradientPoint(0.7500, new ColorCafe(128, 128, 128, 255)); // rock
            renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255)); // snow

            renderer.render();

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {

                    if (Math.pow(width / 2 - i, 2) + Math.pow(height / 2- j, 2) > Math.pow(width / 2, 2)) {
                        continue;
                    }

                    ColorCafe c = image.getValue(i, j);
                    int rgb = 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                    result.setRGB(i, j, rgb);
                }
            }


            float[] matrix = new float[9];
            for (int i = 0; i < 9; i++)
                matrix[i] = 1.0f/9;

            BufferedImage blurred = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null );
            op.filter(result, blurred);


            BufferedImage firstMask = createMask(50, 75, 75, 20);
            BufferedImage secondMask = createMask(50, 75, 75, 60);

            float[] scales = { 1f, 1f, 1f, 0.4f };
            float[] offsets = new float[4];
            RescaleOp rop = new RescaleOp(scales, offsets, null);

            /* Draw the image, applying the filter */
            blurred.createGraphics().drawImage(firstMask, rop, 0, 0);
            blurred.createGraphics().drawImage(secondMask, rop, 0, 0);


            BufferedImage finalResult = new BufferedImage(width + 10, height + 10, BufferedImage.TYPE_4BYTE_ABGR);

            for (int i = 0; i < width + 10; ++i) {
                for (int j = 0; j < height + 10; ++j) {
                    double d = Math.pow(width / 2 + 5 - i, 2) + Math.pow(height / 2- j + 5, 2);
                    if (d > Math.pow(width / 2, 2) && d < Math.pow(width / 2 + 5, 2)) {
                        short alpha = (short)(255 - 255 * (Math.sqrt(d) - (width / 2)) / 5.0);
                        finalResult.setRGB(i, j, alpha << 24 | 0x00FFFFFF);
                    }
                }
            }
            finalResult.getGraphics().drawImage(blurred, 5, 5, null);
            ImageIO.write(finalResult, "png", new File("out.png"));
            return finalResult;
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;

    }

    public static void main(String[] args) throws ExceptionInvalidParam, IOException {

    }
}

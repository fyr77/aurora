/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.04.13
 * Time: 17:26
 */
package ru.game.aurora.world.planet;

import libnoiseforjava.module.Perlin;
import libnoiseforjava.util.*;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.StarSystem;

import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates planetary sprite for use on star system view.
 * Uses Perlin noise and libnoise library
 */
public class PlanetSpriteGenerator {
    private static final class PlanetSpriteParameters {
        public final PlanetCategory cat;

        public final int size;

        private PlanetSpriteParameters(PlanetCategory cat, int size) {
            this.cat = cat;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlanetSpriteParameters that = (PlanetSpriteParameters) o;

            return size == that.size && cat == that.cat;
        }

        @Override
        public int hashCode() {
            int result = cat.hashCode();
            result = 31 * result + size;
            return result;
        }
    }

    private Map<PlanetSpriteParameters, Image> cache = new HashMap<PlanetSpriteParameters, Image>();

    private Perlin p = new Perlin();

    private int seedIncrementer = 0;

    private static final PlanetSpriteGenerator instance = new PlanetSpriteGenerator();

    public static PlanetSpriteGenerator getInstance() {
        return instance;
    }

    public Image createPlanetSprite(Camera camera, PlanetCategory cat, int size) {
        return createPlanetSprite(camera, new PlanetSpriteParameters(cat, size));
    }

    /**
     * Create mask with a shadow.
     * Mask is a black circle with an excluded smaller circle inside it
     *
     * @param radius    circle radius
     * @param solX      center of excluded circle
     * @param solY      center of excluded circle
     * @param solRadius radius of excluded circle
     * @return
     */
    private BufferedImage createMask(int radius, int solX, int solY, int solRadius) {
        BufferedImage result = new BufferedImage(2 * radius, 2 * radius, BufferedImage.TYPE_4BYTE_ABGR);
        final int diameter = 2 * radius;
        for (int i = 0; i < diameter; ++i) {
            for (int j = 0; j < diameter; ++j) {

                if (Math.pow(radius - i, 2) + Math.pow(radius - j, 2) > radius * radius) {
                    continue;
                }

                if (Math.pow(solX - i, 2) + Math.pow(solY - j, 2) < solRadius * solRadius) {
                    continue;
                }

                result.setRGB(i, j, 0xFF000000);
            }
        }

        return result;

    }

    private Image createPlanetSprite(Camera cam, PlanetSpriteParameters params) {
        Image rz = cache.get(params);
        if (rz != null) {
            return rz;
        }
        rz = createPlanetSpriteImpl(cam, params);
        cache.put(params, rz);
        return rz;
    }

    private Image createPlanetSpriteImpl(Camera cam, PlanetSpriteParameters params) {
        try {
            final int radius = StarSystem.PLANET_SCALE_FACTOR * cam.getTileWidth() / (2 * params.size);
            int width = 2 * radius;
            int height = 2 * radius;


            NoiseMap heightMap = new NoiseMap(width, height);
            p.setSeed((int) System.currentTimeMillis() + (seedIncrementer++));
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
            for (byte st : params.cat.availableSurfaces) {
                if (st == SurfaceTypes.WATER) {
                    renderer.addGradientPoint(-1.0000, new ColorCafe(0, 0, 128, 255)); // deeps
                    renderer.addGradientPoint(-0.2500, new ColorCafe(0, 0, 255, 255)); // shallow
                    renderer.addGradientPoint(0.0000, new ColorCafe(0, 128, 255, 255)); // shore
                } else if (st == SurfaceTypes.DIRT) {
                    renderer.addGradientPoint(0.0625, new ColorCafe(240, 240, 64, 255)); // sand
                    renderer.addGradientPoint(0.1250, new ColorCafe(32, 160, 0, 255)); // grass
                    renderer.addGradientPoint(0.3750, new ColorCafe(224, 224, 0, 255)); // dirt
                } else if (st == SurfaceTypes.ICE) {
                    renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255)); // snow
                } else if (st == SurfaceTypes.ROCKS) {
                    renderer.addGradientPoint(0.7500, new ColorCafe(128, 128, 128, 255)); // rock
                }
            }

            renderer.render();

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {

                    if (Math.pow(width / 2 - i, 2) + Math.pow(height / 2 - j, 2) > Math.pow(width / 2, 2)) {
                        continue;
                    }

                    ColorCafe c = image.getValue(i, j);
                    int rgb = 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                    result.setRGB(i, j, rgb);
                }
            }

            float[] matrix = new float[9];
            for (int i = 0; i < 9; i++)
                matrix[i] = 1.0f / 9;

            BufferedImage blurred = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);
            op.filter(result, blurred);

            // todo: mask based on position relative to sun
            BufferedImage firstMask = createMask(50, (int) (0.75 * width), (int) (0.75 * height), (int) (0.2 * width));
            BufferedImage secondMask = createMask(50, (int) (0.75 * width), (int) (0.75 * height), (int) (0.6 * width));

            float[] scales = {1f, 1f, 1f, 0.4f};
            float[] offsets = new float[4];
            RescaleOp rop = new RescaleOp(scales, offsets, null);

            /* Draw the image, applying the filter */
            blurred.createGraphics().drawImage(firstMask, rop, 0, 0);
            blurred.createGraphics().drawImage(secondMask, rop, 0, 0);


            ImageBuffer id = new ImageBuffer(width + 10, height + 10);

            for (int i = 0; i < width + 10; ++i) {
                for (int j = 0; j < height + 10; ++j) {
                    double d = Math.pow(width / 2 + 5 - i, 2) + Math.pow(height / 2 - j + 5, 2);
                    if (d > Math.pow(width / 2, 2) && d < Math.pow(width / 2 + 5, 2)) {
                        short alpha = (short) (255 - 255 * (Math.sqrt(d) - (width / 2)) / 5.0);
                        id.setRGBA(i, j, 0xff, 0xff, 0xff, alpha);
                    }
                }
            }
            Image finalResult = new Image(id);
            finalResult.getGraphics().drawImage(EngineUtils.createImage(blurred), 5, 5);
            // ImageIO.write(finalResult, "png", new File("out.png"));
            return finalResult;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

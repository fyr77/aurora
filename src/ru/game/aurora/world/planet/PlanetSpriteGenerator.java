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
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.StarSystem;

import java.awt.image.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates planetary sprite for use on star system view.
 * Uses Perlin noise and libnoise library
 */
public class PlanetSpriteGenerator {
    private static final class PlanetSpriteParameters {

        public final boolean hasAtmosphere;

        public final PlanetCategory cat;

        public final int size;

        private PlanetSpriteParameters(boolean hasAtmosphere, PlanetCategory cat, int size) {
            this.hasAtmosphere = hasAtmosphere;
            this.cat = cat;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlanetSpriteParameters that = (PlanetSpriteParameters) o;

            if (hasAtmosphere != that.hasAtmosphere) return false;
            if (size != that.size) return false;
            if (cat != that.cat) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (hasAtmosphere ? 1 : 0);
            result = 31 * result + (cat != null ? cat.hashCode() : 0);
            result = 31 * result + size;
            return result;
        }
    }

    private Map<PlanetSpriteParameters, Collection<Image>> cache = new HashMap<PlanetSpriteParameters, Collection<Image>>();

    private Perlin p = new Perlin();

    private int seedIncrementer = 0;

    private static final int SPRITES_PER_PLANET_TYPE = 3;

    private static final PlanetSpriteGenerator instance = new PlanetSpriteGenerator();

    public static PlanetSpriteGenerator getInstance() {
        return instance;
    }

    public Image createPlanetSprite(Camera camera, PlanetCategory cat, int size, boolean hasAtmosphere) {
        return createPlanetSprite(camera, new PlanetSpriteParameters(hasAtmosphere, cat, size));
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
        Collection<Image> images = cache.get(params);
        if (images != null && images.size() >= SPRITES_PER_PLANET_TYPE) {
            return CollectionUtils.selectRandomElement(images);
        }
        if (images == null) {
            images = new ArrayList<Image>(SPRITES_PER_PLANET_TYPE);
        }
        Image im = createPlanetSpriteImpl(cam, params);
        images.add(im);
        cache.put(params, images);
        return im;
    }

    private void setRockPlanetGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(0, 0, 128, 255)); // deeps
        renderer.addGradientPoint(-0.9, new ColorCafe(0, 0, 255, 255)); // shallow
        renderer.addGradientPoint(-0.7000, new ColorCafe(0, 128, 255, 255)); // shore
        renderer.addGradientPoint(-0.500, new ColorCafe(247, 203, 121, 255)); // rock
        renderer.addGradientPoint(0.000, new ColorCafe(251, 166, 89, 255)); // rock
        renderer.addGradientPoint(0.500, new ColorCafe(128, 128, 128, 255)); // rock
        renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255)); // snow
    }

    private void setIcePlanetGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(205, 205, 205, 255));
        renderer.addGradientPoint(-0.2500, new ColorCafe(228, 228, 228, 255));
        renderer.addGradientPoint(0.3000, new ColorCafe(100, 100, 100, 255));
        renderer.addGradientPoint(0.6000, new ColorCafe(128, 128, 128, 255));
        renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255));
    }

    private void setEarthLikePlanetGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(0, 0, 128, 255)); // deeps
        renderer.addGradientPoint(-0.2500, new ColorCafe(0, 0, 255, 255)); // shallow
        renderer.addGradientPoint(0.0000, new ColorCafe(0, 128, 255, 255)); // shore
        renderer.addGradientPoint(0.7500, new ColorCafe(128, 128, 128, 255)); // rock
        renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255)); // snow
        renderer.addGradientPoint(0.0625, new ColorCafe(240, 240, 64, 255)); // sand
        renderer.addGradientPoint(0.1250, new ColorCafe(32, 160, 0, 255)); // grass
        renderer.addGradientPoint(0.3750, new ColorCafe(224, 224, 0, 255)); // dirt
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

            switch (params.cat) {
                case PLANET_ROCK:
                    setRockPlanetGradients(renderer);
                    break;
                case PLANET_ICE:
                    setIcePlanetGradients(renderer);
                    break;
                default:
                    throw new IllegalArgumentException("Can not generate sprite for planet category" + params.cat);
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
            BufferedImage firstMask = createMask(radius, (int) (0.75 * width), (int) (0.75 * height), (int) (0.2 * width));
            BufferedImage secondMask = createMask(radius, (int) (0.75 * width), (int) (0.75 * height), (int) (0.6 * width));

            float[] scales = {1f, 1f, 1f, 0.4f};
            float[] offsets = new float[4];
            RescaleOp rop = new RescaleOp(scales, offsets, null);

            /* Draw the image, applying the filter */
            blurred.createGraphics().drawImage(firstMask, rop, 0, 0);
            blurred.createGraphics().drawImage(secondMask, rop, 0, 0);

            if (params.hasAtmosphere) {
                // draw atmosphere 'glow' surrounding the planet
                final int glowRadius = 20;
                ImageBuffer id = new ImageBuffer(width + glowRadius, height + glowRadius);

                for (int i = 0; i < width + glowRadius; ++i) {
                    for (int j = 0; j < height + glowRadius; ++j) {
                        double d = Math.pow((width + glowRadius)/2 - i, 2) + Math.pow((height + glowRadius) / 2 - j, 2);
                        if (d > Math.pow(width/ 2, 2) && d < Math.pow((width + glowRadius) / 2, 2)) {
                            short alpha = (short) (255 - 255 * (Math.sqrt(d) - (width / 2)) / (glowRadius / 2));
                            id.setRGBA(i, j, 0xff, 0xff, 0xff, alpha);
                        }
                    }
                }
                Image finalResult = new Image(id);
                finalResult.getGraphics().drawImage(EngineUtils.createImage(blurred), glowRadius / 2, glowRadius / 2);
                return finalResult;
            } else {
                return EngineUtils.createImage(blurred);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

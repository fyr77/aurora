/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 01.04.13
 * Time: 17:26
 */
package ru.game.aurora.world.planet;

import libnoiseforjava.exception.ExceptionInvalidParam;
import libnoiseforjava.util.ColorCafe;
import libnoiseforjava.util.ImageCafe;
import libnoiseforjava.util.NoiseMap;
import libnoiseforjava.util.RendererImage;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.StarSystem;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
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

    private Map<PlanetSpriteParameters, Collection<Image>> cache = new HashMap<>();

    private PerlinNoiseGeneratorWrapper noiseGeneratorWrapper = new PerlinNoiseGeneratorWrapper();

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
    private BufferedImage createMask(float radius, int solX, int solY, int solRadius) {
        BufferedImage result = new BufferedImage((int) (2 * radius), (int) (2 * radius), BufferedImage.TYPE_4BYTE_ABGR);
        final float diameter = 2 * radius;
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
        final int spritesPerType = Configuration.getIntProperty("world.planet.spriteGenerator.cacheSize");
        if (images != null && images.size() >= spritesPerType) {
            return CollectionUtils.selectRandomElement(images);
        }
        if (images == null) {
            images = new ArrayList<>(spritesPerType);
        }
        Image im = createPlanetSpriteImpl(cam, params);
        images.add(im);
        cache.put(params, images);
        return im;
    }

    private void setGasGiantGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(45, 19, 18, 255));
        renderer.addGradientPoint(-0.7500, new ColorCafe(84, 45, 30, 255));
        renderer.addGradientPoint(-0.2000, new ColorCafe(183, 111, 161, 255));
        renderer.addGradientPoint(0.5000, new ColorCafe(194, 161, 118, 255));
        renderer.addGradientPoint(1.0000, new ColorCafe(251, 233, 193, 255));
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

    private void setWaterPlanetGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(0, 0, 128, 255)); // deeps
        renderer.addGradientPoint(-0.2500, new ColorCafe(0, 0, 255, 255)); // shallow
        renderer.addGradientPoint(0.3000, new ColorCafe(0, 128, 255, 255)); // shore
        renderer.addGradientPoint(0.4000, new ColorCafe(0, 128, 255, 255));
        renderer.addGradientPoint(0.6000, new ColorCafe(128, 128, 128, 255));
        renderer.addGradientPoint(0.8000, new ColorCafe(64, 64, 64, 255));
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


    private BufferedImage getScaledImage(BufferedImage image, float width, float height) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        return bilinearScaleOp.filter(
                image,
                new BufferedImage((int) width, (int) height, image.getType()));
    }

    private Image createPlanetSpriteImpl(Camera cam, PlanetSpriteParameters params) {
        try {
            final float radius = StarSystem.PLANET_SCALE_FACTOR * cam.getTileWidth() / (4 * params.size);
            float width = 2 * radius;
            float height = 2 * radius;

            double scale = Configuration.getDoubleProperty("world.planet.spriteGenerator.scale");
            final int noiseWidth = (int) Math.ceil(width / (float) scale);
            final int noiseHeight = (int) Math.ceil(height / (float) scale);
            NoiseMap heightMap = noiseGeneratorWrapper.buildNoiseMap(noiseWidth, noiseHeight);

            RendererImage renderer = new RendererImage();
            ImageCafe image = new ImageCafe(noiseWidth, noiseHeight);
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
                case PLANET_WATER:
                    setWaterPlanetGradients(renderer);
                    break;
                case GAS_GIANT:
                    setGasGiantGradients(renderer);
                    break;
                default:
                    throw new IllegalArgumentException("Can not generate sprite for planet category" + params.cat);
            }

            renderer.render();

            BufferedImage result = new BufferedImage(noiseWidth, noiseHeight, BufferedImage.TYPE_4BYTE_ABGR);
            for (int i = 0; i < noiseWidth; ++i) {
                for (int j = 0; j < noiseHeight; ++j) {

                    if (Math.pow(noiseWidth / 2 - i, 2) + Math.pow(noiseHeight / 2 - j, 2) > Math.pow(noiseWidth / 2, 2)) {
                        continue;
                    }

                    ColorCafe c = image.getValue(i, j);
                    int rgb = 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                    result.setRGB(i, j, rgb);
                }
            }

            // scale image up
            result = getScaledImage(result, width, height);

            // todo: mask based on position relative to sun
            BufferedImage firstMask = createMask(radius, (int) (0.75 * width), (int) (0.75 * height), (int) (0.2 * width));
            BufferedImage secondMask = createMask(radius, (int) (0.75 * width), (int) (0.75 * height), (int) (0.6 * width));

            float[] scales = {1f, 1f, 1f, 0.4f};
            float[] offsets = new float[4];
            RescaleOp rop = new RescaleOp(scales, offsets, null);

            /* Draw the image, applying the filter */
            result.createGraphics().drawImage(firstMask, rop, 0, 0);
            result.createGraphics().drawImage(secondMask, rop, 0, 0);

            if (params.hasAtmosphere) {
                // draw atmosphere 'glow' surrounding the planet
                final int glowRadius = 20;
                ImageBuffer id = new ImageBuffer((int) (width + glowRadius), (int) (height + glowRadius));

                for (int i = 0; i < width + glowRadius; ++i) {
                    for (int j = 0; j < height + glowRadius; ++j) {
                        double d = Math.pow((width + glowRadius) / 2 - i, 2) + Math.pow((height + glowRadius) / 2 - j, 2);
                        if (d > Math.pow(width / 2, 2) && d < Math.pow((width + glowRadius) / 2, 2)) {
                            short alpha = (short) (255 - 255 * (Math.sqrt(d) - (width / 2)) / (glowRadius / 2));
                            id.setRGBA(i, j, 0xff, 0xff, 0xff, alpha);
                        }
                    }
                }
                Image finalResult = new Image(id);
                finalResult.getGraphics().drawImage(EngineUtils.createImage(result), glowRadius / 2, glowRadius / 2);
                return finalResult;
            } else {
                return EngineUtils.createImage(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

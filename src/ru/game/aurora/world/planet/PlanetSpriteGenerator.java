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
import org.newdawn.slick.SlickException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.frankenstein.Slick2DColor;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.StarSystem;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * Generates planetary sprite for use on star system view.
 * Uses Perlin noise and libnoise library
 */
public class PlanetSpriteGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PlanetSpriteGenerator.class);

    private static final class PlanetSpriteParameters {

        public final boolean hasAtmosphere;

        public final PlanetCategory cat;

        public final int size;

        public final float shadowXFactor;
        public final float shadowYFactor;

        public ColorCafe plantsColor = null;

        public PlanetCategory.GasGiantColors ggColor = null;

        private PlanetSpriteParameters(BasePlanet planet) {
            this.hasAtmosphere = (planet.getAtmosphere() != PlanetAtmosphere.NO_ATMOSPHERE);
            this.cat = planet.category;
            this.size = planet.size;
            double theta = Math.atan2(planet.getY(), planet.getX());
            shadowXFactor = (float) (0.5 - Math.cos(theta) * 0.25);
            shadowYFactor = (float) (0.5 - Math.sin(theta) * 0.25);
            if (planet instanceof Planet) {
                if (((Planet) planet).getFloraAndFauna() != null) {
                    Slick2DColor leafColor = ((Planet) planet).getFloraAndFauna().getColorMap().get(5);
                    this.plantsColor = new ColorCafe(leafColor.getR(), leafColor.getG(), leafColor.getB(), 255);
                }
            }
            if (planet instanceof GasGiant) {
                this.ggColor = ((GasGiant) planet).getColor();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlanetSpriteParameters that = (PlanetSpriteParameters) o;

            boolean shadowPositionNearlySame =
                    (Math.abs(that.shadowXFactor - shadowXFactor) < 0.1) &&
                    (Math.abs(that.shadowYFactor - shadowYFactor) < 0.1);

            boolean ggEq = (that.ggColor == ggColor);

            return hasAtmosphere == that.hasAtmosphere && size == that.size && cat == that.cat && shadowPositionNearlySame && ggEq;
        }

        @Override
        public int hashCode() {
            int result = (hasAtmosphere ? 1 : 0);
            result = 31 * result + (cat != null ? cat.hashCode() : 0);
            result = 31 * result + size;
            return result;
        }
    }

    private final Map<PlanetSpriteParameters, Collection<Image>> cache = new HashMap<>();

    private final PerlinNoiseGeneratorWrapper noiseGeneratorWrapper = new PerlinNoiseGeneratorWrapper();

    private static final PlanetSpriteGenerator instance = new PlanetSpriteGenerator();

    public static PlanetSpriteGenerator getInstance() {
        return instance;
    }

    public Image createPlanetSprite(Camera camera, BasePlanet planet) {
        return createPlanetSprite(camera, new PlanetSpriteParameters(planet));
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

    private void setFullStonePlanetGradients(RendererImage renderer) throws ExceptionInvalidParam {
        renderer.addGradientPoint(-1.0000, new ColorCafe(50, 50, 70, 255));
        renderer.addGradientPoint(-0.9, new ColorCafe(70, 80, 80, 255));
        renderer.addGradientPoint(-0.7000, new ColorCafe(100, 100, 100, 255));
        renderer.addGradientPoint(-0.500, new ColorCafe(140, 140, 140, 255));
        renderer.addGradientPoint(0.000, new ColorCafe(200, 180, 180, 255));
        renderer.addGradientPoint(0.500, new ColorCafe(128, 128, 128, 255));
        renderer.addGradientPoint(1.0000, new ColorCafe(255, 255, 255, 255));
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

        BufferedImage result = bilinearScaleOp.filter(image, new BufferedImage((int) width, (int) height, image.getType()));
        //"округлить" растянутый спрайт
        double radius = width / 2 - 1;
        for (int y = 0; y < height; y++) {
            double kY = height / 2 - y + 0.5;
            for (int x = 0; x < width; x++) {
                double kX = width / 2 - x + 0.5;
                if (Math.sqrt( Math.pow(kX, 2) +  Math.pow(kY, 2)) > radius) {
                    result.setRGB(x, y, 0x00000000);
                }
            }
        }
        return result;
    }

    private Image createPlanetSpriteImpl(Camera cam, PlanetSpriteParameters params) {
        try {
            final float radius = StarSystem.PLANET_SCALE_FACTOR * cam.getTileWidth() / (4 * params.size);
            float width = 2 * radius;
            float height = 2 * radius;

            double scale = Configuration.getDoubleProperty("world.planet.spriteGenerator.scale");

            final int imageWidth = (int) Math.ceil(width / (float) scale);
            final int imageHeight = (int) Math.ceil(height / (float) scale);
            ImageCafe image = new ImageCafe(imageWidth, imageHeight);

            if (params.cat != PlanetCategory.GAS_GIANT) {
                NoiseMap heightMap = noiseGeneratorWrapper.buildNoiseMap(imageWidth, imageHeight);

                RendererImage renderer = new RendererImage();

                renderer.setSourceNoiseMap(heightMap);
                renderer.setDestImage(image);

                renderer.clearGradient();

                switch (params.cat) {
                    case PLANET_FULL_STONE:
                        setFullStonePlanetGradients(renderer);
                        break;
                    case PLANET_ROCK:
                        setRockPlanetGradients(renderer);
                        break;
                    case PLANET_ICE:
                        setIcePlanetGradients(renderer);
                        break;
                    case PLANET_WATER:
                        setWaterPlanetGradients(renderer);
                        break;
                    default:
                        throw new IllegalArgumentException("Can not generate sprite for planet category" + params.cat);
                }

                if (params.plantsColor != null) {
                    renderer.addGradientPoint(0.200, params.plantsColor);
                }

                renderer.render();
            } else {
                Random r = CommonRandom.getRandom();
                int startR = 70;
                int startG = 70;
                int startB = 70;
                switch (params.ggColor) {
                    case RED:
                        startR = 140;
                        break;
                    case BLUE:
                        startB = 160;
                        break;
                    case YELLOW:
                        startR = 160;
                        startG = 120;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type of gas giant: " + params.ggColor);
                }
                int red = startR;
                int green = startG;
                int blue = startB;
                for (int y = 0; y < image.getHeight(); y++) {
                    red = red + r.nextInt(30) - 15;
                    green = green + r.nextInt(30) - 15;
                    blue = blue + r.nextInt(30) - 15;
                    if (
                            Math.abs(startR - red) > 50 ||
                            Math.abs(startG - green) > 50 ||
                            Math.abs(startB - blue) > 50 ||
                            Math.abs(red - green) > (Math.abs(startR - startG) + 15) ||
                            Math.abs(red - blue) > (Math.abs(startR - startB) + 15) ||
                            Math.abs(blue - green) > (Math.abs(startB - startG) + 15))  {
                        red = startR;
                        green = startG;
                        blue = startB;
                    }
                    ColorCafe col = new ColorCafe(red, green, blue, 255);
                    for (int x = 0; x < image.getWidth(); x++) {
                        image.setValue(x, y, col);
                    }
                }
            }

            BufferedImage result = convertToPlanetSprite(image, params.shadowXFactor, params.shadowYFactor);
            // scale image up
            result = getScaledImage(result, width, height);

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
            logger.error("Failed to create sprite for planet", ex);
        }
        return null;
    }

    private static BufferedImage convertToPlanetSprite(ImageCafe source, float shadowXFactor, float shadowYFactor) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        int[] s = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ColorCafe c = source.getValue(x, y);
                s[y * w + x] = 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
            }
        }
        int[] fe = fisheye(shadowPlanet(s, shadowXFactor, shadowYFactor), w, h);
        //1 - чтобы убрать торчащие пиксели
        for (int y = 1; y < h; y++) {
            for (int x = 1; x < w; x++) {
                if (Math.pow(w / 2 - x, 2) + Math.pow(w / 2 - y, 2) <= Math.pow(w / 2, 2)) {
                    destination.setRGB(x, y, fe[y * w + x]);
                }
            }
        }
        return destination;
    }

    /**
     * Затемняет спрайт планеты.
     *
     * @param source            Спрайт. Ширина должна быть равна высоте.
     * @param shadowXFactor     координата X (от 0.0 до 1.0) - положение самой освещённой точки
     * @param shadowYFactor     координата Y (от 0.0 до 1.0) - положение самой освещённой точки
     *
     * @return                  Затемнённой спрайт
     */
    public static Image shadowPlanet(Image source, float shadowXFactor, float shadowYFactor) {
        int s = source.getWidth();
        int[] sourceArray = new int[s * s];
        for (int y = 0; y < s; y++) {
            for (int x = 0; x < s; x++) {
                org.newdawn.slick.Color c = source.getColor(x, y);
                sourceArray[y * s + x] = 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
            }
        }
        int[] resultArray = shadowPlanet(sourceArray, shadowXFactor, shadowYFactor);
        Image result = null;
        try {
            result = new Image(s, s);
            for (int y = 0; y < s; y++) {
                for (int x = 0; x < s; x++) {
                    org.newdawn.slick.Color c = new org.newdawn.slick.Color(resultArray[y * s + x]);
                    if (source.getColor(x, y).getAlpha() == 255) {
                        result.getGraphics().setColor(c);
                        result.getGraphics().fillRect(x, y, 1f, 1f);
                    }
                }
            }
            result.getGraphics().flush();
        } catch (SlickException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static int[] shadowPlanet(int[] source, float shadowXFactor, float shadowYFactor) {
        int[] dest = new int[source.length];
        int s = (int) Math.sqrt(source.length);
        for (int y = 0; y < s; y++) {
            double dy = s * shadowYFactor - y;
            for (int x = 0; x < s; x++) {
                double dx = s * shadowXFactor - x;
                double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) / (s / 8);
                d -= 1.3;
                if (d < 1) {
                    d = 1;
                } else {
                    d = 2 * d - 1;
                }
                Color c = new Color(source[y * s + x], true);
                Color newC = new Color((int) (c.getRed() / d),(int) (c.getGreen() / d), (int) (c.getBlue() / d), c.getAlpha());
                dest[y * s + x] = newC.getRGB();
            }
        }
        return dest;
    }

    private static int[] fisheye(int[] srcpixels, double w, double h) {
    /*
     *    Fish eye effect
     *    tejopa, 2012-04-29
     *    http://popscan.blogspot.com
     *    http://www.eemeli.de
     */
        int[] dstpixels = new int[(int)(w * h)];
        for (int y = 0; y < h; y++) {
            double ny = ((2 * y) / h) - 1;
            double ny2 = ny * ny;
            for (int x = 0; x < w; x++) {
                double nx = ((2 * x)/ w) - 1;
                double nx2 = nx * nx;
                double r = Math.sqrt(nx2 + ny2);
                if (0.0 <= r && r <= 1.0) {
                    double nr = Math.sqrt(1.0 - r * r);
                    nr = (r + (1.0 - nr)) / 2.0;
                    if (nr <= 1.0) {
                        double theta = Math.atan2(ny, nx);
                        double nxn = nr * Math.cos(theta);
                        double nyn = nr * Math.sin(theta);
                        int x2 = (int)(((nxn + 1) * w) / 2.0);
                        int y2 = (int)(((nyn + 1) * h) / 2.0);
                        int srcpos = (int)(y2 * w + x2);
                        if (srcpos >= 0 & srcpos < w * h) {
                            dstpixels[(int)(y * w + x)] = srcpixels[srcpos];
                        }
                    }
                }
            }
        }
        return dstpixels;
    }
}

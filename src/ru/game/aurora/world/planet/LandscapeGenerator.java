/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.12
 * Time: 18:12
 */
package ru.game.aurora.world.planet;


import libnoiseforjava.util.NoiseMap;

public class LandscapeGenerator {

    private static PerlinNoiseGeneratorWrapper noiseGeneratorWrapper = new PerlinNoiseGeneratorWrapper();

    // noise generation is rather slow, so generate noise SCALE_FACTOR times smaller than planet surface size, and then stretch
    private static final int SCALE_FACTOR = 2;

    private static byte getTileForRockPlanet(double value) {
        if (value < -0.5) {
            return SurfaceTypes.WATER | SurfaceTypes.OBSTACLE_MASK;
        }

        if (value < -0.1) {
            return SurfaceTypes.DIRT;
        }

        if (value < 0.2) {
            return SurfaceTypes.STONES;
        }

        if (value < 0.4) {
            return SurfaceTypes.ROCKS;
        }

        return SurfaceTypes.ROCKS | SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
    }

    private static byte getTileForIcePlanet(double value) {
        if (value < -0.5) {
            return SurfaceTypes.WATER | SurfaceTypes.OBSTACLE_MASK;
        }

        if (value < -0.2) {
            return SurfaceTypes.ICE;
        }

        if (value < 0.0) {
            return SurfaceTypes.STONES;
        }

        if (value < 0.2) {
            return SurfaceTypes.ROCKS;
        }

        if (value < 0.5) {
            return SurfaceTypes.SNOW;
        }

        return SurfaceTypes.SNOW | SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
    }

    private static byte getTileForWaterPlanet(double value) {
        if (value < 0.3) {
            return SurfaceTypes.WATER | SurfaceTypes.OBSTACLE_MASK;
        }
        if (value < 0.5) {
            return SurfaceTypes.DIRT;
        }
        if (value < 0.6) {
            return SurfaceTypes.STONES;
        }

        return SurfaceTypes.STONES | SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
    }

    public static byte[][] generateLandscapePerlin(PlanetCategory cat, int width, int height) {
        if (width % SCALE_FACTOR != 0 || height % SCALE_FACTOR != 0) {
            throw new IllegalArgumentException("Planet surface dimensions should be divided by " + SCALE_FACTOR);
        }

        final int noiseWidth = width / SCALE_FACTOR;
        final int noiseHeight = height / SCALE_FACTOR;
        NoiseMap noiseMap = noiseGeneratorWrapper.buildNoiseMap(noiseWidth, noiseHeight);
        byte[][] surface = new byte[height][width];

        for (int y = 0; y < noiseHeight; ++y) {
            for (int x = 0; x < noiseHeight; ++x) {
                byte tile;
                final double noiseValue = noiseMap.getValue(x, y);
                switch (cat) {
                    case PLANET_ROCK:
                        tile = getTileForRockPlanet(noiseValue);
                        break;
                    case PLANET_ICE:
                        tile = getTileForIcePlanet(noiseValue);
                        break;
                    case PLANET_WATER:
                        tile = getTileForWaterPlanet(noiseValue);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported planet category for surface generator");
                }

                for (int i = 0; i < SCALE_FACTOR; ++i) {
                    for (int j = 0; j < SCALE_FACTOR; ++j) {
                        surface[y * SCALE_FACTOR + i][x * SCALE_FACTOR + j] = tile;
                    }
                }

            }
        }
        return surface;
    }


}

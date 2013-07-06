/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.12
 * Time: 18:12
 */
package ru.game.aurora.world.planet;


import ru.game.aurora.application.CommonRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LandscapeGenerator {

    private static final int ITERATIONS = 2;

    private static int wrap(int x, int size) {
        if (x < 0) {
            return size + x;
        } else if (x >= size) {
            return x - size;
        }
        return x;
    }

    // check that every mountain tile has at least 1 neighbour both on x and on y (standalone mountain tiles can not be drawn correctly)
    private static void updateMountains(byte[][] surface, int width, int height) {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    if (SurfaceTypes.isMountain(surface[i][j])) {
                        continue;
                    }

                    if (SurfaceTypes.isMountain(surface[wrap(i + 1, height)][j]) && SurfaceTypes.isMountain(surface[wrap(i - 1, height)][j])) {
                        surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                        changed = true;
                        continue;
                    }

                    if (SurfaceTypes.isMountain(surface[i][wrap(j - 1, width)]) && SurfaceTypes.isMountain(surface[i][wrap(j + 1, width)])) {
                        surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    // check that every tile has at least 1 neighbour of same type both on x and on y (standalone tiles can not be drawn correctly)
    private static void fixStandaloneTiles(byte[][] surface, int width, int height) {
        boolean changed;
        int iterations = 5;
        do {
            changed = false;
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    boolean sameLeftNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[i][wrap(j - 1, width)]);
                    boolean sameRightNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[i][wrap(j + 1, width)]);

                    if (!sameLeftNeighbour && !sameRightNeighbour) {
                        surface[i][j] = (byte) (surface[i][j] & 0xf0 | SurfaceTypes.getType(surface[i][wrap(j - 1, width)]));
                        changed = true;
                        continue;
                    }

                    boolean sameUpperNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[wrap(i - 1, height)][j]);
                    boolean sameLowerNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[wrap(i + 1, height)][j]);

                    if (!sameUpperNeighbour && !sameLowerNeighbour) {
                        surface[i][j] = (byte) (surface[i][j] & 0xf0 | SurfaceTypes.getType(surface[wrap(i - 1, height)][j]));
                        changed = true;
                    }

                }
            }
        } while (changed && iterations-- > 0);
    }

    public static byte[][] generateLandscape(PlanetCategory cat, int width, int height) {
        byte[][] surface = new byte[height][width];
        final Random random = CommonRandom.getRandom();

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {

                surface[i][j] = cat.availableSurfaces[random.nextInt(cat.availableSurfaces.length)];
                if (random.nextInt(2) == 0) {
                    surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                }
            }
        }

        // generate mountain clusters
        for (int i = 0; i < random.nextInt(50) + 20; ++i) {
            int centerX = random.nextInt(width);
            int centerY = random.nextInt(height);

            for (int j = 0; j < random.nextInt(80) + 10; ++j) {
                final int x = wrap(centerX + random.nextInt(20) - 10, width);
                final int y = wrap(centerY + random.nextInt(20) - 10, height);
                surface[y][x] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
            }
        }

        // cellular automata method described here http://roguebasin.roguelikedevelopment.org/index.php?title=Cellular_Automata_Method_for_Generating_Random_Cave-Like_Levels
        Map<Byte, Byte> neighbours = new HashMap<Byte, Byte>();
        for (int iter = 0; iter < ITERATIONS; ++iter) {
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    neighbours.clear();

                    int mountainCount = 0;
                    for (int ii = -1; ii <= 1; ++ii) {
                        for (int jj = -1; jj <= 1; ++jj) {
                            if ((surface[wrap(j + jj, height)][wrap(i + ii, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0) {
                                mountainCount++;
                            }
                            byte tileValue = (byte) (surface[wrap(j + jj, height)][wrap(i + ii, width)] & 0x0F);

                            Byte b = neighbours.get(tileValue);
                            if (b == null) {
                                b = 0;
                            }
                            neighbours.put(tileValue, (byte) (b + 1));
                        }
                    }

                    for (Map.Entry<Byte, Byte> e : neighbours.entrySet()) {

                        if (e.getValue() >= 5) {
                            surface[j][i] = e.getKey();
                            break;
                        }
                    }

                    boolean sameLeftNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[i][wrap(j - 1, width)]);
                    boolean sameRightNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[i][wrap(j + 1, width)]);

                    if (!sameLeftNeighbour && !sameRightNeighbour) {
                        surface[i][j] = (byte) (surface[i][j] & 0xf0 | SurfaceTypes.getType(surface[i][wrap(j - 1, width)]));
                        continue;
                    }

                    boolean sameUpperNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[wrap(i - 1, height)][j]);
                    boolean sameLowerNeighbour = SurfaceTypes.sameBaseSurfaceType(surface[i][j], surface[wrap(i + 1, height)][j]);

                    if (!sameUpperNeighbour && !sameLowerNeighbour) {
                        surface[i][j] = (byte) (surface[i][j] & 0xf0 | SurfaceTypes.getType(surface[wrap(i - 1, height)][j]));
                    }

                    if (mountainCount >= 4) {
                        surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                    } else {
                        if (SurfaceTypes.isMountain(surface[i][j])) {
                            surface[i][j] ^= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                        }
                    }


                }
            }
        }
        updateMountains(surface, width, height);
        fixStandaloneTiles(surface, width, height);
        return surface;
    }
}

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

    public static byte[][] generateLandscape(PlanetCategory cat, int width, int height) {
        byte[][] surface = new byte[height][width];
        final Random random = CommonRandom.getRandom();

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {

                surface[i][j] = cat.availableSurfaces[random.nextInt(cat.availableSurfaces.length)];
                if (random.nextInt(3) == 0) {
                    surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                }
            }
        }

        // cellular automata method described here http://roguebasin.roguelikedevelopment.org/index.php?title=Cellular_Automata_Method_for_Generating_Random_Cave-Like_Levels
        Map<Byte, Byte> neighbours = new HashMap<Byte, Byte>();
        for (int iter = 0; iter < ITERATIONS; ++iter) {
            for (int i = 1; i < height - 1; ++i) {
                for (int j = 1; j < width - 1; ++j) {
                    neighbours.clear();

                    int mountainCount = 0;
                    for (int ii = -1; ii <= 1; ++ii) {
                        for (int jj = -1; jj <= 1; ++jj) {
                            if ((surface[j + jj][i + ii] & SurfaceTypes.MOUNTAINS_MASK) != 0) {
                                mountainCount++;
                            }
                            byte tileValue = (byte) (surface[j + jj][i + ii] & 0x0F);

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

                    if (mountainCount >= 5) {
                        surface[i][j] |= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                    } else {
                        surface[i][j] ^= SurfaceTypes.MOUNTAINS_MASK | SurfaceTypes.OBSTACLE_MASK;
                    }


                }
            }
        }

        return surface;
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 13:22
 */
package ru.game.aurora.world.planet.nature;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.planet.Planet;

/**
 * Creates and adds plants and animals to a planet
 */
public class PlanetaryLifeGenerator
{
    public static void addPlants(Planet planet)
    {
        int plantsCount = CommonRandom.getRandom().nextInt(5 * planet.getSize());
        PlantSpeciesDesc[] plants = new PlantSpeciesDesc[plantsCount];
        byte[][] plantArray = new byte[planet.getHeight()][planet.getWidth()];


        for (byte plantIdx = 1; plantIdx < plantsCount + 1; ++plantIdx) {
            //todo: algorithm for plants distribution. Preferred coordinates, tile types
            plants[plantIdx - 1] = new PlantSpeciesDesc(planet.getTileTypeAt(CommonRandom.getRandom().nextInt(planet.getWidth()), CommonRandom.getRandom().nextInt(planet.getHeight())));

            if (plants[plantIdx - 1].isSingle()) {
                for (int j = 0; j < CommonRandom.getRandom().nextInt(30 * (5 - planet.getSize())); ++j) {
                    plantArray[CommonRandom.getRandom().nextInt(planet.getWidth())][ CommonRandom.getRandom().nextInt(planet.getHeight())] = plantIdx;
                }

            } else {
                // todo: some kind of finite automata
                for (int j = 0; j < CommonRandom.getRandom().nextInt(20 * (5 - planet.getSize())); ++j) {
                    int size = CommonRandom.getRandom().nextInt(10) + 1;
                    int x = CommonRandom.getRandom().nextInt(planet.getWidth() - size - 1);
                    int y = CommonRandom.getRandom().nextInt(planet.getHeight() - size - 1);

                    for (int ii = 0; ii < size; ++ii) {
                        for (int jj = 0; jj < size; ++jj) {
                            if (CommonRandom.getRandom().nextInt(3) != 0) {
                                plantArray[y + jj][x + ii] = plantIdx;
                            }

                        }
                    }

                }
            }
        }

        planet.setPlantSpecies(plants);

        for (int i = 0; i < planet.getHeight(); ++i) {
            for (int j = 0; j < planet.getWidth(); ++j) {
                if (plantArray[i][j] != 0) {
                    planet.getPlanetObjects().add(new Plant(j, i, plants[plantArray[i][j] - 1], planet));
                }
            }
        }
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 13:22
 */
package ru.game.aurora.world.planet.nature;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.SurfaceTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates and adds plants and animals to a planet
 */
public class PlanetaryLifeGenerator
{
    public static void addPlants(Planet planet)
    {
        int plantsCount = CommonRandom.getRandom().nextInt(5 * (5 - planet.getSize()));
        PlantSpeciesDesc[] plants = new PlantSpeciesDesc[plantsCount];

        for (byte plantIdx = 0; plantIdx < plantsCount; ++plantIdx) {
            //todo: algorithm for plants distribution. Preferred coordinates, tile types

            plants[plantIdx] = new PlantSpeciesDesc(
                    SurfaceTypes.getType(planet.getTileTypeAt(CommonRandom.getRandom().nextInt(planet.getWidth()), CommonRandom.getRandom().nextInt(planet.getHeight())))
                    , CommonRandom.getRandom().nextDouble()
                    , CommonRandom.getRandom().nextDouble() * 0.3 + 0.1
                    , CommonRandom.getRandom().nextDouble() * 0.6 + 0.1
                    , false
                    , CommonRandom.getRandom().nextDouble() > 0.8
            );

        }

        planet.setPlantSpecies(plants);

        List<PlantSpeciesDesc> availablePlants = new ArrayList<>(plantsCount);
        for (int i = 0; i < planet.getHeight(); ++i) {
            for (int j = 0; j < planet.getWidth(); ++j) {
                availablePlants.clear();
                for (PlantSpeciesDesc plant : plants) {
                    if (plant.canPlantOnTile(j, i, planet)) {
                        availablePlants.add(plant);
                    }
                }
                if (availablePlants.isEmpty()) {
                    continue;
                }
                final PlantSpeciesDesc desc = CollectionUtils.selectRandomElement(availablePlants);
                if (CommonRandom.getRandom().nextDouble() <= desc.getBaseProbability()) {
                    planet.getPlanetObjects().add(new Plant(j, i, desc, planet));
                }
            }
        }
    }

    public static void addAnimals(Planet planet)
    {
        // generate random species descs. Currently only one
        final Random r = CommonRandom.getRandom();
        int speciesCount = r.nextInt(5) + 2;
        AnimalSpeciesDesc[] animalSpecies = new AnimalSpeciesDesc[speciesCount];
        for (int i = 0; i < speciesCount; ++i) {
            animalSpecies[i] = new AnimalSpeciesDesc(planet, "Unknown alien animal", r.nextBoolean(), r.nextBoolean(), r.nextInt(10) + 3, r.nextInt(6), 1 + r.nextInt(5), CollectionUtils.selectRandomElement(AnimalSpeciesDesc.Behaviour.values()));
        }
        final int animalCount = r.nextInt(10) + 5;
        for (int i = 0; i < animalCount; ++i) {
            Animal a = new Animal(planet, 0, 0, animalSpecies[r.nextInt(animalSpecies.length)]);
            int animalX;
            int animalY;
            do {
                animalX = r.nextInt(10);
                animalY = r.nextInt(10);
            } while (!SurfaceTypes.isPassible(a, planet.getTileTypeAt(animalX, animalY)));
            a.setPos(animalX, animalY);
            planet.getPlanetObjects().add(a);
        }
        planet.setAnimalSpecies(animalSpecies);
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 13:22
 */
package ru.game.aurora.world.planet.nature;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.planet.Planet;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Creates and adds plants and animals to a planet
 */
public class PlanetaryLifeGenerator {
    /**
     * Available styles of plants.
     * Within single planet only one style is allowed.
     * Only parts with this style in tag list will be used by monster generator
     */
    private static final String[] plantStyles = {"style1", "style2", "style3"};

    private static Map<Integer, Color> createColorsForPlants() {
        Map<Integer, Color> result = new HashMap<>();
        Random r = CommonRandom.getRandom();
        Color trunkColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(1, trunkColor.darker());
        result.put(2, trunkColor);
        result.put(3, trunkColor.brighter());

        Color leafColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(4, leafColor.darker());
        result.put(5, leafColor);
        result.put(6, leafColor.brighter());

        Color decorColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(7, decorColor.darker());
        result.put(8, decorColor);

        return result;
    }

    public static void setPlanetHasLife(Planet planet) {
        int plantsCount = CommonRandom.getRandom().nextInt(5 * (5 - planet.getSize()));
        PlantSpeciesDesc[] plants = new PlantSpeciesDesc[plantsCount];

        final String plantsStyle = CollectionUtils.selectRandomElement(plantStyles);
        PlanetFloraAndFauna floraAndFauna = new PlanetFloraAndFauna(plantsStyle);

        for (byte plantIdx = 0; plantIdx < plantsCount; ++plantIdx) {
            //todo: algorithm for plants distribution. Preferred coordinates, tile types

            plants[plantIdx] = new PlantSpeciesDesc(
                    CollectionUtils.selectRandomElementArray(planet.getCategory().availableSurfaceTypes)
                    , CommonRandom.getRandom().nextDouble()
                    , CommonRandom.getRandom().nextDouble() * 0.3 + 0.1
                    , CommonRandom.getRandom().nextDouble() * 0.6 + 0.1
                    , false
                    , CommonRandom.getRandom().nextDouble() > 0.8
                    , floraAndFauna
            );

        }

        final Random r = CommonRandom.getRandom();
        int speciesCount = r.nextInt(5) + 2;
        AnimalSpeciesDesc[] animalSpecies = new AnimalSpeciesDesc[speciesCount];
        for (int i = 0; i < speciesCount; ++i) {
            animalSpecies[i] = new AnimalSpeciesDesc(planet, Localization.getText("research", "animal.default_name"), r.nextBoolean(), r.nextBoolean(), r.nextInt(10) + 3, r.nextInt(6), 1 + r.nextInt(5), CollectionUtils.selectRandomElement(AnimalSpeciesDesc.Behaviour.values()));
        }

        floraAndFauna.setSpecies(animalSpecies, plants);
        planet.setFloraAndFauna(floraAndFauna);
        floraAndFauna.setColorMap(createColorsForPlants());
    }

    public static void addPlants(Planet planet) {
        List<PlantSpeciesDesc> availablePlants = new ArrayList<>();
        for (int i = 0; i < planet.getHeight(); ++i) {
            for (int j = 0; j < planet.getWidth(); ++j) {
                availablePlants.clear();
                for (PlantSpeciesDesc plant : planet.getFloraAndFauna().getPlantSpecies()) {
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

    public static void addAnimals(Planet planet) {
        // generate random species descs. Currently only one
        final Random r = CommonRandom.getRandom();
        final int animalCount = r.nextInt(10) + 5;
        AnimalSpeciesDesc[] animalSpeciesDescs = planet.getFloraAndFauna().getAnimalSpecies();
        for (int i = 0; i < animalCount; ++i) {
            Animal a = new Animal(planet, 0, 0, animalSpeciesDescs[r.nextInt(animalSpeciesDescs.length)]);
            int animalX;
            int animalY;
            do {
                animalX = r.nextInt(10);
                animalY = r.nextInt(10);
            } while (!planet.getSurface().isTilePassable(animalX, animalY));
            a.setPos(animalX, animalY);
            planet.getPlanetObjects().add(a);
        }
    }
}

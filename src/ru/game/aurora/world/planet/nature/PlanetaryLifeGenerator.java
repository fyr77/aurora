/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 13:22
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.frankenstein.Slick2DColor;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.planet.Planet;

import java.util.*;

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

    private static final String[] animalStyles = {"style1", "style2"};

    private static Map<Integer, Slick2DColor> createColorsForPlants() {
        Map<Integer, Slick2DColor> result = new HashMap<>();
        Random r = CommonRandom.getRandom();
        Color trunkColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(1, new Slick2DColor(trunkColor.darker()));
        result.put(2, new Slick2DColor(trunkColor));
        result.put(3, new Slick2DColor(trunkColor.brighter()));

        Color leafColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(4, new Slick2DColor(leafColor.darker()));
        result.put(5, new Slick2DColor(leafColor));
        result.put(6, new Slick2DColor(leafColor.brighter()));

        Color decorColor = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        result.put(7, new Slick2DColor(decorColor.darker()));
        result.put(8, new Slick2DColor(decorColor));

        return result;
    }

    public static void setPlanetHasLife(Planet planet) {
        int plantsCount = CommonRandom.getRandom().nextInt(5 * (5 - planet.getSize()));
        PlantSpeciesDesc[] plants = new PlantSpeciesDesc[plantsCount];

        final String plantsStyle = CollectionUtils.selectRandomElement(plantStyles);
        final String animalStyle = CollectionUtils.selectRandomElement(animalStyles);
        PlanetFloraAndFauna floraAndFauna = new PlanetFloraAndFauna(plantsStyle, animalStyle);

        for (byte plantIdx = 0; plantIdx < plantsCount; ++plantIdx) {
            //todo: algorithm for plants distribution. Preferred coordinates, tile types

            plants[plantIdx] = new PlantSpeciesDesc(
                    CollectionUtils.selectRandomElementArray(planet.getCategory().availableSurfaceTypes)
                    , CommonRandom.getRandom().nextDouble()
                    , CommonRandom.getRandom().nextDouble() * 0.3 + 0.1
                    , CommonRandom.getRandom().nextDouble() * 0.4 + 0.1
                    , false
                    , CommonRandom.getRandom().nextDouble() > 0.8
                    , floraAndFauna
            );

        }

        final Random r = CommonRandom.getRandom();
        int speciesCount = r.nextInt(5) + 2;
        AnimalSpeciesDesc[] animalSpecies = new AnimalSpeciesDesc[speciesCount];
        for (int i = 0; i < speciesCount; ++i) {
            animalSpecies[i] = AnimalGenerator.getInstance().generateMonster(planet);
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
                    planet.getPlanetObjects().add(new Plant(j, i, desc));
                }
            }
        }
    }

    public static void addAnimals(Planet planet) {
        // generate random species descs. Currently only one
        final Random r = CommonRandom.getRandom();
        final int animalCount = r.nextInt(100 / planet.getSize()) + 5;
        
        addAnimals(planet, animalCount);
        planet.getFloraAndFauna().setMaxAnimals(animalCount);
    }
    
    public static void addAnimals(Planet planet, int amount) {
        Random r = CommonRandom.getRandom();
        AnimalSpeciesDesc[] animalSpeciesDescs = planet.getFloraAndFauna().getAnimalSpecies();
        
        for (int i = 0; i < amount; ++i) {
            Animal a = new Animal(planet, 0, 0, animalSpeciesDescs[r.nextInt(animalSpeciesDescs.length)]);
            int animalX;
            int animalY;
            do {
                animalX = r.nextInt(planet.getWidth());
                animalY = r.nextInt(planet.getHeight());
            } while (!planet.getSurface().isTilePassable(animalX, animalY));
            a.setPos(animalX, animalY);
            planet.getPlanetObjects().add(a);
        }
        
        planet.getFloraAndFauna().addAnimalCount(amount);
    }
}

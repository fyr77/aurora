package ru.game.aurora.world.planet.nature;

import ru.game.aurora.frankenstein.Slick2DFrankensteinImage;
import ru.game.aurora.frankenstein.Slick2DImageFactory;
import ru.game.frankenstein.*;
import ru.game.frankenstein.impl.MonsterPartsLoader;

import java.io.FileNotFoundException;

/**
 * Generates random alien animals
 */
public class AnimalGenerator {

    private MonsterGenerator generator;

    private MonsterGenerationParams params;

    private static AnimalGenerator instance;

    public static void init() throws FileNotFoundException {
        instance = new AnimalGenerator();
    }

    public static AnimalGenerator getInstance() {
        return instance;
    }

    public AnimalGenerator() throws FileNotFoundException {
        ImageFactory imageFactory = new Slick2DImageFactory();
        MonsterPartsSet monsterPartsSet = MonsterPartsLoader.loadFromJSON(imageFactory, getClass().getClassLoader().getResourceAsStream("animal_parts/parts_library.json"));
        generator = new MonsterGenerator(imageFactory, monsterPartsSet);

        params = new MonsterGenerationParams(true, false);
    }



    public void getImageForAnimal(AnimalSpeciesDesc desc) {
        try {
            Monster monster = generator.generateMonster(params);
            desc.setImages(((Slick2DFrankensteinImage)monster.monsterImage).getImpl(), ((Slick2DFrankensteinImage)monster.deadImage).getImpl());
        } catch (FrankensteinException e) {
            System.err.println("Failed to generate monster image");
            e.printStackTrace();
        }
    }

}

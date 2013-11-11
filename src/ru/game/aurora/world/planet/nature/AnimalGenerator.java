package ru.game.aurora.world.planet.nature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.frankenstein.Slick2DFrankensteinImage;
import ru.game.aurora.frankenstein.Slick2DImageFactory;
import ru.game.frankenstein.*;
import ru.game.frankenstein.impl.MonsterPartsLoader;
import ru.game.frankenstein.util.CollectionUtils;
import ru.game.frankenstein.util.ColorUtils;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.HashSet;

/**
 * Generates random alien animals
 */
public class AnimalGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AnimalGenerator.class);

    private MonsterGenerator monsterGenerator;

    private MonsterGenerator plantGenerator;

    private MonsterGenerationParams monsterGenerationParams;

    private MonsterGenerationParams plantGenerationParams;

    private static AnimalGenerator instance;

    public static final Color[] supportedColors = {new Color(0x00697436), new Color(0x00a12e00), new Color(0x00ad5400), new Color(0x005f4d96), new Color(0x00966e00)};

    public static void init() throws FileNotFoundException {
        instance = new AnimalGenerator();
    }

    public static AnimalGenerator getInstance() {
        return instance;
    }

    public AnimalGenerator() throws FileNotFoundException {
        ImageFactory imageFactory = new Slick2DImageFactory();
        MonsterPartsSet monsterPartsSet = MonsterPartsLoader.loadFromJSON(imageFactory, getClass().getClassLoader().getResourceAsStream("animal_parts/parts_library.json"));
        monsterGenerator = new MonsterGenerator(imageFactory, monsterPartsSet);

        MonsterPartsSet plantsPartSet = MonsterPartsLoader.loadFromJSON(imageFactory, getClass().getClassLoader().getResourceAsStream("plant_parts/parts_library.json"));
        plantGenerator = new MonsterGenerator(imageFactory, plantsPartSet);

        monsterGenerationParams = new MonsterGenerationParams(true, false);
        plantGenerationParams = new MonsterGenerationParams(false, false);
        plantGenerationParams.tags = new HashSet<>();
    }


    public void getImageForAnimal(AnimalSpeciesDesc desc) {
        try {
            monsterGenerationParams.colorMap = ColorUtils.createDefault4TintMap(CollectionUtils.selectRandomElement(supportedColors));
            Monster monster = monsterGenerator.generateMonster(monsterGenerationParams);
            desc.setImages(((Slick2DFrankensteinImage) monster.monsterImage).getImpl(), ((Slick2DFrankensteinImage) monster.deadImage).getImpl());
        } catch (FrankensteinException e) {
            logger.error("Failed to generate monster image", e);
        }
    }

    public void getImageForPlant(PlantSpeciesDesc desc) {
        try {
            plantGenerationParams.tags.clear(); //todo: thread-safe?
            plantGenerationParams.tags.add(desc.getMyFlora().getPlantsStyleTag());
            plantGenerationParams.colorMap = desc.getMyFlora().getColorMap();
            desc.setImage(((Slick2DFrankensteinImage) plantGenerator.generateMonster(plantGenerationParams).monsterImage).getImpl());
        } catch (FrankensteinException e) {
            logger.error("Failed to generate plant image", e);
        }
    }

}

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.09.13
 */

package ru.game.aurora.world.planet.nature;

import ru.game.aurora.frankenstein.Slick2DColor;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains all data concerning planet wildlife
 */
public class PlanetFloraAndFauna implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Style name used for generating plant sprites
     */
    private final String plantsStyleTag;

    private final String animalsStyleTag;

    /**
     * Colors that will be used in plants sprites
     */
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private Map<Integer, Slick2DColor> colorMap;
    /**
     * Available animal species descriptions, if any.
     */
    private AnimalSpeciesDesc[] animalSpecies;

    private PlantSpeciesDesc[] plantSpecies;
    
    private int animalCount = 0;
    
    private int maxAnimals = 0;
    
    private int lastLifeUpdate = 0;

    public PlanetFloraAndFauna(String plantsStyleTag, String animalsStyleTag) {
        this.plantsStyleTag = plantsStyleTag;
        this.animalsStyleTag = animalsStyleTag;
    }

    public AnimalSpeciesDesc[] getAnimalSpecies() {
        return animalSpecies;
    }

    public PlantSpeciesDesc[] getPlantSpecies() {
        return plantSpecies;
    }

    public String getPlantsStyleTag() {
        return plantsStyleTag;
    }

    public String getAnimalsStyleTag() {
        return animalsStyleTag;
    }

    public void setSpecies(AnimalSpeciesDesc[] animalSpecies, PlantSpeciesDesc[] plantSpecies) {
        this.plantSpecies = plantSpecies;
        this.animalSpecies = animalSpecies;
    }

    public Map<Integer, Slick2DColor> getColorMap() {
        return colorMap;
    }

    public void setColorMap(Map<Integer, Slick2DColor> colorMap) {
        this.colorMap = colorMap;
    }
    
    public int getAnimalCount() {
        return this.animalCount;
    }
    
    public int getMaxAnimals() {
        return this.maxAnimals;
    }
    
    public void setAnimalCount(int amount) {
        this.animalCount = amount;
    }
    
    public void addAnimalCount(int amount) {
        this.animalCount += amount;
    }
    
    public void setMaxAnimals(int amount) {
        this.maxAnimals = amount;
    }
    
    public int getLastLifeUpdate() {
        return this.lastLifeUpdate;
    }
    
    public void setLastLifeUpdate(int turn) {
        this.lastLifeUpdate = turn;
    }
}

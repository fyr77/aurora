/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.09.13
 */

package ru.game.aurora.world.planet.nature;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Contains all data concerning planet wildlife
 */
public class PlanetFloraAndFauna implements Serializable {
    private static final long serialVersionUID = -73336834077349556L;

    /**
     * Style name used for generating plant sprites
     */
    private final String plantsStyleTag;

    /**
     * Colors that will be used in plants sprites
     */
    private Map<Integer, Color> colorMap;
    /**
     * Available animal species descriptions, if any.
     */
    private AnimalSpeciesDesc[] animalSpecies;

    private PlantSpeciesDesc[] plantSpecies;

    public PlanetFloraAndFauna(String plantsStyleTag) {
        this.plantsStyleTag = plantsStyleTag;
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

    public void setSpecies(AnimalSpeciesDesc[] animalSpecies, PlantSpeciesDesc[] plantSpecies) {
        this.plantSpecies = plantSpecies;
        this.animalSpecies = animalSpecies;
    }

    public Map<Integer, Color> getColorMap() {
        return colorMap;
    }

    public void setColorMap(Map<Integer, Color> colorMap) {
        this.colorMap = colorMap;
    }
}

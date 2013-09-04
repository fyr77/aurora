/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.09.13
 */

package ru.game.aurora.world.planet.nature;

import java.io.Serializable;

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
     * Available animal species descriptions, if any.
     */
    private AnimalSpeciesDesc[] animalSpecies;

    private PlantSpeciesDesc[] plantSpecies;

    public PlanetFloraAndFauna(String plantsStyleTag, PlantSpeciesDesc[] plantSpecies, AnimalSpeciesDesc[] animalSpecies) {
        this.plantsStyleTag = plantsStyleTag;
        this.plantSpecies = plantSpecies;
        this.animalSpecies = animalSpecies;
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
}

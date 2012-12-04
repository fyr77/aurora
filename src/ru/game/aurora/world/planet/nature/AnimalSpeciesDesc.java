/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 16:43
 */
package ru.game.aurora.world.planet.nature;


import ru.game.aurora.world.planet.Planet;

/**
 * Represents common data about animal species: its name, look, behaviour, home planet, research progress for this animal etc
 */
public class AnimalSpeciesDesc {
    private Planet homePlanet;

    private String spriteName;

    private boolean isCarnivorous;

    private boolean isHerbivorous;

    private int hp;

    private int damage;

    public static enum Behaviour {
        /**
         * Will not react on attacks or will run away
         */
        PASSIVE,
        /**
         * Will not react on landing party until attacked
         */
        SELF_DEFENSIVE,
        /**
         * Will attack any human on sight
         */
        AGGRESSIVE
    }

    private Behaviour behaviour;


    // research attributes

    /**
     * True if animal was stunned and brought to ship alive
     */
    private boolean aliveExamined = false;

    /**
     * True if dead animal was brought to ship and examined
     */
    private boolean outopsyMade = false;

    public AnimalSpeciesDesc(Planet homePlanet, String spriteName, boolean carnivorous, boolean herbivorous, int hp, int damage, Behaviour behaviour) {
        this.homePlanet = homePlanet;
        this.spriteName = spriteName;
        isCarnivorous = carnivorous;
        isHerbivorous = herbivorous;
        this.hp = hp;
        this.damage = damage;
        this.behaviour = behaviour;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }
}

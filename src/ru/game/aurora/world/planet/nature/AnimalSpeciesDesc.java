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

    private String name;

    private Planet homePlanet;

    private String spriteName;

    private String deadSpriteName;

    private boolean isCarnivorous;

    private boolean isHerbivorous;

    private int hp;

    /**
     * Turns between animal moves
     */
    private int speed;

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

    public AnimalSpeciesDesc(Planet homePlanet, String name, String spriteName, String deadSpriteName, boolean carnivorous, boolean herbivorous, int hp, int damage, int speed, Behaviour behaviour) {
        this.homePlanet = homePlanet;
        this.name = name;
        this.deadSpriteName = deadSpriteName;
        this.spriteName = spriteName;
        isCarnivorous = carnivorous;
        isHerbivorous = herbivorous;
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.behaviour = behaviour;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public String getDeadSpriteName() {
        return deadSpriteName;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public boolean isOutopsyMade() {
        return outopsyMade;
    }

    public int getSpeed() {
        return speed;
    }
}

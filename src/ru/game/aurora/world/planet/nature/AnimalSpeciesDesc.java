/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 16:43
 */
package ru.game.aurora.world.planet.nature;


import org.newdawn.slick.Image;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.BasePlanet;

import java.io.Serializable;

/**
 * Represents common data about animal species: its name, look, behaviour, home planet, research progress for this animal etc
 */
public class AnimalSpeciesDesc implements Serializable {

    private static final long serialVersionUID = 8842795492227344987L;

    private String name;

    private BasePlanet homePlanet;

    private boolean isCarnivorous;

    private boolean isHerbivorous;

    private int hp;

    /**
     * Turns between animal moves
     */
    private int speed;

    private LandingPartyWeapon weapon;

    private transient Image image;

    private transient Image deadImage;

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

    public AnimalSpeciesDesc(BasePlanet homePlanet, String name, boolean carnivorous, boolean herbivorous, int hp, LandingPartyWeapon weapon, int speed, Behaviour behaviour) {
        this.homePlanet = homePlanet;
        this.name = name;
        this.weapon = weapon;
        isCarnivorous = carnivorous;
        isHerbivorous = herbivorous;
        this.hp = hp;
        this.speed = speed;
        this.behaviour = behaviour;
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

    public LandingPartyWeapon getWeapon() {
        return weapon;
    }

    public Image getImage() {
        return image;
    }

    public Image getDeadImage() {
        return deadImage;
    }

    public void setImages(Image img, Image deadImg)
    {
        image = img;
        deadImage = deadImg;
    }
}

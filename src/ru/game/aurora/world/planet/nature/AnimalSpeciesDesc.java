/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 16:43
 */
package ru.game.aurora.world.planet.nature;


import org.newdawn.slick.Image;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.Planet;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents common data about animal species: its name, look, behaviour, home planet, research progress for this animal etc
 */
public class AnimalSpeciesDesc implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String name;

    private final Planet homePlanet;

    private final Set<AnimalModifier> modifiers;

    private final int hp;

    /**
     * Turns between animal moves
     */
    private final int speed;

    private final LandingPartyWeapon weapon;

    private transient Image image;

    private transient Image deadImage;

    private boolean canBePickedUp = true;

    // animals can not be friendly
    public static final MonsterBehaviour[] animalBehaviours = new MonsterBehaviour[]{MonsterBehaviour.PASSIVE, MonsterBehaviour.SELF_DEFENSIVE, MonsterBehaviour.AGGRESSIVE};

    private final MonsterBehaviour behaviour;


    // research attributes

    /**
     * True if animal was stunned and brought to ship alive
     */
    private boolean aliveExamined = false;

    /**
     * True if dead animal was brought to ship and examined
     */
    private final boolean outopsyMade = false;

    private int armor;

    public AnimalSpeciesDesc(Planet homePlanet
            , String name
            , boolean carnivorous
            , boolean herbivorous
            , int hp
            , LandingPartyWeapon weapon
            , int speed
            , MonsterBehaviour behaviour
            , Set<AnimalModifier> modifiers
    ) {
        this.homePlanet = homePlanet;
        this.name = name;
        this.weapon = weapon;
        this.hp = hp;
        this.speed = speed;
        this.behaviour = behaviour;
        this.modifiers = modifiers;
    }

    public MonsterBehaviour getBehaviour() {
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

    public Set<AnimalModifier> getModifiers() {
        return modifiers;
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

    public void setImages(Image img, Image deadImg) {
        image = img;
        deadImage = deadImg;
    }

    public Planet getHomePlanet() {
        return homePlanet;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public boolean isCanBePickedUp() {
        return canBePickedUp;
    }

    public void setCanBePickedUp(boolean canBePickedUp) {
        this.canBePickedUp = canBePickedUp;
    }
}

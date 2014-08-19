package ru.game.aurora.world.planet.nature;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 08.01.14
 * Time: 22:46
 */
public enum AnimalModifier {
    LARGE(0.1),
    SMALL(0.1),
    FAST(0.2),
    ARMOR(0.2),
    SNIPER(0.3),
    TOUGH(0.3),
    REGEN(0.1);
    // probability of this modifier to apply, used in ProbabilitySet
    public final double weight;

    private AnimalModifier(double weight) {
        this.weight = weight;
    }


}

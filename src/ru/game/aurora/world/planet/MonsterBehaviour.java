package ru.game.aurora.world.planet;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 15.08.14
 * Time: 23:21
 */
public enum MonsterBehaviour {
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
    AGGRESSIVE,

    /**
     * Will attack monsters with AGGRESSIVE behaviour
     */
    FRIENDLY
}

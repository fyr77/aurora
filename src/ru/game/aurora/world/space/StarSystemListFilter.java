package ru.game.aurora.world.space;

/**
 * Used for filtering lists of star systems.
 * Returns ok if system is suitable
 */
public interface StarSystemListFilter {
    boolean filter(StarSystem ss);
}

package ru.game.aurora.world.space.filters;

import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;

/**
 * For selecting star systems with at least one planet with atmosphere and life
 */
public class HasPlanetWithLifeFilter implements StarSystemListFilter {

    public static final Planet getPlanetWithLife(StarSystem ss) {
        for (BasePlanet p : ss.getPlanets()) {
            if (p instanceof Planet) {
                if (p.hasLife()) {
                    return (Planet) p;
                }

                for (BasePlanet satellite : p.getSatellites()) {
                    if (satellite instanceof Planet && satellite.hasLife()) {
                        return (Planet) satellite;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean filter(StarSystem ss) {
        return getPlanetWithLife(ss) != null;
    }
}

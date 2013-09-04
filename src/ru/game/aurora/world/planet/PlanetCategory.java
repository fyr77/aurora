/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:01
 */
package ru.game.aurora.world.planet;

public enum PlanetCategory {

    PLANET_ROCK(SurfaceTypes.WATER, SurfaceTypes.DIRT, SurfaceTypes.ROCKS, SurfaceTypes.STONES),

    PLANET_ICE(SurfaceTypes.WATER, SurfaceTypes.ICE, SurfaceTypes.STONES, SurfaceTypes.ROCKS, SurfaceTypes.SNOW),

    PLANET_WATER(SurfaceTypes.WATER, SurfaceTypes.STONES, SurfaceTypes.DIRT);

    private PlanetCategory(Byte... availableSurfaceTypes) {
        this.availableSurfaceTypes = availableSurfaceTypes;
    }

    // not byte, as it is used to call generic method selectRandomElement()
    public final Byte[] availableSurfaceTypes;

}

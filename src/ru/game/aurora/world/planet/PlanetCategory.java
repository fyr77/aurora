/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:01
 */
package ru.game.aurora.world.planet;

public enum PlanetCategory
{
    PLANET_ROCK(new byte[] {SurfaceTypes.DIRT, SurfaceTypes.ROCKS}),

    PLANET_ICE(new byte[] {SurfaceTypes.ICE, SurfaceTypes.ROCKS});

    PlanetCategory(byte[] availableSurfaces) {
        this.availableSurfaces = availableSurfaces;
    }

    public byte[] availableSurfaces;

}

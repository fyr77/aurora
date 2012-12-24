/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 20:11
 */
package ru.game.aurora.world.space;

public class HomeworldGenerator {

    public static StarSystem generateGardenerHomeworld(int x, int y, int maxSizeX, int maxSizeY) {
        return GalaxyMap.generateRandomStarSystem(x, y, maxSizeX, maxSizeY);
    }

    public static StarSystem generateKliskHomeworld(int x, int y, int maxSizeX, int maxSizeY) {
        return GalaxyMap.generateRandomStarSystem(x, y, maxSizeX, maxSizeY);
    }
}

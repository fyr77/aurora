/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;

public class GalaxyMapWidget
{

    private World world;


    private final GalaxyMapScreen galaxyMapScreen = new GalaxyMapScreen();

    public GalaxyMapWidget(final World world) {

    }


    public void update() {
        StringBuilder sb = new StringBuilder("Ship status:\n");
        final Ship ship = world.getPlayer().getShip();
        sb.append("\t Scientists: ").append(ship.getScientists()).append("\n");
        sb.append("\t Engineers: ").append(ship.getEngineers()).append("\n");
        sb.append("\t Military: ").append(ship.getMilitary()).append("\n");
        sb.append("Resources: ").append(world.getPlayer().getResourceUnits()).append("\n");
        sb.append("Ship coordinates: [").append(ship.getX()).append(", ").append(ship.getY()).append("]");

    }


}

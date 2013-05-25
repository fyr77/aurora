/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.World;

public class GalaxyMapController implements ScreenController {

    private World world;


    public GalaxyMapController(final World world) {

    }


    public void update() {
      /*  StringBuilder sb = new StringBuilder("Ship status:\n");
        final Ship ship = world.getPlayer().getShip();
        sb.append("\t Scientists: ").append(ship.getScientists()).append("\n");
        sb.append("\t Engineers: ").append(ship.getEngineers()).append("\n");
        sb.append("\t Military: ").append(ship.getMilitary()).append("\n");
        sb.append("Resources: ").append(world.getPlayer().getResourceUnits()).append("\n");
        sb.append("Ship coordinates: [").append(ship.getX()).append(", ").append(ship.getY()).append("]");*/

    }


    @Override
    public void bind(Nifty nifty, Screen screen) {

    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }
}

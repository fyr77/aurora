package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 17:14
 */

public class LabUpgrade implements ShipUpgrade
{

    static final int size = Configuration.getIntProperty("upgrades.lab.crew");

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.setMaxScientists(ship.getMaxScientists() + size);
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        ship.setMaxScientists(ship.getMaxScientists() - size);
    }

    @Override
    public int getSpace() {
        return Configuration.getIntProperty("upgrades.lab.size");
    }
}

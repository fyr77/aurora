package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 17:34
 */
public class BarracksUpgrade extends ShipUpgrade {
    static final int size = Configuration.getIntProperty("upgrades.barrack.crew");

    private static final long serialVersionUID = 1L;

    public BarracksUpgrade() {
        super("barracks_upgrade", "barracks_module", "upgrades");
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.setMaxMilitary(ship.getMaxMilitary() + size);
        world.onCrewChanged();
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        ship.setMaxMilitary(ship.getMaxMilitary() - size);
        world.onCrewChanged();
    }

    @Override
    public int getSpace() {
        return Configuration.getIntProperty("upgrades.barrack.size");
    }
}

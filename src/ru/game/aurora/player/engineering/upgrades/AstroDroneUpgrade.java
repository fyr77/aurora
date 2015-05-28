package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.AstroProbe;

/**
 * Allows launching of drones that collect astro data
 */
public class AstroDroneUpgrade extends ShipUpgrade {

    private static final long serialVersionUID = 1L;

    public AstroDroneUpgrade() {
        super("astrodrone_hangar_upgrade", "astrodrone_hangar_module", "upgrades");
    }

    @Override
    public void onInstalled(World world, Ship ship) {

    }

    @Override
    public void onRemoved(World world, Ship ship) {

    }

    @Override
    public int getSpace() {
        return 10;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public boolean canBeUsedNow(World world) {
        return world.getCurrentStarSystem() != null && world.getPlayer().getResourceUnits() > Configuration.getIntProperty("upgrades.astroprobe.price");
    }

    @Override
    public void onUse(World world) {
        int price = Configuration.getIntProperty("upgrades.astroprobe.price");
        if (world.getPlayer().getResourceUnits() < price) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_resources"));
            return;
        }

        world.getPlayer().changeResource(world, Resources.RU, -price);
        AstroProbe probe = new AstroProbe(world);
        probe.setPos(world.getPlayer().getShip().getX() - 1, world.getPlayer().getShip().getY());
        world.getCurrentStarSystem().getObjects().add(probe);
        GameLogger.getInstance().logMessage(Localization.getText("gui", "space.astroprobe.launched"));
    }
}

package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.common.Drawable;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.World;

/**
 * Created by on 27.08.2015.
 * Increases max ship free space
 */
public class ShipSpaceUpgrade extends EarthUpgrade
{
    private int amount;

    public ShipSpaceUpgrade(String id, Drawable drawable, int value) {
        super(id, drawable, value);
    }

    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getPlayer().getShip().addFreeSpace(amount);
    }
}

package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.common.Drawable;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.World;

/**
 * Increases max ship hp by given amount
 */
public class ShipHullUpgrade extends EarthUpgrade
{
    private int amount;

    public ShipHullUpgrade(String id, Drawable drawable, int value) {
        super(id, drawable, value);
    }

    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getPlayer().getShip().changeMaxHull(amount);
    }
}

package ru.game.aurora.world.planet;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.research.Geodata;
import ru.game.aurora.world.World;

/**
 * Item used for selling geodata to traders
 */
public class GeodataInventoryItem extends SellOnlyInventoryItem {
    public GeodataInventoryItem() {
        super("research"
                , "cartography.geodata_item"
                , new Drawable("cartography_research")
                , Configuration.getDoubleProperty("trade.geodata_price"), false);
    }

    @Override
    public double getPrice() {
        return super.getPrice() * Geodata.getPriceMultiplier(World.getWorld());
    }
}

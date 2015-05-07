package ru.game.aurora.world.space;

import ru.game.aurora.common.Drawable;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;

/**
 * Items that are dropped from destroyed alien ship
 */
public class ShipLootItem extends SellOnlyInventoryItem
{

    public enum Type
    {
        COMPUTERS("loot.computers", 4),
        WEAPONS("loot.weaponry", 6),
        ENERGY("loot.energy", 5),
        MATERIALS("loot.materials", 4),
        GOODS("loot.goods", 3);

        public final String id;

        public final int price;

        Type(String id, int price) {
            this.id = id;
            this.price = price;
        }
    }

    private static int getRaceIdx(String raceName)
    {
        switch (raceName) {
            case KliskGenerator.NAME:
                return 0;
            case RoguesGenerator.NAME:
                return 1;
            case ZorsanGenerator.NAME:
                return 2;
            case BorkGenerator.NAME:
                return 3;
            default:
                throw new IllegalArgumentException("Unsupported race " + raceName);
        }
    }

    public ShipLootItem(Type type, String race) {
        super("research", type.id, new Drawable("alien_space_drop_items", getRaceIdx(race), type.ordinal()), type.price, false);
    }

    public ShipLootItem(Type type, Faction race) {
        this(type, race.getName());
    }

    @Override
    public void onReceived(World world, int amount) {
        world.getPlayer().getInventory().add(this, amount);
        ResearchProjectDesc r = world.getResearchAndDevelopmentProjects().getResearchProjects().remove(id);
        if (r != null) {
            world.getPlayer().getResearchState().addNewAvailableProject(r);
        }

        if (id.contains("goods")) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "loot.goods", "news"));
        }
    }

    @Override
    public void onLost(World world, int amount) {
        world.getPlayer().getInventory().remove(this, amount);
    }
}

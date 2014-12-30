package ru.game.aurora.player;

import org.newdawn.slick.Image;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.*;

/**
 * Special items that can only be sold to aliens
 */
public class SellOnlyInventoryItem extends ItemWithTextAndImage implements InventoryItem
{
    private static final long serialVersionUID = 1L;

    private final double price;

    private final String localizationGroup;

    // if set, can not be sold to these factions
    private Set<String> factionFilter;

    public SellOnlyInventoryItem(String localizationGroup, String id, Drawable drawable, double price, String... factionFilter) {
        super(id, drawable);
        this.price = price;
        this.localizationGroup = localizationGroup;
        if (factionFilter.length > 0) {
            this.factionFilter = new HashSet<>();
            Collections.addAll(this.factionFilter, factionFilter);
        }
    }

    @Override
    public String getName() {
        return super.getLocalizedName(localizationGroup);
    }

    @Override
    public Image getImage() {
        return super.getDrawable().getImage();
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void onReturnToShip(World world, int amount) {

    }

    @Override
    public boolean isDumpable() {
        return false;
    }

    @Override
    public boolean isUsable() {
        return false;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean canBeSoldTo(Faction faction) {
        return factionFilter == null || !factionFilter.contains(faction.getName());
    }
}

package ru.game.aurora.world.planet;

import ru.game.aurora.world.SurfaceLootObject;

/**
 * Created by Егор on 15.09.2015.
 * Inventory item that can be picked up
 */
public class PickableInventoryItem extends SurfaceLootObject {
    private InventoryItem item;

    public PickableInventoryItem(InventoryItem item) {
        super(0, 0);
        this.item = item;
    }

    public PickableInventoryItem(int x, int y, InventoryItem item) {
        super(x, y);
        this.item = item;
    }

    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    protected InventoryItem getLootItem() {
        return item;
    }
}

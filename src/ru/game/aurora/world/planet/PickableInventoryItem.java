package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.SurfaceLootObject;
import ru.game.aurora.world.World;

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

    @Override
    public String getName() {
        return item.getName();
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        if (drawable == null || drawable.getImage() == null) {
            drawable = new Drawable(item.getImage());
        }
        super.draw(container, g, camera, world);
    }
}

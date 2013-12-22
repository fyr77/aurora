package ru.game.aurora.world.planet;

import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.InventoryController;
import ru.game.aurora.world.World;

/**
 * Date: 21.12.13
 * Time: 10:55
 */
public abstract class UsableItem implements InventoryItem {
    private static final long serialVersionUID = 6868775045575998500L;

    public void useIt(World world, int amount) {
        world.getPlayer().getLandingParty().getInventory().setCount(this, amount - 1);
        GUI.getInstance().getNifty().findScreenController(InventoryController.class.getCanonicalName()).onStartScreen();
    }
}

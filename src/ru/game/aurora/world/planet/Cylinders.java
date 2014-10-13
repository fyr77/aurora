package ru.game.aurora.world.planet;

import org.newdawn.slick.Image;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;

/**
 * Date: 22.12.13
 * Time: 8:35
 */
public class Cylinders extends UsableItem {
    private static final long serialVersionUID = -8290168486132537828L;

    @Override
    public void useIt(World world, int amount) {
        world.getPlayer().getLandingParty().refillOxygen();
        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.refill_oxygen"));
        super.useIt(world, amount);
    }

    @Override
    public String getName() {
        return "Oxygen";
    }

    @Override
    public Image getImage() {
        return ResourceManager.getInstance().getImage("oxygen_tank");
    }

    @Override
    public double getPrice() {
        return 0;
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
        return true;
    }
}

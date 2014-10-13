package ru.game.aurora.world.planet;

import org.newdawn.slick.Image;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;

/**
 * Date: 21.12.13
 * Time: 11:00
 */
public class MedPack extends UsableItem {
    private static final long serialVersionUID = 6262599585379560216L;

    private static final int healAmount = Configuration.getIntProperty("medpack.heal");

    @Override
    public void useIt(World world, int amount) {
        world.getPlayer().getLandingParty().addHP(world, healAmount);
        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.medpack_used"));
        super.useIt(world, amount);
    }

    @Override
    public String getName() {
        return "medpack";
    }

    @Override
    public Image getImage() {
        return ResourceManager.getInstance().getImage("medpack");
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

package ru.game.aurora.world.space.earth;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.05.14
 * Time: 18:09
 */
public class EarthUpgradeUnlocker extends GameEventListener
{
    private static final long serialVersionUID = 3886247909156678459L;

    private int prevTechValue = 0;

    @Override
    public boolean onReturnToEarth(World world) {
        final int technologyLevel = world.getPlayer().getEarthState().getTechnologyLevel();

        if (technologyLevel > 250 && prevTechValue <= 250) {

            // unlock missiles
            WeaponUpgrade upgrade = new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("humanity_missiles"));
            world.getPlayer().getEarthState().getAvailableUpgrades().add(upgrade);
            world.addOverlayWindow(Dialog.loadFromFile("missiles_unlocked.json"));
        }


        prevTechValue = technologyLevel;
        return false;
    }
}

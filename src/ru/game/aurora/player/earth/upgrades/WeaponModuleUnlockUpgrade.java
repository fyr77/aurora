package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.world.World;

/**
 * Makes new weapon module available at shipyard
 */
public class WeaponModuleUnlockUpgrade extends EarthUpgrade {
    private String weaponId;

    @Override
    public void unlock(World world) {
        super.unlock(world);
        WeaponUpgrade upgrade = new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity(weaponId));
        world.getPlayer().getEarthState().getAvailableUpgrades().add(upgrade);

    }

}

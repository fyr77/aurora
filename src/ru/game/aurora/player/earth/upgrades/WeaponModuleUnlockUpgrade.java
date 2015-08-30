package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.world.World;

/**
 * Created by Егор on 29.08.2015.
 * Makes new weapon module available at shipyard
 */
public class WeaponModuleUnlockUpgrade extends EarthUpgrade {
    private String weaponId;

    @Override
    public void use(World world, int variant) {
        super.use(world, variant);

        WeaponUpgrade upgrade = new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity(weaponId));
        world.getPlayer().getEarthState().getAvailableUpgrades().add(upgrade);

    }

}

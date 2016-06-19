package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponDesc;

/**
 * Created by User on 19.06.2016.
 * Unlocks a given crew weapon and puts it to player ship inventory
 */
public class CrewWeaponUnlockUpgrade extends EarthUpgrade {
    private String weaponId;

    @Override
    public void unlock(World world) {
        super.unlock(world);
        WeaponDesc upgrade = ResourceManager.getInstance().getWeapons().getEntity(weaponId);
        world.getPlayer().getInventory().add(upgrade);
    }
}

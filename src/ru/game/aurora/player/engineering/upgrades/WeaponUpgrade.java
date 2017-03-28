package ru.game.aurora.player.engineering.upgrades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.equip.WeaponInstance;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 16:52
 */

public class WeaponUpgrade extends ShipUpgrade {
    private static final Logger logger = LoggerFactory.getLogger(WeaponUpgrade.class);

    private static final long serialVersionUID = 7410869118141240436L;

    private final WeaponDesc weaponDesc;

    public WeaponUpgrade(WeaponDesc weaponDesc) {
        super(weaponDesc.getId(), weaponDesc.getDrawable(), "weapons");
        this.weaponDesc = weaponDesc;
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.getWeapons().add(new WeaponInstance(weaponDesc));
        logger.info("Installing new weapon " + weaponDesc.getId());
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        logger.info("Removing weapon " + weaponDesc.getId());
        for (Iterator<WeaponInstance> iterator = ship.getWeapons().iterator(); iterator.hasNext(); ) {
            WeaponInstance sw = iterator.next();
            if (sw.getWeaponDesc().equals(weaponDesc)) {
                iterator.remove();
                return;
            }
        }
        logger.warn("Failed to remove weapon, as it is not installed on a ship");
    }

    @Override
    public int getSpace() {
        return weaponDesc.size;
    }

    @Override
    public String getLocalizedDescription() {
        return super.getLocalizedDescription() + '\n' + '\n' + "RNG: " + weaponDesc.getRange() + ", DMG: " + weaponDesc.getDamageInfo() + ", RLD:" + weaponDesc.getReloadTurns();
    }

    @Override
    public String toString() {
        return "WeaponUpgrade{" +
                "weapon=" + weaponDesc.getId() +
                '}';
    }

    public WeaponDesc getWeaponDesc() {
        return weaponDesc;
    }
}

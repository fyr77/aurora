package ru.game.aurora.player.engineering.upgrades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.equip.StarshipWeaponDesc;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 16:52
 */

public class WeaponUpgrade extends ShipUpgrade
{
    private static final Logger logger = LoggerFactory.getLogger(WeaponUpgrade.class);

    private static final long serialVersionUID = 7410869118141240436L;

    private final StarshipWeaponDesc weaponDesc;

    public WeaponUpgrade(StarshipWeaponDesc weaponDesc) {
        super(weaponDesc.id, weaponDesc.image, "weapons");
        this.weaponDesc = weaponDesc;
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.getWeapons().add(new StarshipWeapon(weaponDesc, StarshipWeapon.MOUNT_ALL));
        logger.info("Installing new weapon " + weaponDesc.getId());
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        logger.info("Removing weapon " + weaponDesc.getId());
        for (Iterator<StarshipWeapon> iterator = ship.getWeapons().iterator(); iterator.hasNext(); ) {
            StarshipWeapon sw = iterator.next();
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
        StringBuilder sb = new StringBuilder(super.getLocalizedDescription());
        sb.append('\n').append('\n');
        sb.append("RNG: ").append(weaponDesc.range).append(", DMG: ").append(weaponDesc.damage).append(", RLD:").append(weaponDesc.reloadTurns);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "WeaponUpgrade{" +
                "weapon=" + weaponDesc.getId() +
                '}';
    }
}

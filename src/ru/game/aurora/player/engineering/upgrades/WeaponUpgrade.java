package ru.game.aurora.player.engineering.upgrades;

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

public class WeaponUpgrade extends ShipUpgrade {
    private static final long serialVersionUID = 7410869118141240436L;

    private final StarshipWeaponDesc weaponDesc;

    public WeaponUpgrade(StarshipWeaponDesc weaponDesc) {
        super(weaponDesc.id, weaponDesc.image, "weapons");
        this.weaponDesc = weaponDesc;
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.getWeapons().add(new StarshipWeapon(weaponDesc, StarshipWeapon.MOUNT_ALL));
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        for (Iterator<StarshipWeapon> iterator = ship.getWeapons().iterator(); iterator.hasNext(); ) {
            StarshipWeapon sw = iterator.next();
            if (sw.getWeaponDesc().equals(weaponDesc)) {
                iterator.remove();
                break;
            }
        }
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
}

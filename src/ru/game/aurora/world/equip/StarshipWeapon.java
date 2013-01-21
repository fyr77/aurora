/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 13:13
 */
package ru.game.aurora.world.equip;


import java.io.Serializable;

public class StarshipWeapon implements Serializable {
    private final StarshipWeaponDesc weaponDesc;

    private int reloadTimeLeft;

    private final int mountPosition;

    public static final int MOUNT_FORE = 1 << 1;

    public static final int MOUNT_AFT = 1 << 2;

    public static final int MOUNT_LEFT = 1 << 2;

    public static final int MOUNT_RIGHT = 1 << 2;

    public static final int MOUNT_ALL = MOUNT_AFT | MOUNT_FORE | MOUNT_LEFT | MOUNT_RIGHT;

    public StarshipWeapon(StarshipWeaponDesc weaponDesc, int mountPosition) {
        this.weaponDesc = weaponDesc;
        this.mountPosition = mountPosition;
    }

    public StarshipWeaponDesc getWeaponDesc() {
        return weaponDesc;
    }

    public int getReloadTimeLeft() {
        return reloadTimeLeft;
    }

    public int getMountPosition() {
        return mountPosition;
    }

    public void setReloadTimeLeft(int reloadTimeLeft) {
        this.reloadTimeLeft = reloadTimeLeft;
    }
}

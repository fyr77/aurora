/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 13:13
 */
package ru.game.aurora.world.equip;


import java.io.Serializable;

public class WeaponInstance implements Serializable {
    private static final long serialVersionUID = 2L;

    private final WeaponDesc weaponDesc;

    private int reloadTimeLeft;

    public WeaponInstance(WeaponDesc weaponDesc) {
        this.weaponDesc = weaponDesc;
    }

    public WeaponDesc getWeaponDesc() {
        return weaponDesc;
    }

    public int getReloadTimeLeft() {
        return reloadTimeLeft;
    }

    public void fire() {
        reloadTimeLeft = weaponDesc.getReloadTurns() + 1; // +1 because reload is called at same frame as shooting
    }

    public void reload() {
        if (reloadTimeLeft > 0) {
            reloadTimeLeft--;
        }
    }

    public boolean isReady() {
        return reloadTimeLeft == 0;
    }
}

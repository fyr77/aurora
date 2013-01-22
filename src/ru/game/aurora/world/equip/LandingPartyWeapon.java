/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 14:01
 */

package ru.game.aurora.world.equip;

import java.io.Serializable;

/**
 * Weapon used by landing party.
 * Weapon specifies damage and shooting range
 * Damage is calculated as [party combat strength] * [weapon damage],
 * where combat strength is 1 * number of military + 1/3 * (number of engineers and scientists)
 */
public class LandingPartyWeapon implements Serializable {
    private final int damage;

    private final int range;

    private final String name;

    public LandingPartyWeapon(int damage, int range, String name) {
        this.damage = damage;
        this.range = range;
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public int getRange() {
        return range;
    }

    public String getName() {
        return name;
    }
}

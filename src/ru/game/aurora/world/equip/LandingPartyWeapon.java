/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 14:01
 */

package ru.game.aurora.world.equip;

import ru.game.aurora.application.JsonConfigManager;

import java.io.Serializable;

/**
 * Weapon used by landing party.
 * Weapon specifies damage and shooting range
 * Damage is calculated as [party combat strength] * [weapon damage],
 * where combat strength is 1 * number of military + 1/3 * (number of engineers and scientists)
 */
public class LandingPartyWeapon implements Serializable, JsonConfigManager.EntityWithId {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final int damage;

    private final int range;

    private final String name;

    private final String image;

    public LandingPartyWeapon(String id, int damage, int range, String name, String image) {
        this.id = id;
        this.damage = damage;
        this.range = range;
        this.name = name;
        this.image = image;
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

    @Override
    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }
}

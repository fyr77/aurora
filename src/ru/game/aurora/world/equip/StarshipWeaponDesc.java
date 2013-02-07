/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 13:05
 */
package ru.game.aurora.world.equip;


import java.io.Serializable;

public class StarshipWeaponDesc implements Serializable
{
    private static final long serialVersionUID = -6078375681186450286L;

    public final int damage;

    public final String name;

    public final String desc;

    public final String shotSprite;

    public final int range;

    public final int reloadTurns;

    public StarshipWeaponDesc(int damage, String name, String desc, String shotSprite, int range, int reloadTurns) {
        this.damage = damage;
        this.name = name;
        this.desc = desc;
        this.shotSprite = shotSprite;
        this.range = range;
        this.reloadTurns = reloadTurns;
    }
}

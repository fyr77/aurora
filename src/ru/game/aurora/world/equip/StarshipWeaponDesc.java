/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.13
 * Time: 13:05
 */
package ru.game.aurora.world.equip;


public class StarshipWeaponDesc {
    public final int damage;

    public final String name;

    public final String desc;

    public final int range;

    public final int reloadTurns;

    public StarshipWeaponDesc(int damage, String name, String desc, int range, int reloadTurns) {
        this.damage = damage;
        this.name = name;
        this.desc = desc;
        this.range = range;
        this.reloadTurns = reloadTurns;
    }
}

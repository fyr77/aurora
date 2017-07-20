package ru.game.aurora.world.equip;

import ru.game.aurora.common.Drawable;

/**
 * Created by di Grigio on 04.05.2017.
 */
public class Scanner extends WeaponDesc {

    private static final long serialVersionUID = 1180514568565428762L;

    public Scanner(String id, Drawable drawable, int damage, float damageDeviation, int range, int price, String shotImage, String shotSound, int reloadTurns, String explosionAnimation, String particlesAnimation, int size) {
        super(id, drawable, damage, damageDeviation, range, price, shotImage, shotSound, reloadTurns, explosionAnimation, particlesAnimation, size);
    }
}
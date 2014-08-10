package ru.game.aurora.world;

import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

/**
 * Something that can attack player landing party
 */
public interface IMonster extends GameObject {
    public int getHp();

    public void changeHp(int amount);

    public int getSpeed();

    public LandingPartyWeapon getWeapon();

    public AnimalSpeciesDesc.Behaviour getBehaviour();
}

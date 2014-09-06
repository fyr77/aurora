package ru.game.aurora.world;

import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.planet.MonsterBehaviour;

import java.util.List;

/**
 * Something that can attack, move and die, being controlled by AI
 */
public interface IMonster extends GameObject {
    public int getHp();

    public int getSpeed();

    public List<WeaponInstance> getWeapons();

    public MonsterBehaviour getBehaviour();
}

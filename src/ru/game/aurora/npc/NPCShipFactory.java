package ru.game.aurora.npc;

import ru.game.aurora.world.space.NPCShip;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:22
 */

public interface NPCShipFactory extends Serializable
{
    public NPCShip createShip();
}

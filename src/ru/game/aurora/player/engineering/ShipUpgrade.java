package ru.game.aurora.player.engineering;

import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 16:45
 */
public interface ShipUpgrade extends Serializable
{
    public void onInstalled(World world, Ship ship);

    public void onRemoved(World world, Ship ship);

    public int getSpace();
}

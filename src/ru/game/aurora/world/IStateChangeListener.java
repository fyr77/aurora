package ru.game.aurora.world;

import java.io.Serializable;

public interface IStateChangeListener extends Serializable
{
    public void stateChanged(World world);
}

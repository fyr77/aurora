package ru.game.aurora.world;

import java.io.Serializable;

public interface IStateChangeListener<E> extends Serializable
{
    public void stateChanged(E param);
}

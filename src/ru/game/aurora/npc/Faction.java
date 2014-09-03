package ru.game.aurora.npc;


import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

import java.io.Serializable;

public interface Faction extends Serializable {
    public String getName();

    public boolean isHostileTo(World world, GameObject object);
}

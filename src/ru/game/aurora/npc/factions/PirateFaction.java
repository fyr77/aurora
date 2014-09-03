package ru.game.aurora.npc.factions;

import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

/**
 * Hostile to anyone except other pirates
 */
public class PirateFaction implements Faction {
    private static final long serialVersionUID = 1059155680200575030L;

    public static String NAME = "pirates";

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isHostileTo(World world, GameObject object) {
        return object.getFaction() != this;
    }
}

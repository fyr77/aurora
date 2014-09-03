package ru.game.aurora.npc.factions;

import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

/**
 * These creatures are hostile to anyone
 */
public class FreeForAllFaction implements Faction {
    private static final long serialVersionUID = -906960278770959335L;

    public static String NAME = "freeforall";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isHostileTo(World world, GameObject object) {
        return true;
    }
}

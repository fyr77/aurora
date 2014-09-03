package ru.game.aurora.npc.factions;

import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

/**
 * Faction that is friendly to everyone
 */
public class NeutralFaction implements Faction {
    private static final long serialVersionUID = 4047025729900589536L;

    public static final String NAME = "neutral";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isHostileTo(World world, GameObject object) {
        return false;
    }
}

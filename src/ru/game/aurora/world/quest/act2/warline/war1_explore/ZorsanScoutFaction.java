package ru.game.aurora.world.quest.act2.warline.war1_explore;

import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

/**
 * Created by di Grigio on 01.05.2017.
 */
public class ZorsanScoutFaction implements Faction {

    private static final long serialVersionUID = 8020092476024601126L;
    public static final String NAME = "Humanity zorsan scout";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isHostileTo(World world, GameObject object) {
        switch (object.getFaction().getName()) {

            case HumanityGenerator.NAME:
            case ZorsanGenerator.NAME:
                return false;

            default:
                return true;
        }
    }
}

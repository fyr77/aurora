package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.04.14
 * Time: 17:52
 */
public class RogueBaseEncounter extends GameEventListener
{

    private static final long serialVersionUID = 1L;

    private final double chance;

    private final NPCShip station;

    private boolean isInSystemWithStation = false;

    public RogueBaseEncounter(Dialog dialog) {
        setGroups(EventGroup.ENCOUNTER_SPAWN);
        chance = Configuration.getDoubleProperty("quest.zorsan_rebels.rogue_base_chance");

        station = new NPCShip(3, 3, "zorsan_station", null, new NPC(dialog), "Rebel station", 20);
        station.setStationary(true);
        dialog.addListener(new DialogListener() {

            private static final long serialVersionUID = 5916617109003619719L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                station.setCanBeHailed(false);
            }
        });
        station.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon"), ResourceManager.getInstance().getWeapons().getEntity("zorsan_small_cannon"));
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (!ss.getStar().color.equals(Color.red) || ss.getStar().size != 1) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() > chance) {
            return false;
        }

        // remove all zorsan ships
        for (Iterator<GameObject> iterator = ss.getShips().iterator(); iterator.hasNext(); ) {
            GameObject s = iterator.next();
            if (s.getFaction().getName().equals(ZorsanGenerator.NAME)) {
                iterator.remove();
            }
        }

        ss.setRandomEmptyPosition(station);
        ss.getShips().add(station);
        isInSystemWithStation = true;
        return true;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        if (isInSystemWithStation) {
            ss.getShips().remove(station);
            isAlive = false;
        }
        return false;
    }
}

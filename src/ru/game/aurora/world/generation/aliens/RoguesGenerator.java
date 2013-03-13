/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 14:37
 */

package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

public class RoguesGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = -8911801330633122269L;

    private static class MeetDamagedRogueEvent extends SingleShipEvent
    {

        private static final long serialVersionUID = 4954480685679636543L;

        public MeetDamagedRogueEvent(NPCShip ship) {
            super(0.9, ship);
        }

        @Override
        public void onPlayerEnterStarSystem(World world, StarSystem ss) {
            AlienRace kliskRace = world.getRaces().get("Klisk");

            if (GalaxyMap.getDistance(ss, kliskRace.getHomeworld()) <= kliskRace.getTravelDistance()) {
                // do not spawn this event in Klisk-controlled systems
                return;
            }

            super.onPlayerEnterStarSystem(world, ss);
        }
    }


    @Override
    public void updateWorld(World world) {
        AlienRace rogueRace = new AlienRace("Rogues", null, 5, null);

        world.getRaces().put(rogueRace.getName(), rogueRace);

        // event with meeting a damaged rogue ship asking for help
        NPCShip damagedRogueScout = new NPCShip(0, 0, "rogues_scout_damaged", rogueRace, new NPC(Dialog.loadFromFile("dialogs/encounters/rogues_damaged_scout.json")), "Rogue scout");
        damagedRogueScout.setAi(null);
        damagedRogueScout.setStationary(true);
        world.addListener(new MeetDamagedRogueEvent(damagedRogueScout));
    }
}

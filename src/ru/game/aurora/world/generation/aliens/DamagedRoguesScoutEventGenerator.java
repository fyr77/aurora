package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Player meets a damaged rogues scout in space.
 * Can either help, or destroy it, or sell its coordinates to bork
 */
public class DamagedRoguesScoutEventGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = 1894996386549484298L;

    private static class MeetDamagedRogueEvent extends SingleShipEvent {

        private static final long serialVersionUID = 4954480685679636543L;

        public MeetDamagedRogueEvent(NPCShip ship) {
            super(0.1, ship);
        }

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            AlienRace kliskRace = world.getRaces().get("Klisk");

            return GalaxyMap.getDistance(ss, kliskRace.getHomeworld()) > kliskRace.getTravelDistance() && super.onPlayerEnterStarSystem(world, ss);
        }
    }

    @Override
    public void updateWorld(World world) {

        final AlienRace rogueRace = world.getRaces().get(RoguesGenerator.NAME);

        final NPCShip damagedRogueScout = new NPCShip(0, 0, "rogues_scout_damaged", rogueRace, null, "Rogue scout");
        // event with meeting a damaged rogue ship asking for help
        final Dialog dialog = Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout.json");
        dialog.addListener(new DialogListener() {

            private static final long serialVersionUID = -1975417060573768272L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

                if (returnCode == 0) {
                    return;
                }

                if (returnCode == 100) {
                    world.getGlobalVariables().put("rogues.damaged_scout_found", world.getCurrentStarSystem());
                    return;
                }

                if (world.getPlayer().getResourceUnits() < 5) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_resources"));
                    return;
                }

                world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() - 5);

                if (returnCode == 1) {
                    // player decided to help without reward
                    world.getReputation().setReputation(rogueRace.getName(), HumanityGenerator.NAME, 2);
                    world.getGlobalVariables().remove("rogues.damaged_scout_found");
                } else {
                    // player decided to help for reward
                    world.getPlayer().changeCredits(world, 5);
                    GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.credits_received"), 5));
                    world.getReputation().setReputation(rogueRace.getName(), HumanityGenerator.NAME, 1);
                    world.getGlobalVariables().remove("rogues.damaged_scout_found");
                }

                world.getGlobalVariables().put("rogues.damage_scout_result", "help");
                damagedRogueScout.setSprite("rogues_scout");
                damagedRogueScout.setAi(null);
                damagedRogueScout.setCaptain(new NPC(
                        new Dialog("rogue_damaged_scout.after_help", "no_image",
                                new Statement(0, "", new Reply(0, -1, "end")))
                ));
            }
        });
        damagedRogueScout.setCaptain(new NPC(dialog));
        damagedRogueScout.setAi(null);
        damagedRogueScout.setStationary(true);

        MeetDamagedRogueEvent listener = new MeetDamagedRogueEvent(damagedRogueScout);
        listener.setGroups(GameEventListener.EventGroup.ENCOUNTER_SPAWN);
        world.addListener(listener);
    }
}

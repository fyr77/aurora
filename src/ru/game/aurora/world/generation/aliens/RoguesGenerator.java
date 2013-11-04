/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 14:37
 */

package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.npc.*;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

public class RoguesGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -8911801330633122269L;

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

    private void createDamagedScoutEvent(World world, final AlienRace rogueRace) {
        final NPCShip damagedRogueScout = new NPCShip(0, 0, "rogues_scout_damaged", rogueRace, null, "Rogue scout");
        // event with meeting a damaged rogue ship asking for help
        final Dialog dialog = Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout.json");
        dialog.setListener(new DialogListener() {

            private static final long serialVersionUID = -1975417060573768272L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode) {

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
                    rogueRace.setRelation(world.getPlayer().getShip().getRace(), rogueRace.getRelation(world.getPlayer().getShip().getRace()) + 2);
                    world.getGlobalVariables().remove("rogues.damaged_scout_found");
                } else {
                    // player decided to help for reward
                    world.getPlayer().changeCredits(5);
                    GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.credits_received"), 5));
                    rogueRace.setRelation(world.getPlayer().getShip().getRace(), rogueRace.getRelation(world.getPlayer().getShip().getRace()) + 1);
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

    @Override
    public void updateWorld(World world) {
        Dialog defaultDialog = Dialog.loadFromFile("dialogs/rogues/rogues_frame_dialog.json");
        defaultDialog.setListener(new RoguesMainDialogListener());
        AlienRace rogueRace = new AlienRace("Rogues", "rogues_scout", defaultDialog);
        StarSystem homeworld = HomeworldGenerator.generateRoguesWorld(world, 15, 8, rogueRace);
        homeworld.setQuestLocation(true);
        rogueRace.setHomeworld(homeworld);

        world.getGalaxyMap().addObjectAndSetTile(homeworld, 15, 8);
        world.addListener(new StandardAlienShipEvent(rogueRace));
        world.addListener(new SingleStarsystemShipSpawner(rogueRace.getDefaultFactory(), 0.3, homeworld));

        world.getRaces().put(rogueRace.getName(), rogueRace);
        createDamagedScoutEvent(world, rogueRace);
    }

}

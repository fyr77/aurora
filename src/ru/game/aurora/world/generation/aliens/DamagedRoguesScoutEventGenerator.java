package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.SingleShipEvent;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;
import java.util.Map;

/**
 * Player meets a damaged rogues scout in space.
 * Can either help, or destroy it, or sell its coordinates to bork
 */
public class DamagedRoguesScoutEventGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 1894996386549484298L;

    public static class MeetDamagedRogueEvent extends SingleShipEvent {

        private static final long serialVersionUID = 4954480685679636543L;

        private int counter;

        private StarSystem spawnedStarSystem = null;

        public MeetDamagedRogueEvent(NPCShip ship) {
            super(Configuration.getDoubleProperty("quest.damaged_rogue_scout.chance"), ship);
            setGroups(EventGroup.ENCOUNTER_SPAWN);
        }

        @Override
        public boolean onTurnEnded(World world) {
            if (spawnedStarSystem == null) {
                return false;
            }

            if (world.getGlobalVariables().containsKey("rogues.damage_scout_result")) {
                ship = null;
                return false;
            }

            if (counter++ >= Configuration.getIntProperty("quest.damaged_rogue_scout.max_days")) {
                // player didn't do anything.
                remove(world);
            }
            return false;
        }

        public void remove(World world) {
            spawnedStarSystem.getShips().remove(ship);
            world.getGlobalVariables().remove("rogues.damaged_scout_found");
            ship = null;
            isAlive = false;
        }

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            if (spawnedStarSystem != null) {
                return false;
            }

            boolean spawned = !ss.isQuestLocation() && super.onPlayerEnterStarSystem(world, ss);
            if (spawned) {
                spawnedStarSystem = ss;
            }
            return spawned;
        }

        @Override
        public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
            if (spawnedStarSystem == galaxyMapObject) {
                return Localization.getText("journal", "damaged_rogues_scout.title");
            }
            return null;
        }
    }

    public static void removeScout(World world) {
        StarSystem target = (StarSystem) world.getGlobalVariables().get("rogues.damaged_scout_found");
        world.getGlobalVariables().remove("rogues.damaged_scout_found");
        if (target == null) {
            return;
        }
        for (Iterator<GameObject> iter = target.getShips().iterator(); iter.hasNext(); ) {
            GameObject so = iter.next();
            if (so.getName().equals("Rogue scout")) {
                iter.remove();
                return;
            }
        }
    }

    @Override
    public void updateWorld(World world) {

        final AlienRace rogueRace = (AlienRace) world.getFactions().get(RoguesGenerator.NAME);

        final NPCShip damagedRogueScout = new NPCShip(0, 0, "rogues_scout_damaged", rogueRace, null, "Rogue scout", 2);
        // event with meeting a damaged rogue ship asking for help
        final Dialog initialDialog = Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout.json");
        final Dialog saveByResourceDialog = Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout_repaired.json");


        final DialogListener listener1 = new DialogListener() {

            private static final long serialVersionUID = -1975417060573768272L;


            private void saveByResources(World world) {
                if (world.getPlayer().getResourceUnits() < 10) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_resources"));
                    return;
                }

                world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() - 10);
                world.addOverlayWindow(saveByResourceDialog);

                world.getGlobalVariables().put("rogues.damage_scout_result", "help");
                damagedRogueScout.setSprite("rogues_scout");
                damagedRogueScout.setAi(new LeaveSystemAI());
                damagedRogueScout.setStationary(false);
                damagedRogueScout.setCaptain(new NPC(
                        Dialog.loadFromFile("dialogs/rogues/rogue_damaged_scout.after_help.json")
                ));
            }

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

                if (dialog == initialDialog) {
                    if (returnCode == 1) {
                        saveByResources(world);
                    } else if (returnCode == 2) {
                        saveByShuttle(world);
                    } else {
                        world.getGlobalVariables().put("rogues.damaged_scout_found", world.getCurrentStarSystem());
                    }
                    return;
                }

                if (dialog == saveByResourceDialog) {
                    if (returnCode == 1) {
                        // player decided to help without reward
                        world.getReputation().updateReputation(rogueRace.getName(), HumanityGenerator.NAME, 2);
                        world.getGlobalVariables().remove("rogues.damaged_scout_found");
                    } else {
                        // player decided to help for reward
                        world.getPlayer().changeResource(world, Resources.CREDITS, 5);
                        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.credits_received"), 5));
                        world.getReputation().updateReputation(rogueRace.getName(), HumanityGenerator.NAME, 1);
                        world.getGlobalVariables().remove("rogues.damaged_scout_found");
                    }
                }


            }

            private void saveByShuttle(World world) {
                double boomChance = Configuration.getDoubleProperty("quest.damaged_rogue_scout.boom_chance");
                if (CommonRandom.getRandom().nextDouble() > boomChance) {
                    // successfully saved
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout_shuttle.json"));
                    final ResearchProjectDesc captured_rogues = world.getResearchAndDevelopmentProjects().getResearchProjects().get("captured_rogues");
                    captured_rogues.addListener(new IStateChangeListener<World>() {
                        private static final long serialVersionUID = 1125234617698299511L;

                        @Override
                        public void stateChanged(World world) {
                            world.getPlayer().getJournal().addCodex(new JournalEntry("rogues", "main"));
                        }
                    });
                    world.getPlayer().getResearchState().addNewAvailableProject(captured_rogues);
                    world.getGlobalVariables().put("rogues.damage_scout_result", "help");
                    world.getGlobalVariables().put("rogues.damage_scout_crew_saved", null);
                    world.getReputation().updateReputation(rogueRace.getName(), HumanityGenerator.NAME, 1);
                } else {
                    world.getPlayer().getShip().setEngineers(world.getPlayer().getShip().getEngineers() - 1);
                    world.getPlayer().getShip().setMilitary(world.getPlayer().getShip().getMilitary() - 1);
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/rogues/rogues_damaged_scout_exploded.json"));
                }

                // remove scout and add explosion
                for (Iterator<GameObject> iter = world.getCurrentStarSystem().getShips().iterator(); iter.hasNext(); ) {
                    GameObject so = iter.next();
                    if (so != damagedRogueScout) {
                        continue;
                    }

                    iter.remove();
                    world.getCurrentStarSystem().addEffect(new ExplosionEffect(so.getX(), so.getY(), "ship_explosion", false, true));
                }
                world.getGlobalVariables().remove("rogues.damaged_scout_found");
            }
        };
        initialDialog.addListener(listener1);
        saveByResourceDialog.addListener(listener1);
        damagedRogueScout.setCaptain(new NPC(initialDialog));
        damagedRogueScout.setAi(null);
        damagedRogueScout.setStationary(true);

        MeetDamagedRogueEvent listener = new MeetDamagedRogueEvent(damagedRogueScout);
        listener.setGroups(GameEventListener.EventGroup.ENCOUNTER_SPAWN);
        world.addListener(listener);
    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:10
 */
package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.projects.AdvancedLasers;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceHulk;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Creates quest chain with initial research for brown dwarf radio emission, that is given to player on game startup
 */
public class InitialRadioEmissionQuestGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = -4950686913834019746L;

    private static final class EarthEnergyResearch extends EarthResearch {

        private static final long serialVersionUID = 8367244693901465513L;

        private NPCShip construction;

        private int state = 0;

        public EarthEnergyResearch() {
            super("solar_energy", 100);
        }

        @Override
        protected void onCompleted(World world) {
            AlienRace humanity = (AlienRace) world.getFactions().get(HumanityGenerator.NAME);
            // begin construction of a beacon near the sun
            final EarthState earthState = world.getPlayer().getEarthState();
            if (state == 0) {
                earthState.getMessages().add(new PrivateMessage(
                        world,
                        "news_sender",
                        "icarus_1",
                        "message"
                )
                );
                earthState.getMessages().add(new PrivateMessage(
                        world,
                        "news_sender",
                        "icarus_2"
                        , "news"
                ));


                construction = new NPCShip(0, 1, "earth_construction", humanity, null, "Icarus #1", 25);
                construction.setStationary(true);
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                completed = false;

                targetTurn += 100;
                state = 1;
            } else if (state == 1) {
                // finish construction
                // replace station sprite
                humanity.getHomeworld().getShips().remove(construction);
                construction = new NPCShip(0, 1, "icarus_station", humanity, null, "Icarus #1", 25);
                construction.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("humanity_missiles"));
                construction.setStationary(true);
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                // add messages

                earthState.getMessages().add(new PrivateMessage(
                        world
                        , "news_sender",
                        "icarus_3"
                        , "news"
                ));
                completed = false;
                targetTurn += 20;
                state = 2;
            } else if (state == 2) {
                earthState.getMessages().add(new PrivateMessage(
                        world
                        , "news_sender",
                        "icarus_4"
                        , "news"
                ));
                earthState.updateTechnologyLevel(200);
            }

        }

        @Override
        public void onStarted(World world) {
            super.onStarted(world);
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                    world, "news_sender",
                    "icarus_0"
                    , "news"
            ));
        }
    }

    private static class RoguesStateChanger extends GameEventListener implements DialogListener {
        private final AlienRace rogues;

        private final StarSystem roguesBase;

        private NPCShip roguesFrame;

        private final Dungeon beacon;

        private int turns = -1;

        private int fine = -1;

        private RoguesStateChanger(World world, Dungeon beacon) {
            rogues = (AlienRace) world.getFactions().get(RoguesGenerator.NAME);
            this.roguesBase = rogues.getHomeworld();
            for (GameObject s : roguesBase.getShips()) {
                if (s.getName().equals("Rogues Frame")) {
                    roguesFrame = (NPCShip) s;
                    break;
                }
            }
            this.beacon = beacon;
        }

        private static final long serialVersionUID = 8294376357309852462L;

        @Override
        public boolean onPlayerEnteredDungeon(World world, Dungeon dungeon) {
            if (dungeon == beacon) {
                turns = 500;
            }
            return false;
        }

        @Override
        public boolean onTurnEnded(World world) {
            if (turns < 0) {
                return false;
            }

            if (world.getCurrentDungeon() != null) {
                // do not count turns spent in dungeon
                return false;
            }

            turns--;
            if (turns == 60 && fine < 0) {
                // change rogues default dialog
                Dialog d = Dialog.loadFromFile("dialogs/rogues/search_beacon_attackers.json");
                d.addListener(this);
                ((AlienRace) world.getFactions().get(RoguesGenerator.NAME)).setDefaultDialog(d);
                return true;
            }
            if (turns == 0) {
                if (fine < 0) {
                    // in next star system a group of rogues will be awaiting player ship
                    world.addListener(new GameEventListener() {

                        private static final long serialVersionUID = 4464913526587137850L;

                        @Override
                        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
                            if (ss.isQuestLocation()) {
                                return false;
                            }
                            isAlive = false;

                            NPCShip ship = rogues.getDefaultFactory().createShip(world, RoguesGenerator.SCOUT_SHIP);
                            final Ship playerShip = world.getPlayer().getShip();
                            ship.setPos(playerShip.getX() + 1, playerShip.getY());
                            ss.getShips().add(ship);

                            NPCShip defenceProbe = rogues.getDefaultFactory().createShip(world, RoguesGenerator.PROBE_SHIP);
                            defenceProbe.setPos(playerShip.getX(), playerShip.getY() + 1);
                            ss.getShips().add(defenceProbe);

                            Dialog dialog = Dialog.loadFromFile("dialogs/rogues/court_invitation.json");
                            dialog.addListener(RoguesStateChanger.this);
                            world.addOverlayWindow(dialog);
                            return true;
                        }
                    });


                } else {
                    if (world.getGlobalVariables().containsKey("rogues.fine")) {
                        // player has not payed the fine
                        world.getReputation().setHostile(RoguesGenerator.NAME, HumanityGenerator.NAME);
                        world.getGlobalVariables().put("rogues.beacon_hostile", true);
                        return true;
                    }
                    isAlive = false;
                }
            }
            return false;
        }

        @Override
        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
            if (dialog.getId().equals("search_beacon_attackers") || dialog.getId().equals("court_invitation")) {
                switch (returnCode) {
                    case 200:
                        // player refused to surrender and is now hostile with rogues
                        world.getReputation().setHostile(RoguesGenerator.NAME, HumanityGenerator.NAME);
                        world.getCurrentStarSystem().getReputation().setHostile(RoguesGenerator.NAME, HumanityGenerator.NAME);
                        world.getGlobalVariables().put("rogues.beacon_hostile", true);
                        break;
                    case 100:
                        // player surrenders, transport him to rogues homeworld
                        world.setCurrentRoom(roguesBase);
                        roguesBase.enter(world);
                        world.getPlayer().getShip().setPos(roguesFrame.getX() + 1, roguesFrame.getY());
                        Dialog admiralCourtDialog = Dialog.loadFromFile("dialogs/rogues/admiral_court.json");
                        admiralCourtDialog.addListener(this);
                        world.addOverlayWindow(admiralCourtDialog);
                        break;
                }
            } else if (dialog.getId().equals("admiral_court")) {
                fine = returnCode;
                world.getGlobalVariables().put("rogues.fine", fine);
                turns = 360;
                ((AlienRace) world.getFactions().get(RoguesGenerator.NAME)).setDefaultDialog(Dialog.loadFromFile("dialogs/rogues/rogues_default.json"));
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        // initial research projects and their star system
        StarSystem brownStar = WorldGenerator.generateRandomStarSystem(world, 6, 7);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAtDistance(brownStar, (Positionable) world.getGlobalVariables().get("solar_system"), 10);

        ResearchProjectDesc starInitialResearch = new StarResearchProject(brownStar);
        starInitialResearch.setReport(new ResearchReport("star_research", "brown_dwarf.report"));
        world.getPlayer().getResearchState().addNewAvailableProject(starInitialResearch);
        starInitialResearch.getTargetStarSystems().add(brownStar);
        // add second quest in chain

        brownStar = WorldGenerator.generateRandomStarSystem(world, 12, 12);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        brownStar.setQuestLocation(true);
        world.getGalaxyMap().addObjectAtDistance(brownStar, (Positionable) world.getGlobalVariables().get("solar_system"), 30);

        AlienRace rogues = (AlienRace) world.getFactions().get("Rogues");
        NPCShip defenceProbe = rogues.getDefaultFactory().createShip(world, RoguesGenerator.PROBE_SHIP);
        defenceProbe.setPos(2, 1);
        defenceProbe.setAi(new CombatAI(world.getPlayer().getShip()));
        defenceProbe.setHostile(true);
        brownStar.getShips().add(defenceProbe);
        brownStar.setFirstEnterDialog(Dialog.loadFromFile("dialogs/rogues/rogue_beacon_found.json"));


        Dungeon beaconInternals = new Dungeon(world, new AuroraTiledMap("resources/maps/test.tmx"), brownStar);

        beaconInternals.setPlaylistName("dungeon_invasion");
        final Dialog enterDialog = Dialog.loadFromFile("dialogs/rogues/rogues_beacon_landing.json");
        enterDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -6585091322276759341L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getGlobalVariables().put("rogues_beacon.result", "scanned");
            }
        });
        beaconInternals.setEnterDialog(enterDialog);
        beaconInternals.setSuccessDialog(Dialog.loadFromFile("dialogs/rogues/rogues_beacon_explored.json"));
        final SpaceHulk beacon = new SpaceHulk(1, 1, "Beacon", "rogues_beacon", beaconInternals);
        beaconInternals.getController().addListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = 4018207362752529165L;

            @Override
            public void stateChanged(World world) {
                world.getGlobalVariables().put("rogues_beacon.result", "invaded");
                ExplosionEffect effect = new ExplosionEffect(beacon.getX(), beacon.getY(), "ship_explosion", false, true);
                world.getCurrentStarSystem().addEffect(effect);
                world.getCurrentStarSystem().getShips().remove(beacon);
                world.getPlayer().getInventory().add(new SellOnlyInventoryItem(
                        "items", "rogue_beacon_data", new Drawable("technology_research"), Configuration.getIntProperty("quest.rogues_beacon.price"), true, RoguesGenerator.NAME
                ), 1);
            }
        });
        beacon.setResearchProjectDescs(world.getResearchAndDevelopmentProjects().getResearchProjects().get("beacon"));
        world.addListener(new RoguesStateChanger(world, beaconInternals));
        brownStar.getShips().add(beacon);

        ResearchProjectDesc secondResearch = new StarResearchProject(brownStar);
        secondResearch.setReport(new ResearchReport("star_research", "brown_dwarf_2.report"));
        starInitialResearch.addNextResearch(secondResearch);
        secondResearch.getTargetStarSystems().add(brownStar);

        world.getResearchAndDevelopmentProjects().getEarthResearchProjects().put("solar_energy", new EarthEnergyResearch());
        world.getResearchAndDevelopmentProjects().getResearchProjects().put(secondResearch.getId(), secondResearch);
        AdvancedLasers al = new AdvancedLasers();
        world.getResearchAndDevelopmentProjects().getEngineeringProjects().put(al.getId(), al);

    }
}

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:10
 */
package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.projects.AdvancedLasers;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.RoguesMainDialogListener;
import ru.game.aurora.world.space.*;

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
            // begin construction of a beacon near the sun
            final EarthState earthState = world.getPlayer().getEarthState();
            if (state == 0) {
                earthState.getMessages().add(new PrivateMessage(
                        "icarus_1",
                        "message"
                )
                );
                earthState.getMessages().add(new PrivateMessage(
                        "icarus_2"
                        , "news"
                ));


                final AlienRace humanity = world.getRaces().get("Humanity");
                construction = new NPCShip(0, 1, "earth_construction", humanity, null, "Icarus #1");
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                completed = false;

                targetTurn += 100;
                state = 1;
            } else if (state == 1) {
                // finish construction
                final AlienRace humanity = world.getRaces().get("Humanity");

                // replace station sprite
                humanity.getHomeworld().getShips().remove(construction);
                construction = new NPCShip(0, 1, "icarus_station", humanity, null, "Icarus #1");
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                // add messages

                earthState.getMessages().add(new PrivateMessage(
                        "icarus_3"
                        , "news"
                ));
                completed = false;
                targetTurn += 20;
                state = 2;
            } else if (state == 2) {
                earthState.getMessages().add(new PrivateMessage(
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
                    "icarus_0"
                    , "news"
            ));
        }
    }

    private static class RoguesStateChanger extends GameEventListener implements DialogListener {
        private AlienRace rogues;

        private StarSystem roguesBase;

        private NPCShip roguesFrame;

        private Dungeon beacon;

        private int turns = -1;

        private int fine = -1;

        private RoguesStateChanger(World world, Dungeon beacon) {
            rogues = world.getRaces().get("Rogues");
            this.roguesBase = rogues.getHomeworld();
            for (SpaceObject s : roguesBase.getShips()) {
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
                turns = 100;
            }
            return false;
        }

        @Override
        public boolean onTurnEnded(World world) {
            if (turns < 0) {
                return false;
            }

            turns--;
            if (turns == 60 && fine < 0) {
                // change rogues default dialog
                Dialog d = Dialog.loadFromFile("dialogs/rogues/search_beacon_attackers.json");
                d.setListener(this);
                world.getRaces().get("Rogues").setDefaultDialog(d);
                return true;
            }
            if (turns == 0) {
                if (fine < 0) {
                    // in next star system a group of rogues will be awaiting player ship
                    world.addListener(new GameEventListener() {

                        private static final long serialVersionUID = 4464913526587137850L;

                        @Override
                        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
                            isAlive = false;

                            NPCShip ship = rogues.getDefaultFactory().createShip();
                            final Ship playerShip = world.getPlayer().getShip();
                            ship.setPos(playerShip.getX() + 1, playerShip.getY());
                            ss.getShips().add(ship);

                            NPCShip defenceProbe = new NPCShip(playerShip.getX(), playerShip.getY() + 1, "rogues_probe", rogues, null, "Defence drone");
                            defenceProbe.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), StarshipWeapon.MOUNT_ALL));
                            defenceProbe.setStationary(true);
                            ss.getShips().add(defenceProbe);

                            Dialog dialog = Dialog.loadFromFile("dialogs/rogues/court_invitation.json");
                            dialog.setListener(RoguesStateChanger.this);
                            world.addOverlayWindow(dialog);
                            return true;
                        }
                    });


                } else {
                    if (world.getGlobalVariables().containsKey("rogues.fine")) {
                        // player has not payed the fine
                        world.getRaces().get("Rogues").setRelation(world.getRaces().get("Humanity"), 0);
                        world.getGlobalVariables().put("rogues.beacon_hostile", true);
                        return true;
                    }
                    isAlive = false;
                }
            }
            return false;
        }

        @Override
        public void onDialogEnded(World world, Dialog dialog, int returnCode) {
            if (dialog.getId().equals("search_beacon_attackers") || dialog.getId().equals("court_invitation")) {
                switch (returnCode) {
                    case 200:
                        // player refused to surrender and is now hostile with rogues
                        world.getRaces().get("Rogues").setRelation(world.getRaces().get("Humanity"), 0);
                        world.getGlobalVariables().put("rogues.beacon_hostile", true);
                        break;
                    case 100:
                        // player surrenders, transport him to rogues homeworld
                        world.setCurrentRoom(roguesBase);
                        roguesBase.enter(world);
                        world.getPlayer().getShip().setPos(roguesFrame.getX() + 1, roguesFrame.getY());
                        Dialog admiralCourtDialog = Dialog.loadFromFile("dialogs/rogues/admiral_court.json");
                        admiralCourtDialog.setListener(this);
                        world.addOverlayWindow(admiralCourtDialog);
                        break;
                }
            } else if (dialog.getId().equals("admiral_court")) {
                fine = returnCode;
                world.getGlobalVariables().put("rogues.fine", fine);
                turns = 360;
                Dialog defaultDialog = Dialog.loadFromFile("dialogs/rogues/rogues_frame_dialog.json");
                defaultDialog.setListener(new RoguesMainDialogListener());
                world.getRaces().get("Rogues").setDefaultDialog(defaultDialog);
                roguesFrame.setCaptain(new NPC(defaultDialog));
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        // initial research projects and their star system
        StarSystem brownStar = WorldGenerator.generateRandomStarSystem(world, 6, 7);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 6, 7);

        ResearchProjectDesc starInitialResearch = new StarResearchProject(brownStar);
        starInitialResearch.setReport(new ResearchReport("star_research", "brown_dwarf.report"));
        world.getPlayer().getResearchState().addNewAvailableProject(starInitialResearch);

        // add second quest in chain

        brownStar = WorldGenerator.generateRandomStarSystem(world, 12, 12);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        brownStar.setQuestLocation(true);
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 12, 12);

        AlienRace rogues = world.getRaces().get("Rogues");
        NPCShip defenceProbe = new NPCShip(2, 1, "rogues_probe", rogues, null, "Defence drone");
        defenceProbe.setAi(new CombatAI(world.getPlayer().getShip()));
        defenceProbe.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), StarshipWeapon.MOUNT_ALL));
        defenceProbe.setHostile(true);
        defenceProbe.setStationary(true);
        brownStar.getShips().add(defenceProbe);
        brownStar.setFirstEnterDialog(Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/rogues/rogue_beacon_found.json")));

        Dungeon beaconInternals = new Dungeon(world, new AuroraTiledMap("resources/maps/test.tmx"), brownStar);
        beaconInternals.setEnterDialog(Dialog.loadFromFile("dialogs/rogues/rogue_beacon_landing.json"));
        beaconInternals.setSuccessDialog(Dialog.loadFromFile("dialogs/rogues/rogues_beacon_explored.json"));
        SpaceHulk beacon = new SpaceHulk(1, 1, "Beacon", "rogues_beacon", beaconInternals);
        beacon.setResearchProjectDescs(world.getResearchAndDevelopmentProjects().getResearchProjects().get("beacon"));
        world.addListener(new RoguesStateChanger(world, beaconInternals));
        brownStar.getShips().add(beacon);

        ResearchProjectDesc secondResearch = new StarResearchProject(brownStar);
        secondResearch.setReport(new ResearchReport("star_research", "brown_dwarf_2.report"));
        starInitialResearch.addNextResearch(secondResearch);

        world.getResearchAndDevelopmentProjects().getEarthResearchProjects().put("solar_energy", new EarthEnergyResearch());
        world.getResearchAndDevelopmentProjects().getResearchProjects().put(secondResearch.getId(), secondResearch);
        AdvancedLasers al = new AdvancedLasers();
        world.getResearchAndDevelopmentProjects().getEngineeringProjects().put(al.getId(), al);

    }
}

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
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.projects.AdvancedLasers;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.TILEDMap;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceHulk;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

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
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 12, 12);

        NPCShip defenceProbe = new NPCShip(2, 1, "rogues_probe", world.getRaces().get("Rogues"), null, "Defence drone");
        defenceProbe.setAi(new CombatAI(world.getPlayer().getShip()));
        defenceProbe.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), StarshipWeapon.MOUNT_ALL));
        defenceProbe.setHostile(true);
        defenceProbe.setStationary(true);
        brownStar.getShips().add(defenceProbe);
        brownStar.setFirstEnterDialog(Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/rogue_beacon_found.json")));

        Dungeon beaconInternals = new Dungeon(world, new TILEDMap("resources/maps/test.tmx"), brownStar);
        beaconInternals.setEnterDialog(Dialog.loadFromFile("dialogs/quest/rogue_beacon_landing.json"));
        beaconInternals.setSuccessDialog(Dialog.loadFromFile("dialogs/quest/rogues_beacon_explored.json"));
        SpaceHulk beacon = new SpaceHulk(1, 1, "Beacon", "rogues_beacon", beaconInternals);
        beacon.setResearchProjectDescs(world.getResearchAndDevelopmentProjects().getResearchProjects().get("beacon"));

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

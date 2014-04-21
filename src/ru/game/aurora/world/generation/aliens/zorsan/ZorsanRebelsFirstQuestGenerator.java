/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.04.14
 * Time: 13:43
 */


package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.AlienArtifact;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.*;

import java.util.Map;

/**
 * After some time from successive escape from zorsan homeworld, player meets zorsan ship, that asks him to join rebels
 */
public class ZorsanRebelsFirstQuestGenerator extends GameEventListener
{
    private static final long serialVersionUID = -7613551870732796318L;

    private int starsystemCount = 4;

    public ZorsanRebelsFirstQuestGenerator() {
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation()) {
            return false;
        }
        if (starsystemCount --> 0) {
            return false;
        }

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/rebels/intro/courier_encountered.json"));

        final NPCShip ship = world.getRaces().get(ZorsanGenerator.NAME).getDefaultFactory().createShip(0);
        ship.setRace(null);
        ship.setAi(null);

        Dialog courierDialog = Dialog.loadFromFile("dialogs/zorsan/rebels/intro/courier_dialog.json");
        courierDialog.addListener(new DialogListener()
        {

            private static final long serialVersionUID = 2455530990003907513L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getJournal().addQuestEntries("zorsan_rebels", "start");
                ship.setAi(new LeaveSystemAI());
                ship.setCanBeHailed(false);
            }
        });

        ship.setCaptain(new NPC(courierDialog));

        ss.setRandomEmptyPosition(ship);
        ss.getShips().add(ship);

        return true;
    }

    // makes sure that there is at least 1 red giant starsystem for roaming base
    // prepares planet for intro
    public static class StarsystemGenerator implements WorldGeneratorPart
    {
        private static final long serialVersionUID = 7443772810131150969L;

        private void placeArtifact(World world, StarSystem ss)
        {

            AlienArtifact artifact = new AlienArtifact(10, 20, "builders_pyramid", new ArtifactResearch(new ResearchReport("builders_ruins", "builder_ruins.report")));
            ((Planet)ss.getPlanets()[0]).setNearestFreePoint(artifact, 10, 20);
            ((Planet)ss.getPlanets()[0]).getPlanetObjects().add(artifact);

            world.getGlobalVariables().put("zorsan_rebels.start_coords", ss.getCoordsString());
        }

        @Override
        public void updateWorld(World world) {

            final AlienRace alienRace = world.getRaces().get(ZorsanGenerator.NAME);
            int travelDistance = alienRace.getTravelDistance();

            boolean redGiantFound = false;

            boolean artifactPlaced = false;

            for (GalaxyMapObject gmo : world.getGalaxyMap().getObjects()) {
                if (!StarSystem.class.isAssignableFrom(gmo.getClass())) {
                    continue;
                }

                StarSystem ss = (StarSystem) gmo;

                if (GalaxyMap.getDistance(ss, alienRace.getHomeworld()) > travelDistance) {
                    continue;
                }

                if (ss.isQuestLocation()) {
                    continue;
                }

                if (ss.getStar().color == Color.red && ss.getStar().size == 1) {
                    redGiantFound = true;
                    continue;
                }

                if (redGiantFound && artifactPlaced) {
                    break;
                }

                if (artifactPlaced) {
                    continue;
                }

                if (ss.getPlanets().length < 1) {
                    continue;
                }

                if (!(ss.getPlanets()[0] instanceof Planet)) {
                    continue;
                }
                artifactPlaced = true;
                placeArtifact(world, ss);
            }

            if (!artifactPlaced) {
                StarSystem ss = WorldGenerator.generateRandomStarSystem(world, 10, 10, 3);
                placeArtifact(world, ss);
                world.getGalaxyMap().addObjectAtDistance(ss, alienRace.getHomeworld(), alienRace.getTravelDistance() + 5);
            }

            if (!redGiantFound) {
                StarSystem ss = WorldGenerator.generateRandomStarSystem(new Star(1, Color.red), world, 10, 10, 3);
                world.getGalaxyMap().addObjectAtDistance(ss, alienRace.getHomeworld(), alienRace.getTravelDistance() - 5);
            }
        }
    }
}

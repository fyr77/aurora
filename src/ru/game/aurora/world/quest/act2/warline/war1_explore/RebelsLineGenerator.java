package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.newdawn.slick.Color;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.ContactAI;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;
import ru.game.aurora.world.util.DespawnShipsAfterLeft;

import java.util.Map;

/**
 * Created by di Grigio on 04.04.2017.
 * Part of Act2, War Line, quest 1 - Explore
 */
class RebelsLineGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = -8699229212503596573L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RebelsLineGenerator.class);

    @Override
    public void updateWorld(World world) {
        generateRebelsAgent(world);
    }

    private void generateRebelsAgent(World world) {
        world.addListener(new GameEventListener() {
            private static final long serialVersionUID = 5635161845909338516L;

            // player have two cahnces to contact with agent in different star systems
            private StarSystem firstTrySystem;

            @Override
            public boolean onPlayerEnterStarSystem(World world, StarSystem starSystem) {
                if(firstTrySystem == null){
                    firstTrySystem = starSystem;
                }
                else{
                    this.removeListener(world);
                }

                spawnRebelsAgent(world, starSystem, this);
                return true;
            }
        });
    }

    private void spawnRebelsAgent(final World world, final StarSystem starSystem, final GameEventListener eventListener) {
        Dialog inviteDialog = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/rebels/war1_explore_rebels_invite.json");
        Ship playerShip = world.getPlayer().getShip();

        final NPCShip agentShip = new NPCShip("rebels_agent", playerShip.getX() + 5, playerShip.getY());
        agentShip.setAi(new ContactAI(playerShip, inviteDialog));

        inviteDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -3934145559361798756L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(returnCode == 1){
                    // player accept invite from rebels agent
                    eventListener.removeListener(world);
                    world.getPlayer().getJournal().addQuestEntries("war1_explore", "rebels_agent");
                    world.getGlobalVariables().put("war1_explore_rebels_invite_accepted", true);
                    generateTargetStarSystem(world);
                }

                agentShip.setAi(new LeaveSystemAI());
                agentShip.setCanBeHailed(false);
            }
        });

        starSystem.getShips().add(agentShip);
        world.addListener(new DespawnShipsAfterLeft(agentShip, starSystem));
    }

    private void generateTargetStarSystem(World world) {
        final AlienRace alienRace = (AlienRace) world.getFactions().get(ZorsanGenerator.NAME);

        StarSystem targetSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(alienRace.getHomeworld().getX(), alienRace.getHomeworld().getY(), alienRace.getTravelDistance(), new StarSystemListFilter() {
            @Override
            public boolean filter(StarSystem ss) {
                if(ss.getStar().color.equals(Color.red)){
                    return true;
                }
                else{
                    return false;
                }
            }
        });

        if(targetSystem == null){
            targetSystem = WorldGenerator.generateRandomStarSystem(new Star(1, Color.red), world, 10, 10, 3);
            world.getGalaxyMap().addObjectAtDistance(targetSystem, alienRace.getHomeworld(), alienRace.getTravelDistance() - CommonRandom.getRandom().nextInt(alienRace.getTravelDistance() / 2));
        }

        generateRebelsStation(world, targetSystem);
        logger.info("Rebels station spawn in " + targetSystem.getCoordsString());
    }

    private void generateRebelsStation(World world, StarSystem targetSystem) {
        NPCShip spaceStation = new NPCShip("rebels_station");
        setStationPosition(targetSystem.getPlanets(), spaceStation, targetSystem.getRadius()/2);
        spaceStation.setAi(null);

        NPC capitan = new NPC(getRebelsStationDialog());
        spaceStation.setCaptain(capitan);

        world.getGlobalVariables().put("war1_explore.rebels_star_system", targetSystem);
        world.getGlobalVariables().put("war1_explore.rebels_station", spaceStation);

        targetSystem.getShips().add(spaceStation);
    }

    private Dialog getRebelsStationDialog() {
        final Dialog docking = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/rebels/war1_explore_rebels_station_docking.json");
        final Dialog inside = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/rebels/war1_explore_rebels_station_inside.json");

        docking.addListener(new DialogListener() {
            private static final long serialVersionUID = 1461828817304326496L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.addOverlayWindow(inside);
            }
        });

        inside.addListener(new DialogListener() {
            private static final long serialVersionUID = -6000073845514073825L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                // remove rebels stations after left star system
                despawnRebelsStation(world);

                if(returnCode == 1){ // accept rebels help
                    world.getGlobalVariables().put("war1_explore.intelligence_get", true);
                    world.getGlobalVariables().put("war1_explore.rebels_help", true);

                    UnityLineGenerator.setDefaultUnityDialog(world); // todo: check future dialogues collisions
                }
                else if(returnCode == 2) { // reject
                    world.getGlobalVariables().put("war1_explore.rebels_help_reject", true);
                }
                else{
                    throw new IllegalArgumentException("Incorrect return code: " + returnCode);
                }
            }
        });
        return docking;
    }

    public static void despawnRebelsStation(final World world){
        if(world.getGlobalVariables().containsKey("war1_explore_rebels_invite_accepted")){
            StarSystem starSystem = (StarSystem)world.getGlobalVariables().get("war1_explore.rebels_star_system");
            NPCShip rebelsStation = (NPCShip) world.getGlobalVariables().get("war1_explore.rebels_station");
            world.addListener(new DespawnShipsAfterLeft(rebelsStation, starSystem));

            world.getGlobalVariables().remove("war1_explore_rebels_invite_accepted");
            logger.info("Rebels line removed");

            if(world.getGlobalVariables().containsKey("war1_explore.intelligence_get")){
                // if player buy intelligence in Unity station
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "rebels_ignore");
            }
        }
    }

    private void setStationPosition(BasePlanet[] planets, NPCShip spaceStation, final int radius) {
        int y = CommonRandom.getRandom().nextInt(2 * radius) - radius;
        int x = (int) (Math.sqrt(radius * radius - y * y) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));

        // Remove planet overlap
        if (planets != null && planets.length > 0) {
            for (int i = 0; i < planets.length; ++i) {
                if (planets[i].getX() == x && planets[i].getY() == y) {
                    x += 1;
                }
            }
        }
        spaceStation.setPos(y, x);
    }
}
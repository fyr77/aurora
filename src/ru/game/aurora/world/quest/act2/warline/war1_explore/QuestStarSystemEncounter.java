package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.NPCShipAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.quest.Journal;
import ru.game.aurora.world.quest.act2.warline.war1_explore.ai.ZorsanPatrolAI;
import ru.game.aurora.world.quest.act2.warline.war1_explore.ai.ZorsanStaionCombatAI;
import ru.game.aurora.world.quest.act2.warline.war1_explore.ai.ZorsanStationAI;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;

import java.io.Serializable;
import java.util.*;

/**
 * Created by di Grigio on 20.07.2017.
 */
public class QuestStarSystemEncounter extends GameEventListener implements WorldGeneratorPart {

    private static final Logger logger = LoggerFactory.getLogger(QuestStarSystemEncounter.class);

    private static final long serialVersionUID = 7555962707966957797L;

    private Set<StarSystem> questSystems;
    private Set<StarSystem> exploredSystems;
    private Set<StarSystem> failedStatus;
    private Map<StarSystem, Map<NPCShip, Boolean>> scanStatus;
    private Map<StarSystem, Boolean> warningStatus;

    @Override
    public void updateWorld(World world) {
        this.scanStatus = new HashMap<StarSystem, Map<NPCShip, Boolean>>();
        this.warningStatus = new HashMap<StarSystem, Boolean>();
        this.exploredSystems = new HashSet<StarSystem>();
        this.questSystems = generateTargetStarSystems(world);
        this.failedStatus = new HashSet<>();
    }

    private Set<StarSystem> generateTargetStarSystems(final World world) {
        final AlienRace alienRace = (AlienRace) world.getFactions().get(ZorsanGenerator.NAME);
        final Set<StarSystem> systems = new HashSet<>();

        // find 3 star systems near zorsan homeworld
        for(int i = 0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); ++i){
            StarSystem targetSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(
                    alienRace.getHomeworld().getX(),
                    alienRace.getHomeworld().getY(),
                    alienRace.getTravelDistance(),

                    new StarSystemListFilter() {
                        @Override
                        public boolean filter(StarSystem starSystem) {
                            if(systems.contains(starSystem)){
                                // need 3 unique star systems
                                return false;
                            }

                            if(starSystem.isQuestLocation()){
                                return false;
                            }

                            return true;
                        }
                    });

            if(targetSystem == null){
                targetSystem = generateStarSystem(world, alienRace);
                logger.error("Fail to get random star system near zorsan homeworld, generate new star system at {}", targetSystem.getCoordsString());
            }

            systems.add(targetSystem);
        }

        for(StarSystem starSystem: systems){
            prepareStarSystem(starSystem);
        }

        return systems;
    }

    private StarSystem generateStarSystem(final World world, final AlienRace alienRace) {
        final StarSystem starSystem = WorldGenerator.generateRandomStarSystem(world, 0, 0);
        world.getGalaxyMap().addObjectAtDistance(starSystem, alienRace.getHomeworld(), alienRace.getTravelDistance());
        return starSystem;
    }

    private void prepareStarSystem(final StarSystem starSystem) {
        starSystem.setQuestLocation(true);
        addStarStations(starSystem);
        this.warningStatus.put(starSystem, false);

        logger.info("Select star system to quest: {}", starSystem.getCoordsString());
    }

    private void addStarStations(final StarSystem starSystem) {
        final Map<NPCShip, Boolean> stationsScanStatus = new HashMap<NPCShip, Boolean>();
        this.scanStatus.put(starSystem, stationsScanStatus);

        for(int i = 0; i < Configuration.getIntProperty("war1_explore.stations_in_star_system"); ++i){
            final int x = CommonRandom.nextInt(4, starSystem.getWidthInTiles()/2 - 4);
            final int y = CommonRandom.nextInt(4, starSystem.getHeightInTiles()/2 - 4);

            final NPCShip station = new NPCShip(Configuration.getProperty("war1_explore.spawn_station_id"), x, y);
            station.setStationary(true);
            station.setAi(new ZorsanStationAI(this));
            station.setCaptain(new NPC(null)); // no dialog
            station.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon"));
            starSystem.getShips().add(station);

            stationsScanStatus.put(station, false);

            if(i == 0){
                station.setPos(station.getX(), -station.getY()); // flip y
            }

            if(i == 1){
                station.setPos(-station.getX(), station.getY()); // flip x
            }

            if(i == 2){
                station.setPos(-station.getX(), -station.getY()); // flip x and y
            }
        }
    }

    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if(questSystems == null || exploredSystems.contains(galaxyMapObject)){
            return null;
        }

        if (questSystems.contains(galaxyMapObject)) {
            return Localization.getText("journal", "war1_explore.title");
        }
        return null;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem starSystem) {
        if(questSystems.contains(starSystem)){
            logger.info("Entering to quest star starsystem: {}", starSystem.getCoordsString());

            // spawn random zorsan patrol ships
            final int minCount = Configuration.getIntProperty("war1_explore.random_patrol_ships_count_min");
            final int maxCount = Configuration.getIntProperty("war1_explore.random_patrol_ships_count_max");
            final int count = CommonRandom.nextInt(minCount, maxCount);

            for(int i = 0; i < count; ++i){
                final int x = CommonRandom.nextInt(3, starSystem.getWidthInTiles()/2 - 3);
                final int y = CommonRandom.nextInt(3, starSystem.getHeightInTiles()/2 - 3);
                spawnPatrolShip(starSystem, x, y, new ZorsanPatrolAI(this, getTargetStation(starSystem, null)));
            }

            if(failedStatus.contains(starSystem)){
                notifyZorsanFleet(world, starSystem);
            }
        }

        return false;
    }

    public void spawnPatrolShip(final StarSystem starSystem, final int x, final int y, final NPCShipAI ai){
        final NPCShip ship = new NPCShip(Configuration.getProperty("war1_explore.spawn_patrol_ship_id"), x, y);
        ship.setCaptain(new NPC(null)); // no dialog
        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_small_cannon"));
        ship.setStationary(false);
        ship.setAi(ai);

        if(ai instanceof CombatAI){
            ship.changeThreat(World.getWorld(), World.getWorld().getPlayer().getShip(), 1);
        }

        starSystem.getShips().add(ship);
    }

    public NPCShip getTargetStation(final StarSystem currentSystem, final NPCShip spawnStation) {
        final ArrayList<NPCShip> targetsStation = new ArrayList(scanStatus.get(currentSystem).keySet());
        if(spawnStation != null){
            targetsStation.remove(spawnStation);
        }

        return targetsStation.get(CommonRandom.nextInt(0, targetsStation.size() - 1));
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem starSystem) {
        if(questSystems.contains(starSystem)){
            logger.info("Leave quest star starsystem: {}", starSystem.getCoordsString());

            // remove all patrol ships
            final List<NPCShip> toRemove = new ArrayList<NPCShip>();
            for(GameObject obj: starSystem.getShips()){
                if(obj instanceof NPCShip){
                    final NPCShip ship = (NPCShip)obj;

                    if(ship.getDesc().getId().equals(Configuration.getProperty("war1_explore.spawn_patrol_ship_id"))){
                        toRemove.add(ship);
                    }
                }
            }

            if(toRemove.size() > 0){
                starSystem.getShips().removeAll(toRemove);
            }

            // update result after star system left
            updateProgress(world, starSystem);
        }
        return false;
    }

    private void updateProgress(final World world, final StarSystem starSystem) {
        if(failedStatus.contains(starSystem)){
            return;
        }

        final Map<NPCShip, Boolean> stationsScanStatus = scanStatus.get(starSystem);
        String result = null;
        if(stationsScanStatus.size() == getScannedCount(stationsScanStatus)){
            // scanning completely done
            result = "success";
            exploredSystems.add(starSystem);
        }
        else if(warningStatus.get(starSystem)){
            // player ship has been detected and scanning not completely done
            result = "failed";
            failedStatus.add(starSystem);
        }

        final Map<String, Serializable> globalVariables = world.getGlobalVariables();
        final Journal journal = world.getPlayer().getJournal();

        if(!globalVariables.containsKey("war1_explore.system1")){
            globalVariables.put("war1_explore.system1", result);
            journal.addQuestEntries("war1_explore", "system1_" + result);
        }
        else if(!globalVariables.containsKey("war1_explore.system2")){
            globalVariables.put("war1_explore.system2", result);
            journal.addQuestEntries("war1_explore", "system2_" + result);
        }
        else if(!globalVariables.containsKey("war1_explore.system3")){
            globalVariables.put("war1_explore.system3", result);
            journal.addQuestEntries("war1_explore", "system3_" + result);
        }

        starSystem.setQuestLocation(false);
    }

    @Override
    public boolean onGameObjectAttacked(World world, GameObject attacker, GameObject target, int damage) {
        if(attacker == world.getPlayer().getShip()){
            notifyZorsanFleet(world, world.getCurrentStarSystem());
        }
        return false;
    }

    public void scanStation(World world, NPCShip ship) {
        if (ship != null && !ship.getDesc().getId().equals(Configuration.getProperty("war1_explore.spawn_station_id"))) {
            return; // wrong target type
        }

        final StarSystem starSystem = world.getCurrentStarSystem();
        if (starSystem == null || !scanStatus.containsKey(starSystem)) {
            return; // wrong position
        }

        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.player_scan"), ship.getName()));

        final Map<NPCShip, Boolean> stationsScanStatus = scanStatus.get(starSystem);
        if (!stationsScanStatus.containsKey(ship)) {
            return; // wrong target instance
        }

        if (stationsScanStatus.get(ship)) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.player_already_scanned"), ship.getName()));
        }
        else {
            stationsScanStatus.put(ship, true);

            final int scannedCount = getScannedCount(stationsScanStatus);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.player_scan_counter"), scannedCount, stationsScanStatus.size()));

            // notify player done scanning
            if(stationsScanStatus.size() == scannedCount){
                showScanningDoneDialog(world);
            }
        }
    }

    private int getScannedCount(Map<NPCShip, Boolean> stationsScanStatus) {
        int count = 0;
        for(Boolean value: stationsScanStatus.values()){
            if(value){ // count all 'true' flags
                ++count;
            }
        }
        return count;
    }

    public boolean detectPlayerShip(final World world, final NPCShip ship, final StarSystem currentSystem) {
        final int detectDist = Configuration.getIntProperty("war1_explore.player_detect_distance");
        final Ship playerShip = world.getPlayer().getShip();
        final double dist = ship.getDistance(playerShip);

        if(playerShip.getX() == ship.getX() || playerShip.getY() == ship.getY()){
            // straight (distance=2)
            if(dist <= detectDist){
                notifyZorsanFleet(world, currentSystem);
                return true;
            }
        }
        else{
            // diagonal (distance=2.82)
            if(dist <= Math.sqrt(2*detectDist*detectDist)){
                notifyZorsanFleet(world, currentSystem);
                return true;
            }
        }

        return false;
    }

    private void notifyZorsanFleet(final World world, final StarSystem currentSystem) {
        if(!warningStatus.get(currentSystem)){
            // player ship detected at first time in current system
            warningStatus.put(currentSystem, true);
            showFailDialog(world);
        }

        // notify other zorsan ships in current star system
        for(GameObject obj: currentSystem.getShips()){
            if(obj instanceof NPCShip){
                final NPCShip notifyShip = (NPCShip)obj;

                // set all zorsan stations and ships agressive to player
                if(notifyShip.getDesc().getId().equals(Configuration.getProperty("war1_explore.spawn_patrol_ship_id"))) {
                    notifyShip.setAi(new CombatAI(world.getPlayer().getShip()));
                    notifyShip.changeThreat(world, world.getPlayer().getShip(), 1);
                }
                else if(notifyShip.getDesc().getId().equals(Configuration.getProperty("war1_explore.spawn_station_id"))){
                    notifyShip.setAi(new ZorsanStaionCombatAI(this, world.getPlayer().getShip()));
                }
            }
        }
    }

    private void showScanningDoneDialog(final World world) {
        final Map<String, Serializable> variables = world.getGlobalVariables();

        if(!variables.containsKey("war1_explore.system1")){
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done_1.json"));
        }
        else if(!variables.containsKey("war1_explore.system2")){
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done_2.json"));
        }
        else if(!variables.containsKey("war1_explore.system3")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done_3.json"));
        }
    }

    private void showFailDialog(final World world) {
        final Map<String, Serializable> variables = world.getGlobalVariables();

        if(!variables.containsKey("war1_explore.system1")){
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_fail_1.json"));
        }
        else if(!variables.containsKey("war1_explore.system2")){
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_fail_2.json"));
        }
        else if(!variables.containsKey("war1_explore.system3")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_fail_3.json"));
        }
    }
}

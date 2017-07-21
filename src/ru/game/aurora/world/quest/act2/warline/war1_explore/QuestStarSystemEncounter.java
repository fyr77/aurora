package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by di Grigio on 20.07.2017.
 */
public class QuestStarSystemEncounter extends GameEventListener implements WorldGeneratorPart {

    private static final Logger logger = LoggerFactory.getLogger(QuestStarSystemEncounter.class);

    private static final long serialVersionUID = 7555962707966957797L;

    private static final int STATIONS_IN_STAR_SYSTEM = 4;

    private Set<StarSystem> questSystems;
    private Set<StarSystem> exploredSystems;
    private Map<StarSystem, Map<NPCShip, Boolean>> scanStatus;
    private Map<StarSystem, Boolean> warningStatus;

    @Override
    public void updateWorld(World world) {
        this.scanStatus = new HashMap<StarSystem, Map<NPCShip, Boolean>>();
        this.warningStatus = new HashMap<StarSystem, Boolean>();
        this.exploredSystems = new HashSet<StarSystem>();
        this.questSystems = generateTargetStarSystems(world);
    }

    private Set<StarSystem> generateTargetStarSystems(final World world) {
        final AlienRace alienRace = (AlienRace) world.getFactions().get(ZorsanGenerator.NAME);
        final Set<StarSystem> systems = new HashSet<>();

        // find 3 star systems near zorsan homeworld
        for(int i = 0; i < 3; ++i){
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
                            else{
                                return true;
                            }
                        }
                    });

            if(targetSystem != null){
                systems.add(targetSystem);
            }
            else{
                // todo: generate starsystem
                logger.error("Fail to get random star system near zorsan homeworld");
            }
        }

        for(StarSystem starSystem: systems){
            prepareStarSystem(starSystem);
        }

        return systems;
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

        for(int i = 0; i < STATIONS_IN_STAR_SYSTEM; ++i){
            final int x = CommonRandom.nextInt(3, starSystem.getWidthInTiles()/2 - 3);
            final int y = CommonRandom.nextInt(3, starSystem.getHeightInTiles()/2 - 3);

            final NPCShip station = new NPCShip("quest_zorsan_station", x, y);
            station.setStationary(true);
            station.setAi(null);
            station.setCaptain(new NPC(null)); // no dialog
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
        }

        return false;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem starSystem) {
        if(questSystems.contains(starSystem)){
            logger.info("Leave quest star starsystem: {}", starSystem.getCoordsString());
        }
        return false;
    }

    public void scanStation(World world, NPCShip ship) {
        if (ship != null && !ship.getDesc().getId().equals("quest_zorsan_station")) {
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

            if (scannedCount == stationsScanStatus.size()) {
                endScanning(world);
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

    private void endScanning(World world) {
        final Dialog dialog = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done.json");
        dialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 7575470612535788376L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.addListener(new EndScanningListener());
            }
        });

        world.addOverlayWindow(dialog);
    }

    private class EndScanningListener extends GameEventListener {

        private static final long serialVersionUID = -2710713127133446629L;

        @Override
        public boolean onPlayerLeftStarSystem(World world, StarSystem starSystem) {
            if(warningStatus.get(starSystem)){
                updateProgress(world, "failed");
            }
            else{
                updateProgress(world, "success");
            }

            starSystem.setQuestLocation(false);
            exploredSystems.add(starSystem);

            this.removeListener(world);
            return false;
        }

        private void updateProgress(World world, String value) {
            final Map<String, Serializable> variables = world.getGlobalVariables();

            if(!variables.containsKey("war1_explore.system1")){
                variables.put("war1_explore.system1", value);
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "system1_" + value);
            }
            else if(!variables.containsKey("war1_explore.system2")){
                variables.put("war1_explore.system2", value);
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "system2_" + value);
            }
            else if(!variables.containsKey("war1_explore.system3")){
                variables.put("war1_explore.system3", value);
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "system3_" + value);
            }
        }
    }
}

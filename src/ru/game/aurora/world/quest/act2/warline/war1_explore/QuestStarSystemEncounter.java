package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Localization;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by di Grigio on 20.07.2017.
 */
class QuestStarSystemEncounter extends GameEventListener implements WorldGeneratorPart {

    private static final Logger logger = LoggerFactory.getLogger(QuestStarSystemEncounter.class);

    private static final long serialVersionUID = 7555962707966957797L;

    private Set<StarSystem> questSystems;

    @Override
    public void updateWorld(World world) {
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
                targetSystem.setQuestLocation(true);
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

    private static void prepareStarSystem(final StarSystem starSystem) {
        starSystem.setQuestLocation(true);
        logger.info("Select star system to quest: {}", starSystem.getCoordsString());
    }

    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if(questSystems == null || questSystems.size() == 0){
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

            // todo: generate star system encounter data
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

    /*
        Dialog.loadFromFile("dialogs/act2/warline/war1_explore/crew/war1_explore_scanning_done.json");

        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system1_success");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system1_failed");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system2_success");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system2_failed");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system3_success");
        world.getPlayer().getJournal().addQuestEntries("war1_explore", "system3_failed");
     */
}

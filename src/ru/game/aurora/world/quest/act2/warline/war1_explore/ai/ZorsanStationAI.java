package ru.game.aurora.world.quest.act2.warline.war1_explore.ai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.npc.shipai.NPCShipAI;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.act2.warline.war1_explore.QuestStarSystemEncounter;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by di Grigio on 04.09.2017.
 * Idle NPCAi with spawn patrols ship function
 */
public class ZorsanStationAI implements NPCShipAI {

    private static final long serialVersionUID = -4037312902528996668L;

    private final QuestStarSystemEncounter encounter;

    private int spawnCooldown = CommonRandom.nextInt(0, Configuration.getIntProperty("war1_explore.stations_spawn_patrol_cooldown"));

    public ZorsanStationAI(final QuestStarSystemEncounter encounter){
        this.encounter = encounter;
    }

    @Override
    public void update(NPCShip station, World world, StarSystem currentSystem) {
        checkSpawnCooldown(station, currentSystem);
        encounter.detectPlayerShip(world, station, currentSystem);
    }

    private void checkSpawnCooldown(final NPCShip station, final StarSystem currentSystem) {
        ++spawnCooldown;

        // spawn patrol ship after turns = cooldown_turns + rand_turns(0, rand_factor)
        if (spawnCooldown >= Configuration.getIntProperty("war1_explore.stations_spawn_patrol_cooldown")) {
            if (CommonRandom.nextInt(0, Configuration.getIntProperty("war1_explore.stations_spawn_patrol_cooldown_rand_factor")) == 0) {
                spawnCooldown = 0;
                encounter.spawnPatrolShip(currentSystem, station.getX(), station.getY(), new ZorsanPatrolAI(encounter, encounter.getTargetStation(currentSystem, station)));
            }
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean isOverridable() {
        return true;
    }
}
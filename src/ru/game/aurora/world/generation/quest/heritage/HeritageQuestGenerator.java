package ru.game.aurora.world.generation.quest.heritage;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.filters.HasPlanetWithLifeFilter;

/**
 * Player can meet a klisk-like mutants on some planets. If he kills them and brings them to klisk homeworld he can get credits
 * and learn their story
 */
public class HeritageQuestGenerator extends GameEventListener implements WorldGeneratorPart, IStateChangeListener<World> {
    // there will be 5 dungons on random planet near klisk space
    private static final int monstersCount = 5;

    private static final String dungeonTag = "heritage";

    @Override
    public void updateWorld(World world) {

        final AlienRace klisk = (AlienRace) world.getFactions().get(KliskGenerator.NAME);
        StarSystem kliskHomeworld = klisk.getHomeworld();
        int range = klisk.getTravelDistance() * 2;
        for (int i = 0; i < monstersCount; ++i) {
            StarSystem nextSS = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(kliskHomeworld.getX(), kliskHomeworld.getY(), range, new HasPlanetWithLifeFilter());
            if (nextSS == null) {
                range += klisk.getTravelDistance();
                --i;
                continue;
            }

            Planet p = HasPlanetWithLifeFilter.getPlanetWithLife(nextSS);
            Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/klisk_mutant_dungeon.tmx"), p);
            dungeon.setTag(dungeonTag);
            dungeon.getController().addListener(this);
            DungeonEntrance entrance = new DungeonEntrance(p, 0, 0, "cavern", dungeon);
            p.setNearestFreePoint(entrance, CommonRandom.getRandom().nextInt(p.getWidth()), CommonRandom.getRandom().nextInt(p.getHeight()));
            p.getPlanetObjects().add(entrance);

        }
    }

    @Override
    public void stateChanged(World param) {

    }

    @Override
    public boolean onPlayerEnteredDungeon(World world, Dungeon dungeon) {
        return super.onPlayerEnteredDungeon(world, dungeon);
    }
}

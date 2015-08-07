package ru.game.aurora.world.generation.quest.heritage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonPlaceholder;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.filters.HasPlanetWithLifeFilter;

import java.util.Map;

/**
 * Player can meet a klisk-like mutants on some planets. If he kills them and brings them to klisk homeworld he can get credits
 * and learn their story
 */
public class HeritageQuestGenerator extends GameEventListener implements WorldGeneratorPart, IStateChangeListener<World> {
    // there will be 5 dungons on random planet near klisk space
    private static final int monstersCount = 5;

    private static final String dungeonTag = "heritage";

    private static final String dungeonNumberTag = "number";

    private static final Logger logger = LoggerFactory.getLogger(HeritageQuestGenerator.class);

    @Override
    public void updateWorld(World world) {

        final AlienRace klisk = (AlienRace) world.getFactions().get(KliskGenerator.NAME);
        StarSystem kliskHomeworld = klisk.getHomeworld();
        int range = klisk.getTravelDistance() * 2;
        for (int i = 0; i < monstersCount; ++i) {
            StarSystem nextSS = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(kliskHomeworld.getX(), kliskHomeworld.getY(), range, new HasPlanetWithLifeFilter());
            if (nextSS == null) {
                logger.warn("No suitable planets found withing {} range from klisk homeworld, increasing range", range);
                range += klisk.getTravelDistance();
                --i;
                continue;
            }
            logger.info("Adding heritage quest to star system at " + nextSS.getCoordsString());

            Planet p = HasPlanetWithLifeFilter.getPlanetWithLife(nextSS);
            Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/klisk_mutant_dungeon.tmx"), p);
            dungeon.getUserData().put(dungeonTag, "");
            dungeon.getController().addListener(this);
            DungeonEntrance entrance = new DungeonEntrance(p, 0, 0, "cavern", dungeon);
            p.setNearestFreePoint(entrance, CommonRandom.getRandom().nextInt(p.getWidth()), CommonRandom.getRandom().nextInt(p.getHeight()));
            p.getPlanetObjects().add(entrance);

        }

        world.addListener(this);
    }

    @Override
    public void stateChanged(World param) {

    }

    private GameObject createMonster(ITileMap map, Dialog d) {
        return new KliskMutant(0, 0, map, d);
    }

    @Override
    public boolean onPlayerEnteredDungeon(World world, Dungeon dungeon) {
        if (!dungeon.getUserData().containsKey(dungeonTag)) {
            return false;
        }
        logger.info("Player has entered dungeon of a Heritage quest");
        // check what dungeon is it

        if (dungeon.getUserData().containsKey(dungeonNumberTag)) {
            // player already entered this dungeon before, no need to show new dialogs
            return false;
        }

        Dialog enterDialog;
        Integer monstersKilled = (Integer) world.getGlobalVariables().get("heritage.monsters_killed");
        if (monstersKilled == null) {
            monstersKilled = 0;
        }

        GameObject monster;

        DungeonPlaceholder placeholder = null;

        for (GameObject go : dungeon.getMap().getObjects()) {
            if (go instanceof DungeonPlaceholder) {
                placeholder = (DungeonPlaceholder) go;
                break;
            }
        }

        if (placeholder == null) {
            throw new IllegalStateException("Placeholder object not found in Heritage quest map");
        }

        switch (monstersKilled) {
            case 0:
                enterDialog = Dialog.loadFromFile("dialogs/encounters/heritage/heritage_first_monster.json");
                monster = createMonster(dungeon.getMap(), enterDialog);
                world.getPlayer().getJournal().addQuestEntries("heritage", "start");
                break;
            case 1:
                enterDialog = Dialog.loadFromFile("dialogs/encounters/heritage/heritage_second_monster.json");
                monster = createMonster(dungeon.getMap(), enterDialog);
                world.getPlayer().getJournal().addQuestEntries("heritage", "second_monster");
                break;
            case 2:
                enterDialog = Dialog.loadFromFile("dialogs/encounters/heritage/heritage_third_monster.json");
                monster = createMonster(dungeon.getMap(), enterDialog);
                world.getPlayer().getJournal().addQuestEntries("heritage", "third_monster");
                break;
            case 3:
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/heritage/heritage_fourth_monster.json"));
                monster = new KliskMutantCorpseItem(0, 0);
                world.getGlobalVariables().put("heritage.monsters_killed", 4);
                world.getPlayer().getJournal().addQuestEntries("heritage", "fourth_monster");
                break;
            case 4:

                final Dialog d = Dialog.loadFromFile("dialogs/encounters/heritage/heritage_fifth_monster.json");
                world.getPlayer().getJournal().addQuestEntries("heritage", "fifth_monster");
                d.addListener(new DialogListener() {
                    @Override
                    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                        world.getCurrentDungeon().getController().returnToPrevRoom(false);
                        ((Planet)world.getCurrentRoom()).leavePlanet(world);
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/heritage/heritage_monster_dialog.json"));
                    }
                });
                world.addOverlayWindow(d);
                monster = createMonster(dungeon.getMap(), null);
                world.getGlobalVariables().put("heritage.fifth_monster_killed", true);
                world.getGlobalVariables().put("heritage.monsters_killed", 5);
                isAlive = false;
                break;
            default:
                throw new IllegalStateException("Strange number of killed monsters for heritage quest: " + monstersKilled);

        }
        dungeon.getUserData().put(dungeonNumberTag, String.valueOf(monstersKilled));
        monster.setPos(placeholder.getX(), placeholder.getY());
        dungeon.getMap().getObjects().add(monster);
        return true;
    }


}

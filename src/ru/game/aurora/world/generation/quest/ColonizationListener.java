package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.GameContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalModifier;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.GalaxyMapObject;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 22.05.14
 * Time: 13:50
 */
public class ColonizationListener extends GameEventListener implements DialogListener {

    private static final Logger logger = LoggerFactory.getLogger(ColonizationListener.class);

    private static final long serialVersionUID = 410651695811568220L;

    private BasePositionable colonyCenter;

    private PlanetNPC chief;

    private int monstersKilled = 0;

    private int remainingScientists = 0;

    private static enum State {
        INITED,
        COLONISTS_BOARDED,
        COLONISTS_DELIVERED,
        QUESTS_AVAILABLE
    }

    private static final boolean[][] colonyMap = new boolean[][]{
            {false, false, false, false, false, false},
            {false, true, true, false, false, false},
            {false, true, true, false, false, false},
            {false, true, false, false, false, false},
            {false, true, false, true, true, false},
            {false, true, true, true, true, false},
            {false, false, false, false, false, false}
    };

    private State state = State.INITED;

    private long time;

    public ColonizationListener(World world) {
        time = world.getTurnCount();
        logger.info("Created colonization listener on turn " + time);
    }

    @Override
    public boolean onReturnToEarth(World world) {
        if (state == State.INITED && world.getTurnCount() - time >= 30) {
            Dialog d = Dialog.loadFromFile("dialogs/quest/colony_search/colony_first_party_departure.json");
            d.addListener(this);
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(d);
        }

        return false;
    }

    private void createMonsters(World world, BasePositionable center, int radius, int amount)
    {
        if (amount == 0) {
            return;
        }
        Planet p = (Planet) world.getGlobalVariables().get("colony_search.coords");
        Random r = CommonRandom.getRandom();
        for (int i = 0; i < amount; ++i) {
            AnimalSpeciesDesc desc = CollectionUtils.selectRandomElement(p.getFloraAndFauna().getAnimalSpecies());
            Animal animal = new Animal(p, 0, 0, desc);
            p.setNearestFreePoint(
                    animal
                    , center.getX() + r.nextInt(radius) - r.nextInt(radius / 2)
                    , center.getY() + r.nextInt(radius) - r.nextInt(radius / 2));
            p.getPlanetObjects().add(animal);
        }
    }

    private class LostScientist extends PlanetNPC
    {
        private static final long serialVersionUID = 1L;

        private int dieTime;

        public LostScientist(World world, int x, int y, String tileset, int tileX, int tileY) {
            super(x, y, tileset, tileX, tileY);
            dieTime = world.getTurnCount() + 70 + CommonRandom.getRandom().nextInt(50);
        }

        @Override
        public void onAttack(World world, GameObject attacker, int damaged) {
            super.onAttack(world, attacker, damaged);
            if (!isAlive()) {
                remainingScientists--;
                GameLogger.getInstance().logMessage(Localization.getText("journal", "colony_search.lost_group.scientist_killed_message"));
                checkAllDone(world);
            }
        }

        @Override
        public void interact(World world) {
            super.interact(world);
            remainingScientists--;
            Object savedVal = world.getGlobalVariables().get("colony.lost_group_quest.saved");
            int saved = 0;
            if (savedVal != null) {
                saved = (Integer)savedVal;
            }
            ++saved;
            world.getGlobalVariables().put("colony.lost_group_quest.saved", saved);
            checkAllDone(world);
            isAlive = false;
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (world.getTurnCount() > dieTime) {
                remainingScientists--;
                checkAllDone(world);
                GameLogger.getInstance().logMessage(Localization.getText("journal", "colony_search.lost_group.scientist_killed_message"));
            }
        }

        private void checkAllDone(World world)
        {
            if (remainingScientists == 0) {
                world.getGlobalVariables().put("colony.lost_group_quest.completed", 0);
                GameLogger.getInstance().logMessage(Localization.getText("journal", "colony_search.lost_group.all_done_message"));
                Object savedVal = world.getGlobalVariables().get("colony.lost_group_quest.saved");
                int saved = 0;
                if (savedVal != null) {
                    saved = (Integer)savedVal;
                }
                if (saved == 0) {
                    world.getPlayer().getJournal().addQuestEntries("colony_search", "lost_group.bad");
                } else if (saved == 5) {
                    world.getPlayer().getJournal().addQuestEntries("colony_search", "lost_group.good");
                } else {
                    world.getPlayer().getJournal().addQuestEntries("colony_search", "lost_group.ok");
                }
            }
        }
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("colony_first_party_departure")) {
            state = State.COLONISTS_BOARDED;
            world.getPlayer().getJournal().addQuestEntries("colony_search", "colonists_departure");
        } else if (dialog.getId().equals("colony_default_2")) {
            if (flags.containsKey("colony_news_update")) {
                int val = 0;
                Object o = world.getGlobalVariables().get("colony_news");
                if (o != null) {
                    val = (Integer)o;
                }
                ++val;
                world.getGlobalVariables().put("colony_news", val);
            }
            Planet planet = (Planet)world.getGlobalVariables().get("colony_search.coords");
            // todo quests
            // monster hunt quest - kill any 5 monsters on this planet
            if (flags.containsKey("colony.hunt_quest")) {
                logger.info("Colony monster hunt quest started");
                world.getGlobalVariables().put("colony.hunt_quest", true);
                world.getPlayer().getJournal().addQuestEntries("colony_search", "hunt");
                // make sure there are alive monsters
                createMonsters(world, new BasePositionable(colonyCenter.getX() + 30, colonyCenter.getY() + CommonRandom.getRandom().nextInt(10)), 10, 5 );
            }
            if (flags.containsKey("colony.hunt_quest.reported")) {
                logger.info("Colony monster hunt quest completed");
                world.getGlobalVariables().put("colony.hunt_quest.completed", "1");
            }

            // lost group quest - find and save 5 scientists from monsters
            if (flags.containsKey("colony.lost_group")) {
                logger.info("Lost group quest started");
                world.getGlobalVariables().put("colony.lost_group", true);
                world.getPlayer().getJournal().addQuestEntries("colony_search", "lost_group");
                remainingScientists = 5;

                for (int i = 0; i < remainingScientists; ++i) {
                    LostScientist ls = new LostScientist(world, 0, 0, "humanity_tileset", 6, 2);
                    ls.setDialog(Dialog.loadFromFile("dialogs/quest/colony_search/lost_group_" + (i + 1) + ".json"));
                    planet.setNearestFreePoint(ls, colonyCenter.getX() + 50 + CommonRandom.getRandom().nextInt(100), colonyCenter.getY() + 50 + CommonRandom.getRandom().nextInt(100));
                    createMonsters(world, ls, 20, CommonRandom.getRandom().nextInt(4));
                }

            }
        }
    }

    public void modifyPlanet(World world, Planet planet) {
        final int initialSize = Configuration.getIntProperty("quest.colony_search.colony_initial_size");
        colonyCenter = planet.findPassableRegion(initialSize, initialSize);
        if (colonyCenter == null) {
            logger.error("Failed to find suitable surface for colony");
            colonyCenter = new BasePositionable(0, 0);
        }
        for (int x = 0; x < initialSize / 2; ++x) {
            for (int y = 0; y < initialSize / 2; ++y) {
                int realX = colonyCenter.getX() + x * 2;
                int realY = colonyCenter.getY() + y * 2;
                if (colonyMap[y][x]) {
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            boolean visible = planet.getSurface().isTileVisible(realX + i, realY + j);
                            planet.getSurface().getSurface()[realY + i][realX + j] = visible ? SurfaceTypes.VISIBILITY_MASK | SurfaceTypes.ASPHALT : SurfaceTypes.ASPHALT;
                        }
                    }
                }
            }
        }

        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 2, colonyCenter.getY() + 2, new Drawable("colony_boxes"), null, ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 2, colonyCenter.getY() + 3, new Drawable("colony_boxes"), null, ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 3, colonyCenter.getY() + 4, new Drawable("colony_tractor"), null, ScanGroup.OTHER));

        // workers
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 3, colonyCenter.getY() + 5, new Drawable("humanity_tileset", 0, 6)));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 4, colonyCenter.getY() + 2, new Drawable("humanity_tileset", 0, 6)));

        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 6, colonyCenter.getY() + 8, new Drawable("colony_tent"), null, ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 7, colonyCenter.getY() + 8, new Drawable("colony_smalltent"), null, ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 8, colonyCenter.getY() + 8, new Drawable("colony_house1"), null, null));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 5, colonyCenter.getY() + 10, new Drawable("humanity_tileset", 2, 6)));
        planet.getPlanetObjects().add(new BaseGameObject(colonyCenter.getX() + 7, colonyCenter.getY() + 10, new Drawable("colony_tractor"), null, null));


        AnimalSpeciesDesc guard = new AnimalSpeciesDesc(planet, "Marine", false, false, 10, ResourceManager.getInstance().getWeapons().getEntity("assault"), 1, MonsterBehaviour.FRIENDLY, Collections.<AnimalModifier>emptySet());
        guard.setCanBePickedUp(false);
        guard.setImages(ResourceManager.getInstance().getSpriteSheet("humanity_tileset").getSprite(1, 9), ResourceManager.getInstance().getImage("no_image"));
        planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 9, colonyCenter.getY() + 7, guard));
        if (world.getGlobalVariables().containsKey("colony_search.explored_fully")) {
            planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 2, colonyCenter.getY() + 6, guard));
            planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 7, colonyCenter.getY() + 3, guard));
        }

        chief = new PlanetNPC(colonyCenter.getX() + 8, colonyCenter.getY() + 9, "colony_colonist");
        chief.setDialog(Dialog.loadFromFile("dialogs/quest/colony_search/colony_default.json"));
        planet.getPlanetObjects().add(chief);
        logger.info("Colony established");
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        if (state == State.COLONISTS_BOARDED && planet == world.getGlobalVariables().get("colony_search.coords")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/colony_search/colony_first_party_arrival.json"));
            modifyPlanet(world, planet);
            state = State.COLONISTS_DELIVERED;
            time = world.getTurnCount();
            world.getPlayer().getJournal().addQuestEntries("colony_search", "colonists_arrival");
        }

        if (state == State.COLONISTS_DELIVERED && world.getTurnCount() - time > 100) {
            logger.info("Initializing colony quests as 100 turns have passed");
            state = State.QUESTS_AVAILABLE;
            Dialog d = Dialog.loadFromFile("dialogs/quest/colony_search/colony_default_2.json");
            d.addListener(this);
            chief.setDialog(d);
        }

        return false;
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (world.getGlobalVariables().containsKey("colony_search.coords")) {
            Planet p = (Planet) world.getGlobalVariables().get("colony_search.coords");
            if (p.getOwner() == galaxyMapObject) {
                return Localization.getText("journal", "colony_search.map_label");
            }
        }

        return null;
    }

    @Override
    public boolean onGameObjectAttacked(World world, GameObject attacker, GameObject target, int damage) {
        if (world.getGlobalVariables().containsKey("colony.hunt_quest")
                && !world.getGlobalVariables().containsKey("colony.hunt_quest.completed")
                && world.getCurrentRoom() == world.getGlobalVariables().get("colony_search.coords")) {
            if (!target.isAlive()) {
                monstersKilled++;
                if (monstersKilled >= 5) {
                    logger.info("Player has killed 5 monsters for colony quest");
                    world.getGlobalVariables().put("colony.hunt_quest.completed", "0");
                }
            }

        }

        return false;
    }
}

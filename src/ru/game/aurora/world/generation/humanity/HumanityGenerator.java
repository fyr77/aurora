/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:17
 */
package ru.game.aurora.world.generation.humanity;


import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.TradeScreenController;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceDebris;
import ru.game.aurora.world.space.StarSystem;

import java.util.List;

public class HumanityGenerator implements WorldGeneratorPart {
    public static final String NAME = "Humanity";
    private static final long serialVersionUID = -1289210420627927980L;
    private static final ProbabilitySet<GameObject> defaultLootTable;

    static {
        defaultLootTable = new ProbabilitySet<>();
        defaultLootTable.put(new SpaceDebris.ResourceDebris(5), 1.0);
        defaultLootTable.put(new SpaceDebris.ResourceDebris(10), 0.2);
    }

    @Override
    public void updateWorld(final World world) {
        final AlienRace humans = new AlienRace(NAME, "earth_transport", Dialog.loadFromFile("dialogs/human_ship_default_dialog.json"));
        humans.setTravelDistance(1);
        humans.setKnown(true);
        world.getFactions().put(humans.getName(), humans);

        //todo: restore
        //SingleShipFixedTime listener = new SingleShipFixedTime(4, new AuroraProbe(0, 0), Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/aurora_probe_detected.json")));
        //listener.setGroups(GameEventListener.EventGroup.ENCOUNTER_SPAWN);
        //world.addListener(listener);

        // earth
        final StarSystem solarSystem = HomeworldGenerator.createSolarSystem(world, humans);
        solarSystem.setAstronomyData(0); // everything is explored
        solarSystem.setQuestLocation(true);
        solarSystem.setVisited(true);
        humans.setHomeworld(solarSystem);

        // generate solar system somewhere in lower third of a galaxy
        world.getGalaxyMap().addObjectAndSetTile(
                solarSystem
                , CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesX() / 2) + world.getGalaxyMap().getTilesX() / 3
                , CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesY() / 3) + world.getGalaxyMap().getTilesY() / 2);
        world.getGlobalVariables().put("solar_system", solarSystem);

        world.getPlayer().setShip(world, humans);


        humans.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = -430786152130330165L;

            @Override
            public NPCShip createShip(World world, int shipType) {
                NPCShip ship = new NPCShip("earth_transport");
                if (world.getGlobalVariables().containsKey("earth.advanced_lasers")) {
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon2"));
                } else {
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"));
                }
                ship.setSpeed(2);
                ship.setAi(new LandAI(solarSystem.getPlanets()[CommonRandom.getRandom().nextInt(solarSystem.getPlanets().length)]));
                ship.setLoot(defaultLootTable);
                return ship;
            }
        });

        // set custom ship AIs, only land on planets, do not leave star system
        world.addListener(new StandardAlienShipEvent(humans, humans.getDefaultFactory(), true) {
            @Override
            public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
                if (!ss.equals(world.getGlobalVariable("solar_system", null))) {
                    return false;
                }

                if (CommonRandom.getRandom().nextDouble() < 0.7) {
                    NPCShip ship = shipFactory.createShip(world, 0);
                    ss.setRandomEmptyPosition(ship);
                    ss.getShips().add(ship);
                    ship.setAi(new LandAI(CollectionUtils.selectRandomElement(world.getCurrentStarSystem().getPlanets())));
                    return true;
                }
                return false;
            }
        });

        // add welcoming messages
        List<PrivateMessage> pm = world.getPlayer().getEarthState().getMessages();

        pm.add(new PrivateMessage(world, "game_start", "message"));
        pm.add(new PrivateMessage(world, "game_start_2", "message"));

        // add Enterprise ship event
        //world.addListener(new EnterpriseEncounterCreator()); //TODO: re-add later

        world.addListener(new BiologyResearch());

        /**
         * After player returns with first information about obliterator, on its next return earth researches will analyze obliterator route and show quest dialog
         */
        world.addListener(new GameEventListener() {

            private static final long serialVersionUID = 1;

            @Override
            public boolean onReturnToEarth(World world) {
                if (world.getGlobalVariables().containsKey("quest.main.knows_about_obliterator")) {
                    world.addListener(new EarthResearch("obliterator_study", 30) {

                        private static final long serialVersionUID = 1;

                        @Override
                        protected void onCompleted(World world) {
                            world.getGlobalVariables().put("quest.main.show_earth_dialog", null);
                        }
                    });
                    isAlive = false;
                    return true;
                }
                return false;
            }
        });
    }

    private class TradeShip extends NPCShip
    {

        public TradeShip(Faction race) {
            super("earth_transport", 0, 0);
            setStationary(true);
        }

        @Override
        public boolean interact(World world) {
            Multiset<InventoryItem> inventoryItems = HashMultiset.<InventoryItem>create();
            inventoryItems.add(Resources.CELLS_FROM_PARALLEL_WORLD, 5);
            inventoryItems.add(new SellOnlyInventoryItem(
                            "items", "rogue_beacon_data", new Drawable("technology_research"), Configuration.getIntProperty("quest.rogues_beacon.price"), true, RoguesGenerator.NAME
                    ));
            TradeScreenController.openTrade("klisk_dialog", inventoryItems);

            return true;
        }
    }
}

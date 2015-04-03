package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.SingleStarsystemShipSpawner;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.util.GameTimer;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.EmbassiesQuest;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.13
 * Time: 18:17
 */
public class ZorsanGenerator implements WorldGeneratorPart {
    public static final String NAME = "Zorsan";

    private static final long serialVersionUID = 1083992211652099884L;

    public static final int SCOUT_SHIP = 0;

    public static final int CRUISER_SHIP = 1;

    // makes zorsan hostile to player only after a couple of turns
    // otherwise player ship gets destroyed almost instantly
    private static class ZorsanEscapeListener extends GameEventListener implements IStateChangeListener<World> {

        private static final long serialVersionUID = -5333707636208684602L;

        private final GameTimer timer = new GameTimer(5);

        @Override
        public boolean onTurnEnded(World world) {
            if (timer.update()) {
                world.getReputation().setHostile(ZorsanGenerator.NAME, HumanityGenerator.NAME);
                StarSystem currentStarSystem = world.getCurrentStarSystem();
                if (currentStarSystem != null) {
                    currentStarSystem.getReputation().setHostile(ZorsanGenerator.NAME, HumanityGenerator.NAME);
                }
                world.getReputation().setHostile(HumanityGenerator.NAME, ZorsanGenerator.NAME);
                isAlive = false;
                return true;
            }
            return false;
        }

        @Override
        public void stateChanged(World world) {
            world.addListener(this);
        }
    }

    private StarSystem generateHomeworld(World world, int x, int y, final AlienRace race) {
        final BasePlanet[] planets = new BasePlanet[1];
        final StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.white), x, y);

        final Dialog initialHomeworldDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_homeworld_1.json");
        final Dialog continueDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_homeworld_2.json");
        final Dialog planetSightseeingDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_city_transfer.json");
        final Dialog zorsanFinalDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_before_attack.json");
        final Dialog escapeDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_escape.json");


        planets[0] = new AlienHomeworld("zorsan_homeworld", race, initialHomeworldDialog, 3, 0, ss, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 0, PlanetCategory.PLANET_ROCK);
        initialHomeworldDialog.addListener(new DialogListener() {

            private static final long serialVersionUID = 5653727064261130921L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (dialog.getId().equals(initialHomeworldDialog.getId()) || dialog.getId().equals(continueDialog.getId())) {
                    if (returnCode == 0) {
                        continueDialog.addListener(this);
                        ((AlienHomeworld) planets[0]).setDialog(continueDialog);
                    }

                    if (returnCode == 1) {
                        // descending to planet
                        planetSightseeingDialog.addListener(this);
                        world.addOverlayWindow(planetSightseeingDialog);
                    }
                } else if (dialog.getId().equals(planetSightseeingDialog.getId())) {

                    // set as much marines as possible in a landing party
                    LandingParty party = world.getPlayer().getLandingParty();

                    Ship ship = world.getPlayer().getShip();
                    if (party.getTotalMembers() < 10) {
                        final int marineCount = Math.min(ship.getMilitary(), 10 - party.getTotalMembers());
                        party.setMilitary(party.getMilitary() + marineCount);
                        ship.setMilitary(ship.getMilitary() - marineCount);
                    }

                    party.setWeapon(ResourceManager.getInstance().getWeapons().getEntity("zorsan_laser_rifles"));
                    world.getPlayer().getInventory().add(ResourceManager.getInstance().getWeapons().getEntity("zorsan_laser_rifles"));
                    ResearchProjectDesc zorsan_crew_weapons = world.getResearchAndDevelopmentProjects().getResearchProjects().get("zorsan_crew_weapons");
                    if (zorsan_crew_weapons != null) {
                        world.getPlayer().getResearchState().getCurrentProjects().add(new ResearchProjectState(zorsan_crew_weapons));
                    }
                    Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/zor_escape.tmx"), ss);
                    dungeon.setEnterDialog(zorsanFinalDialog);
                    dungeon.setSuccessDialog(escapeDialog);
                    dungeon.setCommanderInParty(true); // loosing this dungeon will lead to a gameover
                    dungeon.setPlaylistName("dungeon_invasion");
                    dungeon.enter(world);
                    world.setCurrentRoom(dungeon);
                    world.getGlobalVariables().put("zorsan.escape", 0);
                    world.getGlobalVariables().put("diplomacy.zorsan_visited", 0);
                    EmbassiesQuest.updateJournal(world, "zorsan");


                    zorsanFinalDialog.setFlags(dialog.getFlags()); // pass flags from previous dialog to a next one
                    dungeon.getController().addListener(new ZorsanEscapeListener());

                    // after this, zorsan become hostile and player has fixed amount of time before they attack earth
                    addWarDataDrop(world);
                    ((AlienHomeworld) planets[0]).setCanBeCommunicated(false);

                    world.getPlayer().getJournal().addQuestEntries("zorsan_relations", "start");
                }
            }
        });


        HomeworldGenerator.setCoord(planets[0], 3);

        // add a ship to fight with after takeoff
        NPCShip cruiser = race.getDefaultFactory().createShip(world, CRUISER_SHIP);
        cruiser.setPos(planets[0].getX() + 6, planets[0].getY() + 5);
        ss.getShips().add(cruiser);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    public static void addWarDataDrop(World world) {
        ((AlienRace)world.getFactions().get(NAME)).getDefaultLootTable().put(new ZorsanWarData(), 10.3);
    }

    public static void removeWarDataDrop(World world) {
        for (Iterator<Map.Entry<GameObject, Double>> iter = ((AlienRace)world.getFactions().get(NAME)).getDefaultLootTable().entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<GameObject, Double> e = iter.next();
            if (e.getKey() instanceof ZorsanWarData) {
                iter.remove();
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        final AlienRace race = new AlienRace(NAME, "zorsan_scout", Dialog.loadFromFile("dialogs/zorsan_main.json"));

        race.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.COMPUTERS, race)), 0.3);
        race.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.ENERGY, race)), 0.15);
        race.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.GOODS, race)), 0.1);
        race.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.MATERIALS, race)), 0.3);
        race.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.WEAPONS, race)), 0.6);

        race.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = -2842750240901357677L;


            @Override
            public NPCShip createShip(World world, int shipType) {
                NPCShip ship;
                switch (shipType) {
                    case SCOUT_SHIP: {
                        ship = new NPCShip(0, 0, "zorsan_scout", race, null, "Zorsan scout", 10);
                        ship.setSpeed(1);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon"));
                        break;
                    }
                    case CRUISER_SHIP: {
                        ship = new NPCShip(0, 0, "zorsan_cruiser", race, null, "Zorsan cruiser", 15);
                        ship.setSpeed(2);
                        ship.enableRepairs(5);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon"), ResourceManager.getInstance().getWeapons().getEntity("zorsan_small_cannon"));
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unsupported ship type for Zorsan race: " + shipType);
                }

                ship.setLoot(race.getDefaultLootTable());
                return ship;
            }
        });

        StarSystem homeworld = generateHomeworld(world, 3, 8, race);
        world.getGalaxyMap().addObjectAtDistance(homeworld, (Positionable) world.getGlobalVariables().get("solar_system"), 20 + CommonRandom.getRandom().nextInt(Configuration.getIntProperty("world.galaxy.zorsan_homeworld_distance")));
        world.getGlobalVariables().put("zorsan.homeworld", homeworld.getCoordsString());

        race.setTravelDistance(50);
        world.addListener(new StandardAlienShipEvent(race));
        race.setHomeworld(homeworld);
        world.addListener(new SingleStarsystemShipSpawner(race.getDefaultFactory(), 0.8, race.getHomeworld()));
        world.getFactions().put(race.getName(), race);

        // zorsan are hostile to bork
        world.getReputation().setHostile(NAME, BorkGenerator.NAME);
        world.getReputation().setHostile(BorkGenerator.NAME, NAME);

        world.getResearchAndDevelopmentProjects().getEarthResearchProjects().put("zorsan_weapons", new EarthResearch("zorsan_weapons", 100) {
            private static final long serialVersionUID = -7037397500987009921L;

            @Override
            protected void onCompleted(World world) {
                world.getPlayer().getEarthState().getAvailableUpgrades().add(new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon")));
                world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/zorsan_cannon_unlocked.json"));
                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "zorsan_weapons_reserch", "news"));
                world.getPlayer().getEarthState().updateTechnologyLevel(100);
            }
        });
    }

}

package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
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
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.util.GameTimer;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
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
    public static final String NAME = "zorsan";

    private static final long serialVersionUID = 1083992211652099884L;

    public static final int SCOUT_SHIP = 0;

    public static final int CRUISER_SHIP = 1;

    private static final ProbabilitySet<SpaceObject> defaultLootTable;

    static {
        defaultLootTable = new ProbabilitySet<>();
        defaultLootTable.put(new SpaceDebris.ResourceDebris(5), 1.0);
        defaultLootTable.put(new SpaceDebris.ResourceDebris(10), 0.2);
    }


    // makes zorsan hostile to player only after a couple of turns
    // otherwise player ship gets destroyed almost instantly
    private static class ZorsanEscapeListener extends GameEventListener implements IStateChangeListener {

        private static final long serialVersionUID = -5333707636208684602L;

        private GameTimer timer = new GameTimer(5);

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

                    party.setWeapon(ResourceManager.getInstance().getLandingPartyWeapons().getEntity("zorsan_laser"));
                    world.getPlayer().getInventory().put(ResourceManager.getInstance().getLandingPartyWeapons().getEntity("zorsan_laser"), 1);
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
                    EmbassiesQuest.updateJournal(world, "zorsan");
                    world.getGlobalVariables().put("diplomacy.zorsan_visited", 0);

                    zorsanFinalDialog.setFlags(dialog.getFlags()); // pass flags from previous dialog to a next one
                    dungeon.getController().setSuccessListener(new ZorsanEscapeListener());

                    // after this, zorsan become hostile and player has fixed amount of time before they attack earth
                    addWarDataDrop();

                }
            }
        });


        HomeworldGenerator.setCoord(planets[0], 3);

        // add a ship to fight with after takeoff
        NPCShip cruiser = race.getDefaultFactory().createShip(CRUISER_SHIP);
        cruiser.setPos(planets[0].getX() + 6, planets[0].getY() + 5);
        ss.getShips().add(cruiser);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    public static void addWarDataDrop() {
        defaultLootTable.put(new ZorsanWarData(), 10.3);
    }

    public static void removeWarDataDrop() {
        for (Iterator<Map.Entry<SpaceObject, Double>> iter = defaultLootTable.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<SpaceObject, Double> e = iter.next();
            if (e.getKey() instanceof ZorsanWarData) {
                iter.remove();
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        final AlienRace race = new AlienRace(NAME, "zorsan_scout", Dialog.loadFromFile("dialogs/zorsan_main.json"));

        race.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = -2842750240901357677L;


            @Override
            public NPCShip createShip(int shipType) {
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

                ship.setLoot(defaultLootTable);
                return ship;
            }
        });

        StarSystem homeworld = generateHomeworld(world, 3, 8, race);
        world.getGalaxyMap().addObjectAtDistance(homeworld, (Positionable) world.getGlobalVariables().get("solar_system"), Configuration.getIntProperty("world.galaxy.zorsan_homeworld_distance"));
        world.getGlobalVariables().put("zorsan.homeworld", homeworld.getCoordsString());

        race.setTravelDistance(5);
        world.addListener(new StandardAlienShipEvent(race));
        race.setHomeworld(homeworld);
        world.addListener(new SingleStarsystemShipSpawner(race.getDefaultFactory(), 0.8, race.getHomeworld()));
        world.getRaces().put(race.getName(), race);
        addWarDataDrop();

        // zorsan are hostile to anyone
        world.getReputation().setHostile(NAME, BorkGenerator.NAME);
        world.getReputation().setHostile(NAME, RoguesGenerator.NAME);
        world.getReputation().setHostile(NAME, KliskGenerator.NAME);
        world.getReputation().setHostile(BorkGenerator.NAME, NAME);
        world.getReputation().setHostile(RoguesGenerator.NAME, NAME);
        world.getReputation().setHostile(KliskGenerator.NAME, NAME);

        world.getResearchAndDevelopmentProjects().getEarthResearchProjects().put("zorsan_weapons", new EarthResearch("zorsan_weapons", 100) {
            private static final long serialVersionUID = -7037397500987009921L;

            @Override
            protected void onCompleted(World world) {
                // todo: add zorsan ship cannon
                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("zorsan_weapons_reserch", "news"));
                world.getPlayer().getEarthState().updateTechnologyLevel(5);
            }
        });
    }

    public static ProbabilitySet<SpaceObject> getDefaultLootTable() {
        return defaultLootTable;
    }
}

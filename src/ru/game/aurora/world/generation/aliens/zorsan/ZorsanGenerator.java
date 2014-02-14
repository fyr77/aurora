package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.SingleStarsystemShipSpawner;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.EmbassiesQuest;
import ru.game.aurora.world.planet.BasePlanet;
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


    private StarSystem generateHomeworld(World world, int x, int y, final AlienRace race) {
        final BasePlanet[] planets = new BasePlanet[1];
        final StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.white), x, y);

        // add a ship to fight with after takeoff
        NPCShip cruiser = race.getDefaultFactory().createShip(CRUISER_SHIP);
        cruiser.setPos(4, 2);
        ss.getShips().add(cruiser);

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
                if (dialog == initialHomeworldDialog || dialog == continueDialog) {
                    if (returnCode == 0) {
                        continueDialog.addListener(this);
                        ((AlienHomeworld) planets[0]).setDialog(continueDialog);
                    }

                    if (returnCode == 1) {
                        // descending to planet
                        planetSightseeingDialog.addListener(this);
                        world.addOverlayWindow(planetSightseeingDialog);
                    }
                } else if (dialog == planetSightseeingDialog) {
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
                    world.getReputation().setHostile(race.getName(), HumanityGenerator.NAME);

                    // after this, zorsan become hostile and player has fixed amount of time before they attack earth
                    addWarDataDrop();
                }
            }
        });


        HomeworldGenerator.setCoord(planets[0], 3);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    public static void addWarDataDrop()
    {
        defaultLootTable.put(new ZorsanWarData(), 10.3);
    }

    public static void removeWarDataDrop()
    {
        for (Iterator<Map.Entry<SpaceObject, Double>> iter = defaultLootTable.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<SpaceObject, Double> e = iter.next();
            if (e instanceof ZorsanWarData) {
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
                        ship = new NPCShip(0, 0, "zorsan_scout", race, null, "Zorsan scout");
                        ship.setHp(8);
                        ship.setSpeed(1);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"));
                        break;
                    }
                    case CRUISER_SHIP: {
                        ship = new NPCShip(0, 0, "zorsan_cruiser", race, null, "Zorsan cruiser");
                        ship.setHp(15);
                        ship.setSpeed(2);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"));
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
        world.getGlobalVariables().put("zorsan.homeworld", String.format("[%d, %d]", homeworld.getGlobalMapX(), homeworld.getGlobalMapY()));
        world.getGalaxyMap().addObjectAndSetTile(homeworld, 3, 8);
        race.setTravelDistance(5);
        world.addListener(new StandardAlienShipEvent(race));
        race.setHomeworld(homeworld);
        world.addListener(new SingleStarsystemShipSpawner(race.getDefaultFactory(), 0.8, race.getHomeworld()));
        world.getRaces().put(race.getName(), race);
        addWarDataDrop();
    }
}

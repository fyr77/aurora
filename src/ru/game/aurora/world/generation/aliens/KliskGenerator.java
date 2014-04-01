/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:12
 */
package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.NextDialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.AlienRaceResearch;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.EmbassiesQuest;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Creates Klisk alien race
 */
public class KliskGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -6983386879381885934L;

    public static final String NAME = "Klisk";

    // ship IDs used in factory generation
    public static final int DEFAULT_SHIP = 0;

    public static final int TRADE_PROBE = 1;

    public static final int STATION = 2;

    private static final ProbabilitySet<SpaceObject> defaultLootTable;

    static {
        defaultLootTable = new ProbabilitySet<>();
        defaultLootTable.put(new SpaceDebris.ResourceDebris(5), 1.0);
        defaultLootTable.put(new SpaceDebris.ResourceDebris(10), 0.2);
    }

    private Dialog createDefaultKliskPlanetDialog(World world) {
        Dialog d = Dialog.loadFromFile("dialogs/klisk/klisk_planet_default.json");
        d.addListener(new DialogListener() {
            private static final long serialVersionUID = 4082728827280648178L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (flags.containsKey("klisk.war_help")) {
                    world.getGlobalVariables().put("klisk.war_help", true);
                }

                if (flags.containsKey("klisk_trader_drone.withdraw")) {
                    // remove trader drone
                    for (Iterator<SpaceObject> iter = ((StarSystem)world.getGlobalVariables().get("solar_system")).getShips().iterator(); iter.hasNext(); ) {
                        SpaceObject so = iter.next();
                        if (so.getName().equals("Klisk trade probe")) {
                            iter.remove();
                            break;
                        }
                    }

                    world.getGlobalVariables().put("klisk_trader_drone.result", "withdraw");
                    world.getPlayer().getJournal().addQuestEntries("klisk_trader_drone", "withdrawed");
                }

                if (world.getGlobalVariables().containsKey("klisk_trade.result")) {
                    int repDelta = 0;
                    final String tradeResult = (String) world.getGlobalVariables().get("klisk_trade.result");
                    switch (tradeResult) {
                        case "perfect":
                            repDelta = 2;
                            break;
                        case "good":
                            repDelta = 1;
                            break;
                        case "bad":
                            repDelta = -1;
                            break;
                    }
                    EmbassiesQuest.updateJournal(world, "klisk_" + tradeResult);
                    world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, repDelta);
                    world.getGlobalVariables().remove("klisk_trade.result");
                }

            }
        });

        return d;
    }

    private void beginTradeQuest(World world, AlienHomeworld kliskPlanet, final StarSystem targetSystem) {
        NPCShip ship = world.getRaces().get(KliskGenerator.NAME).getDefaultFactory().createShip(0);
        ship.setCaptain(new NPC(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_ship_default.json")));

        ship.setAi(new LeaveSystemAI());
        ship.setPos(kliskPlanet.getX() - 1, kliskPlanet.getY() + 1);
        world.getCurrentStarSystem().getShips().add(ship);
        world.getPlayer().getJournal().addQuestEntries("klisk_trade", "start");
        world.addListener(new KliskTradequestDialogListener(targetSystem));
    }

    private StarSystem generateTargetStarsystemForTradeQuest(World world, AlienRace race) {
        StarSystem ss = WorldGenerator.generateRandomStarSystem(world, 12, 15);
        world.getGalaxyMap().addObjectAtDistance(ss, (Positionable) world.getGlobalVariables().get("solar_system"), 20);
        world.getGlobalVariables().put("klisk_trade.coords", ss.getCoordsString());

        NPCShip spaceStation = race.getDefaultFactory().createShip(STATION);
        ss.setRandomEmptyPosition(spaceStation);
        ss.getShips().add(spaceStation);
        spaceStation.setCaptain(new NPC(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_station_default.json")));
        ss.setQuestLocation(true);
        return ss;
    }

    private Dialog createPlanetDialogAndQuests(final AlienHomeworld kliskPlanet, final StarSystem targetSystemForQuest) {
        Dialog startDialog = Dialog.loadFromFile("dialogs/klisk/klisk_station_start.json");

        Dialog ambassadorDialog = Dialog.loadFromFile("dialogs/klisk/klisk_station_main.json");
        startDialog.addListener(new NextDialogListener(ambassadorDialog));
        ambassadorDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 4082728827280648178L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getGlobalVariables().put("diplomacy.klisk_visited", 0);
                if (returnCode == 0) {
                    // no quest
                    world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, -1);

                } else {
                    // accepts a quest
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_start.json"));
                    world.getGlobalVariables().put("klisk_trade.result", 1); // hack to enable required statement in space station dialog
                    beginTradeQuest(world, kliskPlanet, targetSystemForQuest);
                }

                kliskPlanet.setDialog(createDefaultKliskPlanetDialog(world));
            }
        });

        return startDialog;
    }


    private StarSystem generateKliskHomeworld(World world, int x, int y, AlienRace kliskRace) {
        BasePlanet[] planets = new BasePlanet[3];
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.yellow), x, y);

        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        HomeworldGenerator.setCoord(planets[0], 2);

        planets[1] = new AlienHomeworld("klisk_homeworld", kliskRace, null, 3, 0, ss, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 0, PlanetCategory.PLANET_ROCK);
        ((AlienHomeworld) planets[1]).setDialog(createPlanetDialogAndQuests((AlienHomeworld) planets[1], generateTargetStarsystemForTradeQuest(world, kliskRace)));
        HomeworldGenerator.setCoord(planets[1], 3);

        planets[2] = new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[2], 5);

        AlienArtifact a = new AlienArtifact(3, 4, "small_artifact", new ArtifactResearch(new ResearchReport("small_artifact", "klisk_banner.report")));
        final Planet planet = (Planet) planets[2];
        planet.setNearestFreePoint(a, CommonRandom.getRandom().nextInt(planet.getWidth()), CommonRandom.getRandom().nextInt(planet.getHeight()));
        planet.getPlanetObjects().add(a);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile("dialogs/klisk_1.json");
        final AlienRace kliskRace = new AlienRace(NAME, "klisk_ship", mainDialog);
        mainDialog.addListener(new DialogListener() {

            private static final long serialVersionUID = 8770464358766507288L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

                switch (returnCode) {
                    case 3:
                        // free info about klisk race
                        world.getGlobalVariables().put("klisk.klisk_info", true);
                        ResearchProjectDesc research = new AlienRaceResearch("klisk", world.getRaces().get(KliskGenerator.NAME), new JournalEntry("klisk", "main"));
                        world.getPlayer().getResearchState().addNewAvailableProject(research);
                        break;
                    case 4:
                        // free info about colony planet
                        world.getGlobalVariables().put("klisk.planet_info", true);
                        break;
                    case 10:
                        // decided to take time
                        return;
                }

                if (flags.containsKey("klisk.knows_about_path_philosophy")) {
                    world.getGlobalVariables().put("klisk.knows_about_path_philosophy", true);
                }

                Dialog newDefaultDialog = Dialog.loadFromFile("dialogs/klisk_main.json");
                newDefaultDialog.addListener(new KliskMainDialogListener(kliskRace));
                kliskRace.setDefaultDialog(newDefaultDialog);
            }
        });
        kliskRace.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = 5473066320214324094L;

            @Override
            public NPCShip createShip(int shipType) {

                NPCShip ship;
                switch (shipType) {
                    case DEFAULT_SHIP:
                        ship = new NPCShip(0, 0, "klisk_ship", kliskRace, null, "Klisk Ship", 15);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_small_laser"), ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
                        ship.setSpeed(2);
                        break;

                    case TRADE_PROBE:
                        ship = new NPCShip(0, 0, "klisk_drone", kliskRace, null, "Klisk drone", 7);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_small_laser"));
                        ship.setStationary(true);
                        ship.setCanBeHailed(false);
                        ship.setSpeed(2);
                        break;
                    case STATION:
                        ship = new NPCShip(0, 0, "klisk_station", kliskRace, null, "Klisk station", 25);
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_small_laser"), ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
                        ship.enableRepairs(3);
                        ship.setSpeed(2);
                        ship.setStationary(true);
                        break;
                    default:
                        throw new IllegalArgumentException("Klisk race does not define ship of type " + shipType);
                }
                ship.setLoot(defaultLootTable);
                return ship;
            }});
            StarSystem kliskHomeworld = generateKliskHomeworld(world, 15, 15, kliskRace);
            kliskRace.setHomeworld(kliskHomeworld);

            world.addListener(new StandardAlienShipEvent(kliskRace));
            final GalaxyMapObject solar_system = (GalaxyMapObject) world.getGlobalVariables().get("solar_system");
            world.getGalaxyMap().addObjectAtDistance(kliskHomeworld, solar_system, Configuration.getIntProperty("world.galaxy.klisk_homeworld_distance"));
            world.getRaces().put(kliskRace.getName(), kliskRace);
            world.getGlobalVariables().put("klisk.homeworld", kliskHomeworld.getCoordsString());

        }
    }

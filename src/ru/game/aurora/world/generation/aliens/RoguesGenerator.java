/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 14:37
 */

package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.NextDialogListener;
import ru.game.aurora.npc.*;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.quest.EmbassiesQuest;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.*;

import java.util.Map;

public class RoguesGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -8911801330633122269L;

    public static final String NAME = "Rogues";

    public static final int SCOUT_SHIP = 0;

    public static final int PROBE_SHIP = 1;

    private static final ProbabilitySet<GameObject> defaultLootTable;

    static {
        defaultLootTable = new ProbabilitySet<>();
        defaultLootTable.put(new SpaceDebris.ResourceDebris(5), 1.0);
        defaultLootTable.put(new SpaceDebris.ResourceDebris(10), 0.2);
    }


    private Dialog createFrameDialog(final NPCShip frame) {

        Dialog frameDialog = Dialog.loadFromFile("dialogs/rogues/rogues_mothership_first_time.json");
        Dialog admiralDialog = Dialog.loadFromFile("dialogs/rogues/rogues_admiral_first.json");
        frameDialog.addListener(new NextDialogListener(admiralDialog));


        admiralDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -1121310501064131337L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getGlobalVariables().put("diplomacy.rogues_visited", 0);
                EmbassiesQuest.updateJournal(world, "rogues");
                Dialog newFrameDefault = Dialog.loadFromFile("dialogs/rogues/rogues_admiral_default.json");
                newFrameDefault.addListener(new RoguesMainDialogListener());
                frame.setCaptain(new NPC(newFrameDefault));

                world.addOverlayWindow(newFrameDefault);
            }
        });

        return frameDialog;
    }

    private StarSystem generateRoguesWorld(World world, int x, int y, AlienRace roguesRace) {
        BasePlanet[] planets = new BasePlanet[2];
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.red), x, y);

        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        HomeworldGenerator.setCoord(planets[0], 2);

        planets[1] = new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[1], 5);

        ss.setPlanets(planets);
        ss.setRadius(8);

        NPCShip frame = new NPCShip("rogues_frame", 2, 2);
        frame.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), ResourceManager.getInstance().getWeapons().getEntity("long_range_plasma_cannon"));
        frame.setCaptain(new NPC(createFrameDialog(frame)));
        frame.setScanDescription("races", "Rogues.mothership.description");
        frame.setAi(null);
        ss.getShips().add(frame);

        for (int i = 0; i < 3; ++i) {
            NPCShip probe = roguesRace.getDefaultFactory().createShip(world, RoguesGenerator.PROBE_SHIP);
            probe.setPos(CommonRandom.getRandom().nextInt(6) - 3, CommonRandom.getRandom().nextInt(6) - 3);
            probe.setAi(new LeaveSystemAI());
            ss.getShips().add(probe);
        }
        ss.setQuestLocation(true);
        return ss;
    }

    @Override
    public void updateWorld(World world) {
        Dialog defaultDialog = Dialog.loadFromFile("dialogs/rogues/rogues_default.json");
        final AlienRace rogueRace = new AlienRace(NAME, "rogues_scout", defaultDialog);

        rogueRace.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.COMPUTERS, rogueRace)), 0.4);
        rogueRace.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.ENERGY, rogueRace)), 0.4);
        rogueRace.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.GOODS, rogueRace)), 0.4);
        rogueRace.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.MATERIALS, rogueRace)), 0.3);
        rogueRace.getDefaultLootTable().put(new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.WEAPONS, rogueRace)), 0.2);

        rogueRace.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = 1334986755758313061L;

            @Override
            public NPCShip createShip(World world, int shipType) {
                NPCShip ship;
                switch (shipType) {
                    case SCOUT_SHIP: {
                        ship = new NPCShip("rogues_scout");
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), ResourceManager.getInstance().getWeapons().getEntity("long_range_plasma_cannon"));
                        break;
                    }
                    case PROBE_SHIP: {
                        ship = new NPCShip("rogues_probe");
                        ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("long_range_plasma_cannon"));
                        ship.setCanBeHailed(false);
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unsupported ship type for Rogues race: " + shipType);
                }
                ship.setLoot(defaultLootTable);
                return ship;

            }
        });

        StarSystem homeworld = generateRoguesWorld(world, 0, 0, rogueRace);
        world.getGalaxyMap().addObjectAtDistance(homeworld, (Positionable) world.getGlobalVariables().get("solar_system"), 20 + CommonRandom.getRandom().nextInt(Configuration.getIntProperty("world.galaxy.rogues_homeworld_distance")));
        world.getGlobalVariables().put("rogues.homeworld", homeworld.getCoordsString());
        homeworld.setQuestLocation(true);
        rogueRace.setHomeworld(homeworld);
        world.addListener(new StandardAlienShipEvent(rogueRace));
        world.addListener(new SingleStarsystemShipSpawner(rogueRace.getDefaultFactory(), 0.3, homeworld));

        world.getFactions().put(rogueRace.getName(), rogueRace);
    }

}

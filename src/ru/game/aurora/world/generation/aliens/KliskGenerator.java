/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:12
 */
package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.Color;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.NextDialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.space.*;

import java.util.Map;

/**
 * Creates Klisk alien race
 */
public class KliskGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -6983386879381885934L;

    public static final String NAME = "Klisk";

    // ship IDs used in factory generation
    public static final int DEFAULT_SHIP = 0;

    private static AlienRace kliskRace;

    private Dialog createDefaultKliskPlanetDialog(World world)
    {
        Dialog d = Dialog.loadFromFile("dialogs/klisk/klisk_planet_default.json");
        d.setListener(new DialogListener() {
            private static final long serialVersionUID = 4082728827280648178L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (flags.containsKey("klisk.war_help")) {
                    world.getGlobalVariables().put("klisk.war_help", true);
                }

                if (flags.containsKey("klisk_trader_drone.withdraw")) {
                    // remove trader drone
                }

                if (world.getGlobalVariables().containsKey("klisk_trade.result")) {
                    int repDelta = 0;
                    switch ((String)world.getGlobalVariables().get("klisk_trade.result")) {
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

                    world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, repDelta);
                    world.getGlobalVariables().remove("klisk_trade.result");
                }

            }
        });

        return d;
    }

    private void beginTradeQuest(World world, AlienHomeworld kliskPlanet)
    {
        NPCShip ship = world.getRaces().get(KliskGenerator.NAME).getDefaultFactory().createShip(0);
        ship.setCaptain(new NPC(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_ship_default.json")));

        ship.setAi(new LeaveSystemAI());
        ship.setPos(kliskPlanet.getX() - 1, kliskPlanet.getY() + 1);
        world.getCurrentStarSystem().getShips().add(ship);

        world.addListener(new GameEventListener() {
            private static final long serialVersionUID = -4786822024248669833L;

            @Override
            public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {

                Dialog start = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_captain.json");
                start.setListener(new KliskTradequestDialogListener());
                world.addOverlayWindow(start);
                return true;
            }
        });
    }

    private Dialog createPlanetDialogAndQuests(final AlienHomeworld kliskPlanet)
    {
        Dialog startDialog = Dialog.loadFromFile("dialogs/klisk/klisk_station_start.json");

        Dialog ambassadorDialog = Dialog.loadFromFile("dialogs/klisk/klisk_station_main.json");
        startDialog.setListener(new NextDialogListener(ambassadorDialog));
        ambassadorDialog.setListener(new DialogListener() {
            private static final long serialVersionUID = 4082728827280648178L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (returnCode == 0) {
                    // no quest
                    world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, -1);

                } else {
                    // accepts a quest
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_start.json"));
                    beginTradeQuest(world, kliskPlanet);
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
        ((AlienHomeworld)planets[1]).setDialog(createPlanetDialogAndQuests((AlienHomeworld) planets[1]));
        HomeworldGenerator.setCoord(planets[1], 3);

        planets[2] = new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[2], 5);

        AlienArtifact a = new AlienArtifact(3, 4, "small_artifact", new ArtifactResearch(new ResearchReport("small_artifact", "klisk_banner.report")));
        ((Planet) planets[2]).setNearestFreePoint(a, 2, 2);
        ((Planet) planets[2]).getPlanetObjects().add(a);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile("dialogs/klisk_1.json");
        kliskRace = new AlienRace(NAME, "klisk_ship", mainDialog);
        mainDialog.setListener(new DialogListener() {
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

                switch (returnCode) {
                    case 2:
                    case 4:
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
                newDefaultDialog.setListener(new KliskMainDialogListener(kliskRace));
                kliskRace.setDefaultDialog(newDefaultDialog);
            }
        });

        StarSystem kliskHomeworld = generateKliskHomeworld(world, 15, 15, kliskRace);
        kliskRace.setHomeworld(kliskHomeworld);

        world.addListener(new StandardAlienShipEvent(kliskRace));
        world.getGalaxyMap().getObjects().add(kliskHomeworld);
        world.getGalaxyMap().setTileAt(15, 15, world.getGalaxyMap().getObjects().size() - 1);

        world.getRaces().put(kliskRace.getName(), kliskRace);

        kliskRace.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = 5473066320214324094L;

            @Override
            public NPCShip createShip(int shipType) {
                if (shipType == DEFAULT_SHIP) {
                    NPCShip ship = new NPCShip(0, 0, "klisk_ship", kliskRace, null, "Klisk Ship");
                    ship.setHp(15);
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_small_laser"), ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
                    return ship;
                }

                throw new IllegalArgumentException("Klisk race does not define ship of type " + shipType);
            }
        });
    }
}

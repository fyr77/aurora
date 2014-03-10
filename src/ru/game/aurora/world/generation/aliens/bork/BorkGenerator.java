package ru.game.aurora.world.generation.aliens.bork;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.NextDialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPCShipFactory;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.EmbassiesQuest;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.space.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.10.13
 * Time: 17:12
 */
public class BorkGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = 3878128826545719756L;

    public static final String NAME = "Bork";


    private Dialog generatePlanetDialog(World world, final AlienRace borkRace) {
        Dialog landDialog = Dialog.loadFromFile("dialogs/bork/bork_planet_land.json");
        Dialog transferDialog = Dialog.loadFromFile("dialogs/bork/bork_planet_transfer.json");
        Dialog testDialog = Dialog.loadFromFile("dialogs/bork/bork_embassy_test.json");
        landDialog.addListener(new NextDialogListener(transferDialog));
        transferDialog.addListener(new NextDialogListener(testDialog));

        testDialog.addListener(new DialogListener() {

            private static final long serialVersionUID = 6603409563932739582L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                Dialog nextDialog;
                AlienRace humanity = world.getRaces().get(HumanityGenerator.NAME);
                String variableValue;
                switch (returnCode) {
                    case 1:
                        variableValue = "kill";
                        world.getReputation().updateReputation(borkRace.getName(), humanity.getName(), 1);
                        nextDialog = Dialog.loadFromFile("dialogs/bork/bork_embassy_test_kill.json");
                        break;
                    case 2:
                        variableValue = "injure";
                        world.getReputation().setReputation(borkRace.getName(), humanity.getName(), 8);
                        nextDialog = Dialog.loadFromFile("dialogs/bork/bork_embassy_test_injure.json");
                        break;
                    case 3:
                        variableValue = "miss";
                        nextDialog = Dialog.loadFromFile("dialogs/bork/bork_embassy_test_miss.json");
                        break;
                    default:
                        throw new IllegalStateException("Unknown bork dialog return value " + returnCode);
                }
                EmbassiesQuest.updateJournal(world, "bork_" + variableValue);
                world.getGlobalVariables().put("bork.diplomacy_test", variableValue);
                world.getGlobalVariables().put("diplomacy.bork_visited", 0);
                world.addOverlayWindow(nextDialog);
                nextDialog.setFlags(flags);

                BorkDialogListener listener = new BorkDialogListener();
                nextDialog.addListener(listener);

                Dialog newDefaultDialog = Dialog.loadFromFile("dialogs/bork/bork_planet_default.json");
                newDefaultDialog.addListener(listener);
                ((AlienHomeworld) borkRace.getHomeworld().getPlanets()[4].getSatellites().get(0)).setDialog(newDefaultDialog);
            }
        });
        return landDialog;
    }


    @Override
    public void updateWorld(World world) {
        Dialog mainDialog = Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/bork/bork_default_not_aggressive.json"));
        mainDialog.addListener(new BorkShipDialogListener());
        // TODO: generate agressive bork ships
        final AlienRace borkRace = new AlienRace(NAME, "bork_ship", mainDialog);


        BasePlanet[] planets = new BasePlanet[5];
        StarSystem ss = new StarSystem("Bork homeworld", new Star(1, Color.white), 13, 2);
        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        HomeworldGenerator.setCoord(planets[0], 2);
        planets[1] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[1], 3);

        planets[2] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.BREATHABLE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[2], 5);
        planets[2].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));

        planets[3] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[3], 7);

        planets[4] = new GasGiant(0, 0, ss);
        HomeworldGenerator.setCoord(planets[4], 9);
        planets[4].setRings(1);

        Dialog homeworldDialog = generatePlanetDialog(world, borkRace);
        planets[4].addSatellite(new AlienHomeworld("klisk_homeworld", borkRace, homeworldDialog, 1, 0, ss, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 0, PlanetCategory.PLANET_ROCK));

        ss.setPlanets(planets);
        ss.setRadius(Math.max((int) (12 * 1.5), 10));
        world.getGalaxyMap().addObjectAndSetTile(ss, 13, 20);
        world.getGlobalVariables().put("bork.homeworld", "[13, 20]");
        borkRace.setHomeworld(ss);
        ss.setQuestLocation(true);

        borkRace.setDefaultFactory(new NPCShipFactory() {
            private static final long serialVersionUID = 8000558666433188574L;

            @Override
            public NPCShip createShip(int shipId) {
                NPCShip ship = new NPCShip(0, 0, "bork_ship", borkRace, null, "Bork ship", 5);
                if (CommonRandom.getRandom().nextInt(3) == 1) {
                    // some random bork ships carry a missile launcher
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("bork_cannon"), ResourceManager.getInstance().getWeapons().getEntity("bork_missiles"));
                } else {
                    ship.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("bork_cannon"));
                }
                return ship;
            }
        });


        /**   mainDialog.setListener(new DialogListener() {

         private static final long serialVersionUID = -4476192487724362451L;

         @Override public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
         // after first dialog swarm becomes hostile
         borkRace.setRelation(world.getPlayer().getShip().getRace(), 0);
         }
         });*/

        world.addListener(new BorkShipGenerator(0.5, 3, null, borkRace.getDefaultFactory(), Configuration.getIntProperty("encounter.bork_pirates.count")));

        world.getRaces().put(borkRace.getName(), borkRace);
    }
}

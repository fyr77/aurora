package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.Color;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.SingleStarsystemShipSpawner;
import ru.game.aurora.npc.StandardAlienShipEvent;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

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

    private StarSystem generateHomeworld(World world, int x, int y, final AlienRace race) {
        final BasePlanet[] planets = new BasePlanet[1];
        final StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(2, Color.white), x, y);

        final Dialog initialHomeworldDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_homeworld_1.json");
        final Dialog continueDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_homeworld_2.json");
        final Dialog planetSightseeingDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_city_transfer.json");
        final Dialog zorsanFinalDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_before_attack.json");
        final Dialog escapeDialog = Dialog.loadFromFile("dialogs/zorsan/zorsan_escape.json");


        planets[0] = new AlienHomeworld("zorsan_homeworld", race, initialHomeworldDialog, 3, 0, ss, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 0, PlanetCategory.PLANET_ROCK);
        initialHomeworldDialog.setListener(new DialogListener() {

            private static final long serialVersionUID = 5653727064261130921L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (dialog == initialHomeworldDialog || dialog == continueDialog) {
                    if (returnCode == 0) {
                        continueDialog.setListener(this);
                        ((AlienHomeworld)planets[0]).setDialog(continueDialog);
                    }

                    if (returnCode == 1) {
                        // descending to planet
                        planetSightseeingDialog.setListener(this);
                        world.addOverlayWindow(planetSightseeingDialog);
                    }
                } else if (dialog == planetSightseeingDialog) {
                    Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/zor_escape.tmx"), ss);
                    dungeon.setEnterDialog(zorsanFinalDialog);
                    dungeon.setSuccessDialog(escapeDialog);
                    dungeon.setCommanderInParty(true); // loosing this dungeon will lead to a gameover
                    dungeon.enter(world);
                    world.setCurrentRoom(dungeon);

                    zorsanFinalDialog.setFlags(dialog.getFlags()); // pass flags from previous dialog to a next one
                    race.setRelation(world.getRaces().get(HumanityGenerator.NAME), 0);
                }
            }
        });



        HomeworldGenerator.setCoord(planets[0], 3);

        ss.setPlanets(planets);
        ss.setQuestLocation(true);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }


    @Override
    public void updateWorld(World world) {
        AlienRace race = new AlienRace(NAME, "zorsan_scout", Dialog.loadFromFile("dialogs/zorsan_main.json"));
        StarSystem homeworld = generateHomeworld(world, 3, 8, race);
        world.getGlobalVariables().put("zorsan.homeworld", String.format("[%d, %d]", homeworld.getGlobalMapX(), homeworld.getGlobalMapY()));
        world.getGalaxyMap().addObjectAndSetTile(homeworld, 3, 8);
        world.addListener(new StandardAlienShipEvent(race));
        world.addListener(new SingleStarsystemShipSpawner(race.getDefaultFactory(), 0.5, race.getHomeworld()));
        race.setHomeworld(homeworld);
        race.setTravelDistance(5);
        world.getRaces().put(race.getName(), race);
    }
}
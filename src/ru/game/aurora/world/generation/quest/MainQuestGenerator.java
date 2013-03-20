package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.03.13
 * Time: 14:05
 */
public class MainQuestGenerator implements WorldGeneratorPart {

    /**
     * Checks that player has entered a 'cloned' star system and shows dialog in proper cases
     */
    private static final class MainQuestSystemEnterListener extends GameEventListener {
        private static final long serialVersionUID = -1300971040110944699L;

        private int count = 0;

        @Override
        public boolean isAlive() {
            return count < 3;
        }

        @Override
        public void onPlayerEnterStarSystem(World world, StarSystem ss) {
            if (!ss.getVariables().containsKey(CLONED_SYSTEM_PROPERTY)) {
                return;
            }

            if (ss.isVisited()) {
                return;
            }

            ++count;

            if (count == 2) {
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/main/second_cloned_system_found.json"));
                world.getGlobalVariables().put("quest.main.cloned_starsystems_encountered", "2");
                return;
            }

            if (count == 3) {
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/main/third_cloned_system_found.json"));
                world.getGlobalVariables().put("quest.main.cloned_starsystems_encountered", "3");
            }
        }
    }

    private static final long serialVersionUID = -6652475850606604775L;

    private static final int CLONED_SYSTEM_COUNT = 5;

    private static final String CLONED_SYSTEM_PROPERTY = "quest.main.cloned";

    @Override
    public void updateWorld(World world) {
        // generate 5 cloned star systems
        // player must visit at least 2 of them
        StarSystem original = createStarSystem(0, 0);
        for (int i = 0; i < CLONED_SYSTEM_COUNT; ++i) {
            int x;
            int y;
            do {
                x = CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesX());
                y = CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesX());
            } while (world.getGalaxyMap().getObjectAt(x, y) != null);

            final StarSystem ss = cloneStarSystem(original, x, y);
            world.getGalaxyMap().addObjectAndSetTile(ss, x, y);

            if (i == 0) {
                // this system is currently in process of terraforming, Obliterator is there
                ss.setBackgroundSprite("obliterator_background");
                ss.setFirstEnterDialog(Dialog.loadFromFile("dialogs/quest/main/obliterator_encountered.json"));
                ss.setQuestLocation(true);
                world.addListener(new GameEventListener() {
                    private static final long serialVersionUID = -8345545783110990443L;

                    @Override
                    public void onPlayerEnterStarSystem(World world, StarSystem s) {
                        if (s == ss) {
                            world.getGlobalVariables().put("quest.main.obliterator_encountered", null);
                            isAlive = false;
                            world.getPlayer().getEarthState().getMessages().add(
                                    new PrivateMessage(
                                            "Greatest known artificial structure discovered"
                                            ,"UNS " + world.getPlayer().getShip().getName() + " has encountered the biggest artificial structure ever seen by man in a distant star system. A huge object of" +
                                            "unknown origin, larger than a star can be seen on these amazing photos. Who has created it? What is its purpose? What power does it have? Read more in next issue of 'Galaxy News'."
                                            ,"news"
                                    )
                            );
                        }
                    }
                });
            } else {
                ss.getVariables().put(CLONED_SYSTEM_PROPERTY, null);
            }
        }

        world.addListener(new MainQuestSystemEnterListener());
    }

    public static StarSystem createStarSystem(int x, int y) {

        Planet[] planets = new Planet[3];
        StarSystem ss = new StarSystem(new StarSystem.Star(2, Color.green), 9, 9);

        planets[0] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0, false);
        HomeworldGenerator.setCoord(planets[0], 2);

        // venus
        planets[1] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0, false);
        HomeworldGenerator.setCoord(planets[1], 3);


        // mars
        planets[2] = new Planet(ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0, false);
        HomeworldGenerator.setCoord(planets[2], 5);


        ss.setPlanets(planets);
        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }

    private StarSystem cloneStarSystem(StarSystem proto, int x, int y) {
        BasePlanet[] planets = new BasePlanet[3];
        StarSystem ss = new StarSystem(new StarSystem.Star(2, Color.green), x, y);

        planets[0] = new Planet(ss, (Planet) proto.getPlanets()[0]);
        HomeworldGenerator.setCoord(planets[0], 2);

        planets[1] = new Planet(ss, (Planet) proto.getPlanets()[1]);
        HomeworldGenerator.setCoord(planets[1], 3);

        planets[2] = new Planet(ss, (Planet) proto.getPlanets()[2]);
        HomeworldGenerator.setCoord(planets[2], 5);

        ss.setPlanets(planets);

        ss.setRadius(Math.max((int) (6 * 1.5), 10));
        return ss;
    }
}

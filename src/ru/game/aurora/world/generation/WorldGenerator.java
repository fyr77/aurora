/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.01.13
 * Time: 16:13
 */
package ru.game.aurora.world.generation;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GlobalThreadPool;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.GardenerGenerator;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.artifacts.BuildersRuinGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.InitialRadioEmissionQuestGenerator;
import ru.game.aurora.world.generation.quest.MainQuestGenerator;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * Generates world in separate thread
 */
public class WorldGenerator implements Runnable {
    private String currentStatus = "Initializing";

    public static final int maxStars = 30;

    public static final int worldWidth = 100;

    public static final int worldHeight = 100;

    private World world;

    private static final WorldGeneratorPart[] questGenerators = {
            new InitialRadioEmissionQuestGenerator()
            , new MainQuestGenerator()
    };

    private static final WorldGeneratorPart[] alienGenerators = {
            new KliskGenerator()
            , new GardenerGenerator()
            , new HumanityGenerator()
            , new RoguesGenerator()
    };

    private static final WorldGeneratorPart[] otherGenerators = {
            new BuildersRuinGenerator()
            , new TutorialGenerator()
    };

    private void createAliens(World world) {
        currentStatus = "Creating aliens";
        for (WorldGeneratorPart part : alienGenerators) {
            part.updateWorld(world);
        }
    }

    private void createArtifactsAndAnomalies(World world) {
        currentStatus = "Creating artifacts and anomalies";
        for (WorldGeneratorPart part : otherGenerators) {
            part.updateWorld(world);
        }
    }

    private void generateMap(final World world) {
        currentStatus = "Generating star systems";
        List<Future> futures = new ArrayList<>(maxStars);
        // now generate random star systems
        for (int i = 0; i < maxStars; ++i) {
            futures.add(GlobalThreadPool.getExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int x;
                        int y;
                        do {
                            x = CommonRandom.getRandom().nextInt(worldWidth);
                            y = CommonRandom.getRandom().nextInt(worldHeight);
                        } while (world.getGalaxyMap().getObjectAt(x, y) != null);
                        StarSystem ss = generateRandomStarSystem(world, x, y);

                        synchronized (world) {
                            final int idx = world.getGalaxyMap().getObjects().size();
                            world.getGalaxyMap().getObjects().add(ss);
                            world.getGalaxyMap().setTileAt(x, y, idx);
                        }
                    } catch (Throwable t) {
                        System.err.println("Failed to generate world");
                        t.printStackTrace();
                    }
                }
            }));
        }


        while (!futures.isEmpty()) {
            try {
                for (Iterator<Future> iter = futures.iterator(); iter.hasNext();) {
                    Future f = iter.next();
                    if (f.isDone()) {
                        iter.remove();
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // nothing
            }
        }
    }

    public static StarSystem generateRandomStarSystem(World world, int x, int y) {
        final Random r = CommonRandom.getRandom();

        int size = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        Color starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        final int planetCount = r.nextInt(5);
        Planet[] planets = new Planet[planetCount];
        int maxRadius = 0;
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new StarSystem.Star(size, starColor), x, y);

        int astroData = 20 * size;

        for (int i = 0; i < planetCount; ++i) {
            int radius = r.nextInt(planetCount * StarSystem.PLANET_SCALE_FACTOR) + StarSystem.STAR_SCALE_FACTOR;
            maxRadius = Math.max(radius, maxRadius);
            int planetX = r.nextInt(2 * radius) - radius;

            int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (r.nextBoolean() ? -1 : 1));
            PlanetAtmosphere atmosphere = CollectionUtils.selectRandomElement(PlanetAtmosphere.values());
            final int planetSize = r.nextInt(3) + 1;
            planets[i] = new Planet(
                    ss
                    , CollectionUtils.selectRandomElement(PlanetCategory.values())
                    , atmosphere
                    , planetSize
                    , planetX
                    , planetY
                    , atmosphere != PlanetAtmosphere.NO_ATMOSPHERE);
            astroData += 10 * planetSize;
        }
        ss.setPlanets(planets);
        astroData += r.nextInt(30);
        ss.setAstronomyData(astroData);
        ss.setRadius(Math.max((int) (maxRadius * 1.5), 10));
        return ss;
    }

    private void createQuestWorlds(World world) {
        currentStatus = "Creating quests";
        for (WorldGeneratorPart part : questGenerators) {
            part.updateWorld(world);
        }
    }

    @Override
    public void run() {
        World world = new World(worldWidth, worldHeight);

        generateMap(world);
        createAliens(world);
        createArtifactsAndAnomalies(world);
        createQuestWorlds(world);

        currentStatus = "All done";

        this.world = world;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public World getWorld() {
        return world;
    }

    public boolean isGenerated() {
        return world != null;
    }
}

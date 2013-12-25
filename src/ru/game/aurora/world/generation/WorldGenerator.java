/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.01.13
 * Time: 16:13
 */
package ru.game.aurora.world.generation;

import org.newdawn.slick.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GlobalThreadPool;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.CrewChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.*;
import ru.game.aurora.world.generation.artifacts.BuildersRuinGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.InitialRadioEmissionQuestGenerator;
import ru.game.aurora.world.generation.quest.LastBeaconQuestGenerator;
import ru.game.aurora.world.generation.quest.MainQuestGenerator;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.quest.Journal;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.Star;
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
    private static final Logger logger = LoggerFactory.getLogger(WorldGenerator.class);

    private String currentStatus = "Initializing";

    public static final PlanetCategory[] satelliteCategories = {PlanetCategory.PLANET_ROCK, PlanetCategory.PLANET_ICE};

    private World world;

    private static final WorldGeneratorPart[] questGenerators = {
            new InitialRadioEmissionQuestGenerator()
            , new MainQuestGenerator()
            , new LastBeaconQuestGenerator()
    };

    private static final WorldGeneratorPart[] alienGenerators = {
            new KliskGenerator()
            , new GardenerGenerator()
            , new HumanityGenerator()
            , new RoguesGenerator()
            , new BorkGenerator()
            , new ZorsanGenerator()
    };

    private static final WorldGeneratorPart[] otherGenerators = {
            new BuildersRuinGenerator()
            , new TutorialGenerator()
    };

    private void createAliens(World world) {
        currentStatus = Localization.getText("gui", "generation.aliens");
        for (WorldGeneratorPart part : alienGenerators) {
            part.updateWorld(world);
        }
    }

    private void createArtifactsAndAnomalies(World world) {
        currentStatus = Localization.getText("gui", "generation.artifacts");
        for (WorldGeneratorPart part : otherGenerators) {
            part.updateWorld(world);
        }
    }

    private void generateMap(final World world) {
        currentStatus = Localization.getText("gui", "generation.stars");
        final int maxStars = Configuration.getIntProperty("world.galaxy.maxStars");
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
                            x = CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesX());
                            y = CommonRandom.getRandom().nextInt(world.getGalaxyMap().getTilesY());
                        } while (world.getGalaxyMap().getObjectAt(x, y) != null);
                        StarSystem ss = generateRandomStarSystem(world, x, y);

                        synchronized (world) {
                            final int idx = world.getGalaxyMap().getObjects().size();
                            world.getGalaxyMap().getObjects().add(ss);
                            world.getGalaxyMap().setTileAt(x, y, idx);
                        }
                    } catch (Throwable t) {
                        logger.error("Failed to generate world", t);
                    }
                }
            }));
        }


        while (!futures.isEmpty()) {
            try {
                for (Iterator<Future> iter = futures.iterator(); iter.hasNext(); ) {
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

    public static StarSystem generateRandomStarSystem(World world, int x, int y, int planetCount)
    {
        final Random r = CommonRandom.getRandom();

        int starSize = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        Color starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        BasePlanet[] planets = new BasePlanet[planetCount];
        int maxRadius = 0;
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(starSize, starColor), x, y);

        int astroData = 20 * starSize;

        final double ringsChance = Configuration.getDoubleProperty("world.starsystem.planetRingsChance");
        final int maxSatellites = Configuration.getIntProperty("world.starsystem.maxSatellites");

        for (int i = 0; i < planetCount; ++i) {
            int radius = r.nextInt(planetCount * StarSystem.PLANET_SCALE_FACTOR) + StarSystem.STAR_SCALE_FACTOR;
            maxRadius = Math.max(radius, maxRadius);
            int planetX = r.nextInt(2 * radius) - radius;

            int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (r.nextBoolean() ? -1 : 1));
            PlanetAtmosphere atmosphere = CollectionUtils.selectRandomElement(PlanetAtmosphere.values());
            final int planetSize = r.nextInt(3) + 1;
            astroData += 10 * planetSize;
            PlanetCategory cat = CollectionUtils.selectRandomElement(PlanetCategory.values());
            if (cat == PlanetCategory.GAS_GIANT) {
                // no gas giants on inner orbits
                if (i < 2) {
                    cat = PlanetCategory.PLANET_ROCK;
                } else {
                    planets[i] = new GasGiant(planetX, planetY, ss);
                    continue;
                }
            }
            planets[i] = new Planet(
                    world,
                    ss
                    , cat
                    , atmosphere
                    , planetSize
                    , planetX
                    , planetY
            );
            if (atmosphere != PlanetAtmosphere.NO_ATMOSPHERE) {
                PlanetaryLifeGenerator.setPlanetHasLife((Planet) planets[i]);
            }

            // only large planets have rings and satellites

            if (planetSize <= 2) {
                if (r.nextDouble() < ringsChance) {
                    planets[i].setRings(r.nextInt(Configuration.getIntProperty("world.starsystem.ringsTypes")) + 1);
                }

                int satelliteCount = r.nextInt(maxSatellites);
                for (int i1 = 1; i1 < satelliteCount + 1; ++i1) {
                    atmosphere = CollectionUtils.selectRandomElement(PlanetAtmosphere.values());
                    astroData += 10;
                    cat = CollectionUtils.selectRandomElement(satelliteCategories);
                    final Planet satellite = new Planet(world, ss, cat, atmosphere, 4, 0, 0);
                    planets[i].addSatellite(satellite);
                }

            }

        }
        ss.setPlanets(planets);
        astroData += r.nextInt(30);
        ss.setAstronomyData(astroData);
        ss.setRadius(Math.max((int) (maxRadius * 1.5), 10));
        return ss;
    }

    public static StarSystem generateRandomStarSystem(World world, int x, int y) {
        final int planetCount = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("world.starsystem.maxPlanets"));
        return generateRandomStarSystem(world, x, y, planetCount);
    }

    private void createQuestWorlds(World world) {
        currentStatus = Localization.getText("gui", "generation.quests");
        for (WorldGeneratorPart part : questGenerators) {
            part.updateWorld(world);
        }
    }

    private void createMisc(World world)
    {
        Journal journal = world.getPlayer().getJournal();
        journal.addCodex(new JournalEntry("aurora_desc", "1"));
        journal.addCodex(new JournalEntry("engineer_dossier", "1"));
        journal.addCodex(new JournalEntry("scientist_dossier", "1"));
        journal.addCodex(new JournalEntry("military_dossier", "1"));

        journal.addQuest(new JournalEntry("colony_search", "start"));
        journal.addQuest(new JournalEntry("last_beacon", "start"));
    }

    @Override
    public void run() {
        try {
            World world = new World(Configuration.getIntProperty("world.galaxy.width"), Configuration.getIntProperty("world.galaxy.height"));
            world.addListener(new CrewChangeListener());
            generateMap(world);
            createAliens(world);
            createArtifactsAndAnomalies(world);
            createQuestWorlds(world);
            createMisc(world);

            currentStatus = Localization.getText("gui", "generation.done");

            this.world = world;
        } catch (Exception ex) {
            logger.error("Failed to generate world", ex);
            System.exit(-1);
        }
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

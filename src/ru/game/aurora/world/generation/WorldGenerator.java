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
import ru.game.aurora.application.*;
import ru.game.aurora.music.StarSystemMusicChangeListener;
import ru.game.aurora.npc.AlienRaceFirstCommunicationListener;
import ru.game.aurora.npc.factions.FreeForAllFaction;
import ru.game.aurora.npc.factions.NeutralFaction;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.CrewChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.*;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.bork.FamilyProblemsEventGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanRebelsFirstQuestGenerator;
import ru.game.aurora.world.generation.artifacts.BuildersRuinGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.*;
import ru.game.aurora.world.generation.quest.heritage.HeritageQuestGenerator;
import ru.game.aurora.world.generation.quest.inside.InsideEncounterGenerator;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.quest.FasterThanLightQuestGenerator;
import ru.game.aurora.world.quest.Journal;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.EarthUpgradeUnlocker;

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

    private boolean completed = false;

    private static final WorldGeneratorPart[] questGenerators = {
            new InitialRadioEmissionQuestGenerator()
            , new MainQuestGenerator()
            , new LastBeaconQuestGenerator()
            , new ColonyPlanetSearchListener()
            , new EmbassiesQuest()
            , new DamagedRoguesScoutEventGenerator()
            , new EnergySphereEncounterGenerator()
            , new ZorsanRebelsFirstQuestGenerator()
            , new FasterThanLightQuestGenerator()
            , new FamilyProblemsEventGenerator()
            , new InsideEncounterGenerator()
            , new RedMeatEncounterGenerator()
            , new HeritageQuestGenerator()
    };

    private static final WorldGeneratorPart[] alienGenerators = {
            new HumanityGenerator()
            , new KliskGenerator()
            , new GardenerGenerator()
            , new RoguesGenerator()
            , new BorkGenerator()
            , new ZorsanGenerator()
    };

    private static final WorldGeneratorPart[] otherGenerators = {
            new BuildersRuinGenerator()
            , new TutorialGenerator()
            , new ResearchProjectsGenerator()
    };

    private void createAliens(World world) {
        currentStatus = Localization.getText("gui", "generation.aliens");
        for (WorldGeneratorPart part : alienGenerators) {
            part.updateWorld(world);
        }

        world.getFactions().put("freeforall", new FreeForAllFaction());
        world.getFactions().put("neutral", new NeutralFaction());
        world.addListener(new AlienRaceFirstCommunicationListener());
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
                            final int idx = world.getGalaxyMap().getGalaxyMapObjects().size();
                            world.getGalaxyMap().getGalaxyMapObjects().add(ss);
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

    public static StarSystem generateRandomStarSystem(Star star, World world, int x, int y, int planetCount) {
        final Random r = CommonRandom.getRandom();
        boolean belt = false;

        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), star, x, y);

        int astroData = 20 * star.size;

        final double ringsChance = Configuration.getDoubleProperty("world.starsystem.planetRingsChance");
        final int maxSatellites = Configuration.getIntProperty("world.starsystem.maxSatellites");

        int prevRadius = StarSystem.STAR_SCALE_FACTOR;
        int maxRadius = prevRadius;

        List<BasePlanet> planetList = new ArrayList<>(planetCount);

        for (int i = 0; i < planetCount; ++i) {
            int radius = prevRadius + r.nextInt(StarSystem.PLANET_SCALE_FACTOR) + StarSystem.PLANET_SCALE_FACTOR;
            prevRadius = radius;
            maxRadius = Math.max(radius, maxRadius);
            int planetX = r.nextInt(2 * radius) - radius;

            int planetY = (int) (Math.sqrt(radius * radius - planetX * planetX) * (r.nextBoolean() ? -1 : 1));
            PlanetAtmosphere atmosphere = CollectionUtils.selectRandomElement(PlanetAtmosphere.values());
            final int planetSize = r.nextInt(3) + 1;
            astroData += 10 * planetSize;
            PlanetCategory cat = CollectionUtils.selectRandomElement(PlanetCategory.values());
            if (cat == PlanetCategory.GAS_GIANT) {
                // no gas giants on inner orbits
                if (planetList.size() < 2) {
                    cat = r.nextBoolean() ? PlanetCategory.PLANET_ROCK : PlanetCategory.PLANET_FULL_STONE;
                } else {
                    double rnd = Math.random();
                    if (rnd < Configuration.getDoubleProperty("world.starsystem.asteroidBeltChance") && (planetCount > 1) && !belt) {
                        int width = r.nextInt(2) + 1;
                        belt = true;
                        planetCount--;
                        ss.setAsteroidBelt(radius, width);
                        prevRadius += (width - 1);
                    } else {
                        planetList.add(new GasGiant(planetX, planetY, ss, CollectionUtils.selectRandomElement(PlanetCategory.GasGiantColors.values())));
                    }
                    continue;
                }
            }
            final Planet e = new Planet(
                    world,
                    ss
                    , cat
                    , atmosphere
                    , planetSize
                    , planetX
                    , planetY
            );
            if (atmosphere != PlanetAtmosphere.NO_ATMOSPHERE && CommonRandom.getRandom().nextBoolean()) {
                PlanetaryLifeGenerator.setPlanetHasLife(e);
            }
            addEnvironmentDangers(e);
            planetList.add(e);


            // only large planets have rings and satellites

            if (planetSize <= 2) {
                if (r.nextDouble() < ringsChance) {
                    e.setRings(r.nextInt(Configuration.getIntProperty("world.starsystem.ringsTypes")) + 1);
                }

                int satelliteCount = r.nextInt(maxSatellites);
                for (int i1 = 1; i1 < satelliteCount + 1; ++i1) {
                    atmosphere = CollectionUtils.selectRandomElement(PlanetAtmosphere.values());
                    astroData += 10;
                    cat = CollectionUtils.selectRandomElement(satelliteCategories);
                    final Planet satellite = new Planet(world, ss, cat, atmosphere, 4, 0, 0);
                    e.addSatellite(satellite);
                }

            }
        }

        ss.setPlanets(planetList.toArray(new BasePlanet[planetList.size()]));
        astroData += r.nextInt(30);
        ss.setAstronomyData(astroData);
        ss.setRadius(Math.max((int) (maxRadius * 1.5), 10));

        int rndNeb1 = r.nextInt(Configuration.getIntProperty("world.starsystem.nebulaeTypes") + 1); //0 - без туманности
        int rndNeb2;
        if (rndNeb1 == 0) {
            rndNeb2 = r.nextInt(Configuration.getIntProperty("world.starsystem.nebulaeTypes") + 1);
        } else {
            rndNeb2 = rndNeb1;
            //туманности не должны быть одинаковыми
            while (rndNeb2 == rndNeb1) {
                rndNeb2 = r.nextInt(Configuration.getIntProperty("world.starsystem.nebulaeTypes") + 1);
            }
        }

        if (rndNeb1 != 0) ss.setBackgroundNebula1("nebula" + rndNeb1);
        if (rndNeb2 != 0) ss.setBackgroundNebula2("nebula" + rndNeb2);

        return ss;
    }

    public static void addEnvironmentDangers(Planet e) {
        if (e.getAtmosphere() != PlanetAtmosphere.NO_ATMOSPHERE) {
            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.tornado.chance")) {
                e.addEnvironmentFlag(Environment.WIND);
            }

            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.rain.chance")) {
                e.addEnvironmentFlag(Environment.RAIN);
            }
            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.acid_rain.chance")) {
                e.addEnvironmentFlag(Environment.ACID_RAIN);
            }
        } else {
            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("environment.meteor.chance")) {
                e.addEnvironmentFlag(Environment.METEORS);
            }
        }

    }

    public static StarSystem generateRandomStarSystem(World world, int x, int y) {
        final int planetCount = CommonRandom.getRandom().nextInt(Configuration.getIntProperty("world.starsystem.maxPlanets"));
        return generateRandomStarSystem(world, x, y, planetCount);
    }

    public static StarSystem generateRandomStarSystem(World world, int x, int y, int planets) {
        int starSize = StarSystem.possibleSizes[CommonRandom.getRandom().nextInt(StarSystem.possibleSizes.length)];
        Color starColor = StarSystem.possibleColors[CommonRandom.getRandom().nextInt(StarSystem.possibleColors.length)];
        return generateRandomStarSystem(new Star(starSize, starColor), world, x, y, planets);
    }

    private void createQuestWorlds(World world) {
        currentStatus = Localization.getText("gui", "generation.quests");
        for (WorldGeneratorPart part : questGenerators) {
            part.updateWorld(world);
        }
    }

    private void createMisc(World world) {
        Journal journal = world.getPlayer().getJournal();
        journal.addCodex(new JournalEntry("aurora_desc", "1"));
        journal.addCodex(new JournalEntry("engineer_dossier", "1"));
        journal.addCodex(new JournalEntry("scientist_dossier", "1"));
        journal.addCodex(new JournalEntry("military_dossier", "1"));
        journal.addCodex(new JournalEntry("translator_device", "text"));

        journal.addQuest(new JournalEntry("colony_search", "start"));
        journal.addQuest(new JournalEntry("last_beacon", "start"));

        world.addListener(new EarthUpgradeUnlocker());

        world.addListener(new SaveGameManager.Autosaver());
        world.addListener(new StarSystemMusicChangeListener());
        world.addListener(new Environment.PlanetProcessor());
        world.addListener(new LoggingListener());
        world.addListener(new PlanetLifeUpdater());
    }

    // perform some fast initialization in gui thread
    public World initWorld() {
        world = new World(Configuration.getIntProperty("world.galaxy.width"), Configuration.getIntProperty("world.galaxy.height"));
        world.addListener(new CrewChangeListener());
        return world;
    }

    @Override
    public void run() {
        try {

            generateMap(world);
            createAliens(world);
            createArtifactsAndAnomalies(world);
            createQuestWorlds(world);
            createMisc(world);

            currentStatus = Localization.getText("gui", "generation.done");
            completed = true;
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
        return completed;
    }
}

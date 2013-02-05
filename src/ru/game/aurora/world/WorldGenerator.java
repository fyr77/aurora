/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.01.13
 * Time: 16:13
 */
package ru.game.aurora.world;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.gui.StoryScreen;
import ru.game.aurora.npc.*;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.quest.AuroraProbe;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Generates world in separate thread
 */
public class WorldGenerator implements Runnable {
    private String currentStatus = "Initializing";

    public static final int maxStars = 15;

    public static final int worldWidth = 100;

    public static final int worldHeight = 100;

    private World world;

    private ExecutorService executor = Executors.newFixedThreadPool(4);

    private void createAliens(World world) {
        currentStatus = "Creating aliens";

        AlienRace gardenerRace = null;
        AlienRace kliskRace = null;

        gardenerRace = new AlienRace("Gardeners", "gardener_ship", 8, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/gardener_default_dialog.json")));
        kliskRace = new AlienRace("Klisk", "klisk_ship", 8, Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/klisk_default_dialog.json")));
        StarSystem kliskHomeworld = HomeworldGenerator.generateKliskHomeworld(5, 5, kliskRace);
        kliskRace.setHomeworld(kliskHomeworld);

        NPCShip gardenerShip = new NPCShip(0, 0, gardenerRace.getShipSprite(), gardenerRace, null, null);
        gardenerShip.setAi(null);
        world.addListener(new SingleShipEvent(0.9, gardenerShip));
        world.addListener(new StandartAlienShipEvent(kliskRace));
        world.addListener(new SingleShipFixedTime(4, new AuroraProbe(0, 0), Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/aurora_probe_detected.json"))));

        world.getGalaxyMap().getObjects().add(kliskHomeworld);
        world.getGalaxyMap().setTileAt(5, 5, world.getGalaxyMap().getObjects().size() - 1);

        // earth
        StarSystem solarSystem = HomeworldGenerator.createSolarSystem();
        world.getGalaxyMap().getObjects().add(solarSystem);
        world.getGalaxyMap().setTileAt(9, 9, world.getGalaxyMap().getObjects().size() - 1);
    }

    private void generateMap(final World world) {
        currentStatus = "Generating star systems";

        // now generate random star systems
        for (int i = 0; i < maxStars; ++i) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int x;
                        int y;
                        do {
                            x = CommonRandom.getRandom().nextInt(worldWidth);
                            y = CommonRandom.getRandom().nextInt(worldHeight);
                        } while (world.getGalaxyMap().getObjectAt(x, y) != null);
                        StarSystem ss = generateRandomStarSystem(x, y);

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
            });
        }
        executor.shutdown();

        while (true) {
            try {
                if (executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
                // nothing
            }
        }
    }

    public static StarSystem generateRandomStarSystem(int x, int y) {
        final Random r = CommonRandom.getRandom();

        int size = StarSystem.possibleSizes[r.nextInt(StarSystem.possibleSizes.length)];
        Color starColor = StarSystem.possibleColors[r.nextInt(StarSystem.possibleColors.length)];
        final int planetCount = r.nextInt(5);
        Planet[] planets = new Planet[planetCount];
        int maxRadius = 0;
        StarSystem ss = new StarSystem(new StarSystem.Star(size, starColor), x, y);

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
        currentStatus = "Creating quest locations";
        // initial research projects and their star system
        StarSystem brownStar = generateRandomStarSystem(6, 7);
        brownStar.setStar(new StarSystem.Star(6, new Color(128, 0, 0)));

        final int idx = world.getGalaxyMap().getObjects().size();
        world.getGalaxyMap().getObjects().add(brownStar);
        world.getGalaxyMap().setTileAt(6, 7, idx);

        ResearchProjectDesc starInitialResearch = new StarResearchProject(brownStar);
        starInitialResearch.setReport(new ResearchReport("star_research", "This brown dwarf is unusual, as it actively emits radiowaves. Origin of this emission is currently unclear, and it is changing in time in a way that breaks all current theories concerning brown dwarves structure. " +
                "This star is small, and its surface temperature is only about 900K, which makes it look more like a gas giant than like a star. Tracking such stars from Solar system using long-range radio telescopes is very difficult due to their low contrast and great distance." +
                " \n Data collected by expedition can lead to better understanding of processes occurring inside these 'wannabe-stars'."));
        world.getPlayer().getResearchState().getAvailableProjects().add(starInitialResearch);
    }

    @Override
    public void run() {
        World world = new World(worldWidth, worldHeight);

        generateMap(world);
        createAliens(world);
        createQuestWorlds(world);

        world.addOverlayWindow(new StoryScreen("story/beginning.json"));
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

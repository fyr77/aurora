/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 18:46
 */
package ru.game.aurora.world.planet;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.nature.PlanetFloraAndFauna;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * Class for planet.
 * Draws itself on Star System map.
 * Contains planetary data - atmosphere, size, flora and fauna (if any)
 * Contains Room - planet surface, with logic for landing party movement.
 */
public class Planet extends BasePlanet implements IDungeon {
    private static final Logger logger = LoggerFactory.getLogger(Planet.class);

    private static final long serialVersionUID = 3L;

    private SurfaceTileMap surface = null;


    private LandingParty landingParty;

    /**
     * Where landing shuttle is located. Launching to orbit and refilling oxygen is available at shuttle
     */
    private LandingShuttle shuttle;

    private PlanetFloraAndFauna floraAndFauna = null;

    private transient Future surfaceGenerationFuture = null;

    private transient Animation shuttle_landing;

    private DungeonController controller;

    private World world;

    // total amount of all tiles explored on this planet
    private int exploredTiles = 0;


    public Planet(World world, StarSystem owner, Planet other) {
        super(other.getX(), other.getY(), other.size, owner, other.atmosphere, other.category);
        if (other.surface == null) {
            other.createSurface();
        }
        this.surface = new SurfaceTileMap(other.surface);
        this.controller = new DungeonController(world, owner, this);
        createOreDeposits(size, CommonRandom.getRandom());
        this.world = world;
    }

    public Planet(World world, StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
        super(x, y, size, owner, atmosphere, cat);
        this.world = world;
    }

    private void createSurface() {
        final Random r = CommonRandom.getRandom();

        long start = System.currentTimeMillis();
        int width;
        int height;
        switch (size) {
            case 1:
                width = 250;
                height = 250;
                break;
            case 2:
                width = 150;
                height = 150;
                break;
            case 3:
                width = 100;
                height = 100;
                break;
            case 4:
                width = 70;
                height = 70;
                break;
            default:
                throw new IllegalArgumentException("Unsupported planet size value");
        }

        surface = new SurfaceTileMap(width, height, LandscapeGenerator.generateLandscapePerlin(category, width, height));
        logger.info("Generated landscape in " + (System.currentTimeMillis() - start));

        createOreDeposits(size, r);

        if (floraAndFauna != null) {
            PlanetaryLifeGenerator.addAnimals(this);
            PlanetaryLifeGenerator.addPlants(this);
        }
        controller = new DungeonController(world, owner, this);
    }

    private void createOreDeposits(int size, Random r) {
        final int resourceDeposits = r.nextInt(40 / size);
        for (int i = 0; i < resourceDeposits; ++i) {
            OreDeposit d = new OreDeposit(this, r.nextInt(10), r.nextInt(10), CollectionUtils.selectRandomElement(OreDeposit.OreType.values()), r.nextInt(3) + 1);
            int oreX;
            int oreY;
            do {
                oreX = r.nextInt(getWidth());
                oreY = r.nextInt(getHeight());
            } while (!surface.isTilePassable(oreX, oreY));
            d.setPos(oreX, oreY);
            surface.getObjects().add(d);
        }
    }


    public void setNearestFreePoint(Positionable p, int x, int y) {
        if (surface == null) {
            createSurface();
        }
        while (!surface.isTilePassable(EngineUtils.wrap(x, surface.getWidthInTiles()), y)) {
            x++;
        }

        p.setPos(x, y);
    }


    public List<PlanetObject> getPlanetObjects() {
        return getSurface().getObjects();
    }

    @Override
    public void enter(final World world) {
        world.getCamera().resetViewPort();
        if (shuttle_landing == null) {
            shuttle_landing = ResourceManager.getInstance().getAnimation("shuttle_landing");
            shuttle_landing.setAutoUpdate(false);
            shuttle_landing.setLooping(true);
            shuttle_landing.start();
        }
        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("landing");
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);

        world.onPlayerLandedPlanet(this);
        surfaceGenerationFuture = GlobalThreadPool.getExecutor().submit(new Runnable() {
            @Override
            public void run() {

                try {
                    if (surface == null) {
                        createSurface();
                    }

                } catch (Exception e) {
                    logger.error("Failed to generate surface for planet", e);
                }
            }
        });
    }

    @Override
    public void returnTo(World world) {
        GUI.getInstance().getNifty().gotoScreen("surface_gui");
        world.getCamera().setTarget(landingParty);
        world.getCamera().resetViewPort();

    }


    public BasePositionable getShuttle() {
        return shuttle;
    }

    public void leavePlanet(World world) {
        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.launch_shuttle"));
        world.setCurrentRoom(owner);
        owner.enter(world);
        world.getPlayer().getShip().setPos(x, y);
        world.onPlayerLeftPlanet(this);
        landingParty.onReturnToShip(world);
    }


    @Override
    public void update(GameContainer container, World world) {
        if (surfaceGenerationFuture != null) {
            if (surfaceGenerationFuture.isDone()) {
                final Element topMostPopup = GUI.getInstance().getNifty().getTopMostPopup();
                if (topMostPopup != null) {
                    GUI.getInstance().getNifty().closePopup(topMostPopup.getId());
                }
                GUI.getInstance().getNifty().gotoScreen("surface_gui");
                landingParty = world.getPlayer().getLandingParty();
                if (!landingParty.canBeLaunched(world) || world.getGlobalVariables().containsKey("tutorial.landing")) {
                    // either this is first landing, or landing party can not be launched in current state and must be reconfigured. Show landing party screen
                    if (GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("landing_party_equip_screen")) {
                        return;
                    }
                    GUI.getInstance().pushCurrentScreen();
                    GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
                    if (world.getGlobalVariables().containsKey("tutorial.landing")) {
                        // this is first landing on a planet, show tutorial dialog
                        Dialog d = Dialog.loadFromFile("dialogs/tutorials/planet_landing_tutorial.json");
                        world.addOverlayWindow(d);
                        world.getGlobalVariables().remove("tutorial.landing");
                    }
                    return;
                }

                int x = landingParty.getX();
                int y = landingParty.getY();

                while (!surface.isTilePassable(landingParty, EngineUtils.wrap(x, getWidth()), EngineUtils.wrap(y, getHeight()))) {
                    x = EngineUtils.wrap(x + 1, getWidth());
                    y = EngineUtils.wrap(y + CommonRandom.getRandom().nextInt(2) - 1, getHeight());
                }
                landingParty.setPos(x, y);
                landingParty.onLaunch(world);
                landingParty.refillOxygen();


                world.getCamera().setTarget(landingParty);
                shuttle = null;
                for (PlanetObject po : surface.getObjects()) {
                    if (po instanceof LandingShuttle) {
                        shuttle = (LandingShuttle) po;
                    }
                }
                if (shuttle == null) {
                    shuttle = new LandingShuttle(this, landingParty.getX(), landingParty.getY());
                    surface.getObjects().add(shuttle);
                } else {
                    shuttle.setPos(landingParty.getX(), landingParty.getY());
                }

                int openedTiles = surface.updateVisibility(landingParty.getX(), landingParty.getY(), 5);
                exploredTiles += openedTiles;
                landingParty.addCollectedGeodata(openedTiles);
                surfaceGenerationFuture = null;
            }
            return;
        }

        controller.update(container, world);

        checkAndConsumeOxygen();

        if (landingParty.getDistanceFromTargetPointWrapped(shuttle, getWidth(), getHeight()) == 0) {
            if (world.isUpdatedThisFrame()) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.refill_oxygen"));
                world.getPlayer().getLandingParty().refillOxygen();
            }
            if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                leavePlanet(world);
            }
        }

        if (world.getPlayer().getLandingParty().getOxygen() < 0) {
            controller.onLandingPartyDestroyed(world);
        }

        landingParty.update(container, world);
    }


    public void drawLandscape(GameContainer container, Graphics graphics, Camera camera) {
        if (surface == null) {
            createSurface();
        }
        surface.draw(container, graphics, camera);
    }

    public void drawObjects(Graphics graphics, Camera camera) {
        // this part (monsters, shuttle, landing party) is drawn only when landing party is on surface
        graphics.drawImage(ResourceManager.getInstance().getImage("shuttle"), camera.getXCoordWrapped(shuttle.getX(), getWidth()), camera.getYCoordWrapped(shuttle.getY(), getHeight()));

    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        if (surfaceGenerationFuture != null) {
            final Element topMostPopup = GUI.getInstance().getNifty().getTopMostPopup();
            if (topMostPopup == null) {
                return;
            }
            final Element shuttle_image = topMostPopup.findElementByName("shuttle_image");

            if (shuttle_image == null) {
                // it is possible that some other popup will be on top, e.g. listbox item list
                return;
            }
            final long delta = container.getTime() - AuroraGame.getLastFrameTime();
            shuttle_landing.update(delta);
            EngineUtils.setImageForGUIElement(shuttle_image, shuttle_landing.getCurrentFrame());
            return;
        }
        drawLandscape(container, graphics, camera);
        drawObjects(graphics, camera);
        controller.draw(container, graphics, camera);
    }

    public int getWidth() {
        return getSurface().getWidthInTiles();
    }

    public int getHeight() {
        return getSurface().getHeightInTiles();
    }

    public PlanetFloraAndFauna getFloraAndFauna() {
        return floraAndFauna;
    }

    public void setFloraAndFauna(PlanetFloraAndFauna floraAndFauna) {
        this.floraAndFauna = floraAndFauna;
    }

    @Override
    public boolean hasLife() {
        return floraAndFauna != null;
    }

    public SurfaceTileMap getSurface() {
        if (surface == null) {
            createSurface();
        }
        return surface;
    }

    public DungeonController getController() {
        return controller;
    }

    @Override
    public ITileMap getMap() {
        return surface;
    }

    @Override
    public boolean isCommanderInParty() {
        return false;
    }

    @Override
    public boolean hasCustomMusic() {
        return false;
    }

    public void checkAndConsumeOxygen() {
        if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE && world.isUpdatedThisFrame()) {
            world.getPlayer().getLandingParty().consumeOxygen();
        }
    }

    @Override
    public StringBuilder getScanText() {
        StringBuilder sb = new StringBuilder(Localization.getText("gui", "scan.planetary_data")).append('\n');
        sb.append(Localization.getText("gui", "scan.atmosphere")).append(' ').append(getAtmosphere()).append('\n');

        String sizeText;
        switch (getSize()) {
            case 4:
                sizeText = Localization.getText("gui", "scan.size.small");
                break;
            case 3:
                sizeText = Localization.getText("gui", "scan.size.medium");
                break;
            case 2:
                sizeText = Localization.getText("gui", "scan.size.large");
                break;
            case 1:
                sizeText = Localization.getText("gui", "scan.size.huge");
                break;
            default:
                throw new IllegalArgumentException();
        }
        sb.append(Localization.getText("gui", "scan.size")).append(' ').append(sizeText).append('\n');
        sb.append(Localization.getText("gui", "scan.bio_activity")).append(' ').append(hasLife() ? Localization.getText("gui", "scan.detected") : Localization.getText("gui", "scan.not_detected")).append('\n');
        sb.append(Localization.getText("gui", "scan.surface_type")).append(' ').append(getCategory()).append('\n');

        return sb;
    }

    public int getExploredTiles() {
        return exploredTiles;
    }
}

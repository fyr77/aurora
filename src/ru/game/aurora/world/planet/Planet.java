/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 18:46
 */
package ru.game.aurora.world.planet;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.*;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.DungeonController;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
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
public class Planet extends BasePlanet {

    private static final long serialVersionUID = 3L;


    private SurfaceTileMap surface = null;


    private LandingParty landingParty;

    /**
     * Where landing shuttle is located. Launching to orbit and refilling oxygen is available at shuttle
     */
    private LandingShuttle shuttle;

    private PlanetFloraAndFauna floraAndFauna = null;


    private transient Image sprite;

    private transient Future surfaceGenerationFuture = null;

    private transient Animation shuttle_landing;

    private DungeonController controller;

    private World world;


    public Planet(World world, StarSystem owner, Planet other) {
        super(other.size, other.globalY, owner, other.atmosphere, other.globalX, other.category);
        if (other.surface == null) {
            other.createSurface();
        }
        this.surface = new SurfaceTileMap(other.surface);
        createOreDeposits(size, CommonRandom.getRandom());
        this.world = world;
    }

    public Planet(World world, StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
        super(size, y, owner, atmosphere, x, cat);
        this.world = world;
    }

    private void createSurface() {
        final Random r = CommonRandom.getRandom();

        long start = System.currentTimeMillis();
        int width;
        int height;
        switch (size) {
            case 1:
                width = 500;
                height = 500;
                break;
            case 2:
                width = 300;
                height = 300;
                break;
            case 3:
                width = 200;
                height = 200;
                break;
            case 4:
                width = 100;
                height = 100;
                break;
            default:
                throw new IllegalArgumentException("Unsupported planet size value");
        }

        surface = new SurfaceTileMap(width, height, LandscapeGenerator.generateLandscapePerlin(category, width, height));
        System.out.println("Generated landscape in " + (System.currentTimeMillis() - start));

        createOreDeposits(size, r);

        if (floraAndFauna != null) {
            PlanetaryLifeGenerator.addAnimals(this);
            PlanetaryLifeGenerator.addPlants(this);
        }
        controller = new DungeonController(world, owner, surface, true);
    }

    private void createOreDeposits(int size, Random r) {
        final int resourceDeposits = r.nextInt(40 / size);
        for (int i = 0; i < resourceDeposits; ++i) {
            OreDeposit d = new OreDeposit(this, r.nextInt(10), r.nextInt(10), CollectionUtils.selectRandomElement(OreDeposit.OreType.values()), r.nextInt(3) + 1);
            int oreX;
            int oreY;
            do {
                oreX = r.nextInt(10);
                oreY = r.nextInt(10);
            } while (!surface.isTilePassable(oreX, oreY));
            d.setPos(oreX, oreY);
            surface.getObjects().add(d);
        }
    }


    public void setNearestFreePoint(Positionable p, int x, int y) {
        if (surface == null) {
            createSurface();
        }
        while (!surface.isTilePassable(EngineUtils.wrap(x, surface.getWidth()), y)) {
            x++;
        }

        p.setPos(x, y);
    }


    public List<PlanetObject> getPlanetObjects() {
        return getSurface().getObjects();
    }

    @Override
    public void enter(final World world) {
        if (shuttle_landing == null) {
            shuttle_landing = ResourceManager.getInstance().getAnimation("shuttle_landing");
            shuttle_landing.setAutoUpdate(false);
            shuttle_landing.setLooping(true);
            shuttle_landing.start();
        }
        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("landing");
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);


        surfaceGenerationFuture = GlobalThreadPool.getExecutor().submit(new Runnable() {
            @Override
            public void run() {

                try {
                    if (surface == null) {
                        createSurface();
                    }
                    nifty.closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
                    nifty.gotoScreen("surface_gui");


                } catch (Exception e) {
                    System.err.println("Failed to enter planet");
                    e.printStackTrace();
                }
            }
        });
    }


    public BasePositionable getShuttle() {
        return shuttle;
    }

    public void leavePlanet(World world) {
        GameLogger.getInstance().logMessage("Launching shuttle to orbit...");
        world.setCurrentRoom(owner);
        owner.enter(world);
        world.getPlayer().getShip().setPos(globalX, globalY);
        landingParty.onReturnToShip(world);
    }


    @Override
    public void update(GameContainer container, World world) {
        if (surfaceGenerationFuture != null) {
            if (surfaceGenerationFuture.isDone()) {
                landingParty = world.getPlayer().getLandingParty();
                if (landingParty == null) {
                    landingParty = new LandingParty();
                }
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

                landingParty.setPos(10, 10); //todo: set position on land
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
                landingParty.addCollectedGeodata(openedTiles);
                surfaceGenerationFuture = null;
            }
            return;
        }

        controller.update(container, world);

        if (landingParty.getDistance(shuttle) == 0) {
            if (world.isUpdatedThisFrame()) {
                GameLogger.getInstance().logMessage("Refilling oxygen");
                world.getPlayer().getLandingParty().refillOxygen();
            }
            if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                leavePlanet(world);
            }
        }

        if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE && world.isUpdatedThisFrame()) {
            world.getPlayer().getLandingParty().consumeOxygen();
        }

        if (world.getPlayer().getLandingParty().getOxygen() < 0 || world.getPlayer().getLandingParty().getTotalMembers() == 0) {
            controller.onLandingPartyDestroyed(world);
        }

    }


    public void drawLandscape(GameContainer container, Graphics graphics, Camera camera) {
        if (surface == null) {
            createSurface();
        }
        surface.draw(container, graphics, camera);
    }

    public void drawObjects(GameContainer container, Graphics graphics, Camera camera) {
        // this part (monsters, shuttle, landing party) is drawn only when landing party is on surface
        graphics.drawImage(ResourceManager.getInstance().getImage("shuttle"), camera.getXCoordWrapped(shuttle.getX(), getWidth()), camera.getYCoordWrapped((int) shuttle.getY(), getHeight()));

    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        if (surfaceGenerationFuture != null) {
            final Element topMostPopup = GUI.getInstance().getNifty().getTopMostPopup();
            if (topMostPopup == null) {
                return;
            }
            final Element shuttle_image = topMostPopup.findElementByName("shuttle_image");


            final long delta = container.getTime() - AuroraGame.getLastFrameTime();
            shuttle_landing.update(delta);
            EngineUtils.setImageForGUIElement(shuttle_image, shuttle_landing.getCurrentFrame());
            return;
        }
        drawLandscape(container, graphics, camera);
        drawObjects(container, graphics, camera);
        controller.draw(container, graphics, camera);
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }
        if (sprite == null) {
            sprite = PlanetSpriteGenerator.getInstance().createPlanetSprite(camera, category, size, atmosphere != PlanetAtmosphere.NO_ATMOSPHERE);
        }
        graphics.drawImage(sprite, camera.getXCoord(globalX) + (camera.getTileWidth() - sprite.getWidth()) / 2, camera.getYCoord(globalY) + (camera.getTileHeight() - sprite.getHeight()) / 2);
    }

    public int getWidth() {
        return getSurface().getWidth();
    }

    public int getHeight() {
        return getSurface().getHeight();
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
}

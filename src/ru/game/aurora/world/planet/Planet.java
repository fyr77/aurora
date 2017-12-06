/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 18:46
 */
package ru.game.aurora.world.planet;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.steam.AchievementManager;
import ru.game.aurora.steam.StatNames;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.nature.PlanetFloraAndFauna;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;
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
    private final World world;
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
    // total amount of all tiles explored on this planet
    private int exploredTiles = 0;

    // flags that show if planet has some natural dangers like acid rains, see Environment class
    private int environment = 0;


    public Planet(World world, StarSystem owner, Planet other) {
        super(other.getX(), other.getY(), other.size, owner, other.atmosphere, other.category);
        if (other.surface == null) {
            other.createSurface();
        }
        this.surface = new SurfaceTileMap(other.surface);
        this.controller = new DungeonController(world, owner, this);
        addOreDeposits(1 + CommonRandom.getRandom().nextInt(40 / size));
        this.world = world;
    }

    public Planet(World world, StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
        super(x, y, size, owner, atmosphere, cat);
        this.world = world;
    }

    private void createSurface() {
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

        addOreDeposits(CommonRandom.getRandom().nextInt(40 / size));

        if (floraAndFauna != null) {
            PlanetaryLifeGenerator.addAnimals(this);
            PlanetaryLifeGenerator.addPlants(this);
        }
        controller = new DungeonController(world, owner, this);
    }
    public void addQuestObject (GameObject planetObject) {
        getPlanetObjects().add(planetObject);
        environment = environment & (~Environment.METEORS);
    }

    // re-generate planet surface untill it has a width x height passable rectangle
    public void ensureFreeSpace(int width, int height)
    {
        if (surface != null) {
            throw new IllegalStateException("ensureFreeSpace can not be called after surface is generated");
        }
        do {
            createSurface();
        } while (findPassableRegion(width, height) == null);
    }

    public void addOreDeposits(int resourceDeposits) {
        Random r = CommonRandom.getRandom();
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

    public BasePositionable findPassableRegion(int regionWidth, int regionHeight) {
        getSurface();
        short[][] customMap = new short[surface.getHeightInTiles()][surface.getWidthInTiles()];

        for (int y = 1; y < surface.getHeightInTiles(); ++y) {
            for (int x = 1; x < surface.getWidthInTiles(); ++x) {
                customMap[y][x] = (short) (customMap[y - 1][x] + customMap[y][x - 1] - customMap[y - 1][x - 1] + (surface.isTilePassable(x - 1, y - 1) ? 1 : 0));
            }
        }

        for (int y = regionHeight; y < surface.getHeightInTiles(); y += 2) {
            for (int x = regionWidth; x < surface.getWidthInTiles(); x += 2) {
                if (customMap[y][x] - customMap[y - regionHeight][x] - customMap[y][x - regionWidth] + customMap[y - regionHeight][x - regionWidth] == regionHeight * regionWidth) {
                    return new BasePositionable(x - regionWidth, y - regionHeight);
                }
            }
        }

        logger.warn("Given planet does not have a passable terrain of " + regionWidth + "x" + regionHeight + " size ");
        return null;
    }

    public void setNearestFreePoint(Positionable p, int x, int y) {
        if (surface == null) {
            createSurface();
        }
        AuroraTiledMap.setNearestFreePoint(surface, p, x, y);
    }


    public List<GameObject> getPlanetObjects() {
        return getSurface().getObjects();
    }

    @Override
    public void enter(final World world) {
        landingParty = world.getPlayer().getLandingParty();
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
        landingParty.onReturnToShip(world);
        world.setCurrentRoom(owner);
        owner.returnTo(world);
        world.getPlayer().getShip().setPos(x, y);
        world.onPlayerLeftPlanet(this);
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
                world.getCamera().resetViewPort();
                shuttle = null;
                for (GameObject po : surface.getObjects()) {
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

                if (exploredTiles == 0) {
                    // this is first time we landed on a planet
                    AchievementManager.getInstance().updateStat(StatNames.worldsVisited, 1);
                }
                int viewRangeBonus = ((Number) world.getGlobalVariable("landingPartyViewRangeBonus", 0)).intValue();
                int openedTiles = surface.updateVisibility(landingParty.getX(), landingParty.getY(), 5 + viewRangeBonus);
                exploredTiles += openedTiles;
                landingParty.addCollectedGeodata(openedTiles);
                surfaceGenerationFuture = null;

                world.onPlayerLandedPlanet(this);
            }
            return;
        }

        controller.update(container, world);
        exploredTiles += controller.getTilesExploredThisTurn();
        landingParty.addCollectedGeodata(controller.getTilesExploredThisTurn());

        checkAndConsumeOxygen();

        if (landingParty.getDistanceFromTargetPointWrapped(shuttle, getWidth(), getHeight()) == 0) {
            if (world.isUpdatedThisFrame()) {
                if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.refill_oxygen"));
                    world.getPlayer().getLandingParty().refillOxygen();
                }

                // dump dumpable cargo
                for (Iterator<Multiset.Entry<InventoryItem>> iter = landingParty.getInventory().entrySet().iterator(); iter.hasNext(); ) {
                    Multiset.Entry<InventoryItem> o = iter.next();
                    if (o.getElement().isDumpable()) {
                        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.dump_item") + " " + o.getCount() + " " + o.getElement().getName());
                        o.getElement().onReceived(world, o.getCount());
                        iter.remove();
                    }
                }
            }
            if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
            || container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY))) {
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
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {

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
        return getSurface();
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
    public String getScanText() {
        StringBuilder sb = new StringBuilder(Localization.getText("gui", "scan.planetary_data")).append('\n');
        sb.append(Localization.getText("gui", "scan.atmosphere")).append(' ');
        sb.append(Localization.getText("gui", "atmosphere." + getAtmosphere().descriptionKey())).append('\n');

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
        sb.append(Localization.getText("gui", "scan.surface_type")).append(' ').append(getCategory().getLocalizationText()).append('\n');
        Environment.appendScanText(sb, environment);

        return sb.toString();
    }

    public int getExploredTiles() {
        return exploredTiles;
    }

    @Override
    public String getInteractMessage() {
        return Localization.getText("gui", "space.land");
    }

    public int getEnvironment() {
        return environment;
    }

    public void addEnvironmentFlag(byte val) {
        environment |= val;
    }
}

/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 18:46
 */
package ru.game.aurora.world.planet;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.*;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.PlanetFloraAndFauna;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
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
public class Planet extends BasePlanet {

    /**
     * Mode for moving. Arrows control landing party movement.
     */
    private static final int MODE_MOVE = 0;

    /**
     * Mode for shooting. Arrows control target selection.
     */
    private static final int MODE_SHOOT = 1;

    private static final long serialVersionUID = 2L;

    /**
     * Current mode
     */
    private int mode = MODE_MOVE;

    private SurfaceTileMap surface = null;


    private LandingParty landingParty;

    /**
     * Where landing shuttle is located. Launching to orbit and refilling oxygen is available at shuttle
     */
    private BasePositionable shuttlePosition;

    private PlanetFloraAndFauna floraAndFauna = null;

    /**
     * Animals that are located on planet surface.
     */
    private List<PlanetObject> planetObjects = new ArrayList<>();

    private transient Image sprite;

    /**
     * When in fire mode, this is currently selected target
     */
    private transient PlanetObject target = null;

    private transient Effect currentEffect = null;

    private transient Future surfaceGenerationFuture = null;

    private transient Animation shuttle_landing;


    public Planet(StarSystem owner, Planet other) {
        super(other.size, other.globalY, owner, other.atmosphere, other.globalX, other.category);
        if (other.surface == null) {
            other.createSurface();
        }
        this.surface = new SurfaceTileMap(other.surface);
        createOreDeposits(size, CommonRandom.getRandom());
    }

    public Planet(StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
        super(size, y, owner, atmosphere, x, cat);

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
            planetObjects.add(d);
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
        return planetObjects;
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

    @Override
    public Screen getGUI() {
        return null;
    }

    /**
     * This update is used in MOVE mode. Moving landing party around.
     */
    private void updateMove(GameContainer container, World world) {
        int x = world.getPlayer().getLandingParty().getX();
        int y = world.getPlayer().getLandingParty().getY();

        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            y++;
            world.setUpdatedThisFrame(true);
        }

        if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            x++;
            world.setUpdatedThisFrame(true);
        }

        x = EngineUtils.wrap(x, getWidth());
        y = EngineUtils.wrap(y, getHeight());

        if (!surface.isTilePassable(landingParty, x, y)) {
            world.setUpdatedThisFrame(false);
            x = world.getPlayer().getLandingParty().getX();
            y = world.getPlayer().getLandingParty().getY();
        }

        final boolean enterPressed = container.getInput().isKeyPressed(Input.KEY_ENTER);
        if (enterPressed) {
            interactWithObject(world);

        }

        int tilesExplored = surface.updateVisibility(x, y, 1);
        landingParty.addCollectedGeodata(tilesExplored);

        if ((x == shuttlePosition.getX()) && (y == shuttlePosition.getY())) {
            if (world.isUpdatedThisFrame()) {
                GameLogger.getInstance().logMessage("Refilling oxygen");
                world.getPlayer().getLandingParty().refillOxygen();
            }
            if (enterPressed) {
                leavePlanet(world);
            }
        }
        world.getPlayer().getLandingParty().setPos(x, y);
    }

    public BasePositionable getShuttlePosition() {
        return shuttlePosition;
    }

    public void leavePlanet(World world) {
        GameLogger.getInstance().logMessage("Launching shuttle to orbit...");
        world.setCurrentRoom(owner);
        owner.enter(world);
        world.getPlayer().getShip().setPos(globalX, globalY);
        landingParty.onReturnToShip(world);
    }

    public void interactWithObject(World world) {
        int x = world.getPlayer().getLandingParty().getX();
        int y = world.getPlayer().getLandingParty().getY();
        // check if can pick up smth
        for (Iterator<PlanetObject> iter = planetObjects.iterator(); iter.hasNext(); ) {
            PlanetObject p = iter.next();

            if (!p.canBePickedUp()) {
                continue;
            }

            if (p.getX() != x || p.getY() != y) {
                continue;
            }
            p.onPickedUp(world);
            world.setUpdatedThisFrame(true);
            // some items (like ore deposits) can be picked up more than once, do not remove them in this case
            if (!p.isAlive()) {
                iter.remove();
            }
        }
    }

    private int getDist(int first, int second, int total) {
        int max = Math.max(first, second);
        int min = Math.min(first, second);

        return Math.min(max - min, total + min - max);

    }

    private int getRange(LandingParty party, Positionable target) {
        int xDist = getDist(party.getX(), target.getX(), getWidth());
        int yDist = getDist(party.getY(), target.getY(), getHeight());
        return xDist + yDist;
    }

    /**
     * This update method is used in FIRE mode. Selecting targets and shooting.
     */
    private void updateShoot(GameContainer container, World world) {
        if (planetObjects.isEmpty()) {
            return;
        }
        int targetIdx = 0;
        List<PlanetObject> availableTargets = new ArrayList<>();

        if (target != null && getRange(landingParty, target) > landingParty.getWeapon().getRange()) {
            // target moved out of range
            target = null;
        }

        for (PlanetObject planetObject : planetObjects) {
            if (!planetObject.canBeShotAt()) {
                continue;
            }
            if (surface.isTileVisible(planetObject.getX(), planetObject.getY())) {
                // do not target animals on unexplored tiles
                continue;
            }
            if (landingParty.getWeapon().getRange() >= getRange(landingParty, planetObject)) {
                availableTargets.add(planetObject);
                if (target == null) {
                    target = planetObject;
                }

                if (target == planetObject) {
                    targetIdx = availableTargets.size() - 1;
                }
            }

        }

        if (availableTargets.isEmpty()) {
            // no target available in weapon range
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_UP) || container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            targetIdx++;
            if (targetIdx >= availableTargets.size()) {
                targetIdx = 0;
            }
        } else if (container.getInput().isKeyPressed(Input.KEY_DOWN) || container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            targetIdx--;
            if (targetIdx < 0) {
                targetIdx = availableTargets.size() - 1;
            }
        }

        target = availableTargets.get(targetIdx);

        if (container.getInput().isKeyPressed(Input.KEY_F) || container.getInput().isKeyPressed(Input.KEY_ENTER)) {
            // firing
            final int damage = landingParty.calcDamage();

            currentEffect = new BlasterShotEffect(landingParty, world.getCamera().getXCoordWrapped(target.getX(), getWidth()), world.getCamera().getYCoordWrapped(target.getY(), getHeight()), world.getCamera(), 800, "blaster_shot");

            target.onShotAt(damage);
            GameLogger.getInstance().logMessage("Bang! Dealt " + damage + " damage to " + target.getName());
            if (!target.isAlive()) {
                GameLogger.getInstance().logMessage(target.getName() + " killed");
                planetObjects.remove(target);
                target = null;
            }
            world.setUpdatedThisFrame(true);
        }

    }

    public void changeMode() {
        mode = (mode == MODE_MOVE) ? MODE_SHOOT : MODE_MOVE;
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
                shuttlePosition = new BasePositionable(landingParty.getX(), landingParty.getY());
                int openedTiles = surface.updateVisibility(landingParty.getX(), landingParty.getY(), 5);
                landingParty.addCollectedGeodata(openedTiles);
                surfaceGenerationFuture = null;
            }
            return;
        }
        if (currentEffect != null) {
            currentEffect.update(container, world);
            if (currentEffect.isOver()) {
                currentEffect = null;
            }
            return;
        }
        switch (mode) {
            case MODE_MOVE:
                if (container.getInput().isKeyPressed(Input.KEY_F)) {
                    mode = MODE_SHOOT;
                    return;
                }
                updateMove(container, world);
                break;
            case MODE_SHOOT:
                if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                    mode = MODE_MOVE;
                    return;
                }
                updateShoot(container, world);
                break;
            default:
                throw new IllegalStateException("Unknown planet update type " + mode);

        }

        if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE && world.isUpdatedThisFrame()) {
            world.getPlayer().getLandingParty().consumeOxygen();
        }

        if (world.getPlayer().getLandingParty().getOxygen() < 0 || world.getPlayer().getLandingParty().getTotalMembers() == 0) {
            GameLogger.getInstance().logMessage("Lost connection with landing party");
            world.setCurrentRoom(owner);
            owner.enter(world);
            world.getPlayer().getShip().setPos(globalX, globalY);

            final Nifty nifty = GUI.getInstance().getNifty();
            Element popup = nifty.createPopup("landing_party_lost");
            nifty.setIgnoreKeyboardEvents(false);
            nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        }

        for (PlanetObject a : planetObjects) {
            a.update(container, world);
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
        if (landingParty != null) {
            landingParty.draw(container, graphics, camera);

            graphics.drawImage(ResourceManager.getInstance().getImage("shuttle"), camera.getXCoordWrapped(shuttlePosition.getX(), getWidth()), camera.getYCoordWrapped((int) shuttlePosition.getY(), getHeight()));

            if (landingParty.getX() == shuttlePosition.getX() && landingParty.getY() == shuttlePosition.getY()) {
            }

            graphics.setColor(Color.red);
            for (PlanetObject a : planetObjects) {
                // draw only if tile under this animal is visible
                if (surface.isTileVisible(a.getX(), a.getY())) {
                    a.draw(container, graphics, camera);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && a.canBeShotAt() && getRange(landingParty, a) < landingParty.getWeapon().getRange()) {
                        graphics.drawRect(camera.getXCoordWrapped(a.getX(), getWidth()), camera.getYCoordWrapped(a.getY(), getHeight()), camera.getTileWidth(), camera.getTileHeight());
                    }
                }

                if (a.getX() == landingParty.getX() && a.getY() == landingParty.getY()) {
                    a.printStatusInfo();
                }

            }

            if (mode == MODE_SHOOT && target != null) {
                // draw target mark
                graphics.drawImage(ResourceManager.getInstance().getImage("target"), camera.getXCoordWrapped(target.getX(), getWidth()), camera.getYCoordWrapped(target.getY(), getHeight()));
            }
        }
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
        if (currentEffect != null) {
            currentEffect.draw(container, graphics, camera);
        }
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
}

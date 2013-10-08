/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.*;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.player.Player;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetCategory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject {

    public static final Color[] possibleColors = {Color.red, Color.white, Color.yellow, new Color(122, 155, 243)};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    private static final long serialVersionUID = -1L;

    /**
     * Mode for moving. Arrows control ship movement
     */
    private static final int MODE_MOVE = 0;

    /**
     * Mode for shooting. Arrows control target selection
     */
    private static final int MODE_SHOOT = 1;

    private int selectedWeapon = 0;

    /**
     * Quest star system, like solar system and others.
     * Blocks spawning of random encounters in them.
     */
    private boolean isQuestLocation = false;

    private SpaceObject target;

    private boolean visited = false;

    /**
     * Dialog that will be shown when player enters this system for first time.
     */
    private Dialog firstEnterDialog;

    /**
     * Special background sprite that will be drawn between parallax background and planets pane
     */
    private String backgroundSprite;

    /**
     * Current mode
     */
    private int mode = MODE_MOVE;

    private Star star;

    private BasePlanet[] planets;

    private int globalMapX;

    private int globalMapY;

    private transient ParallaxBackground background;

    private transient List<Effect> effects = new LinkedList<Effect>();

    /**
     * Relation between tile size and max planet size
     * 3 means max planet will have radius of 3 tiles
     */
    public final static int PLANET_SCALE_FACTOR = 3;

    public final static int STAR_SCALE_FACTOR = 4;

    // size of star system. moving out of radius from the star initiates return to global map
    private int radius;

    private List<SpaceObject> ships = new ArrayList<SpaceObject>();

    /**
     * Variables available for quest logic
     */
    private Map<String, Serializable> variables = new HashMap<String, Serializable>();

    /**
     * How many unexplored data for Astronomy research this star system contains
     */
    private int astronomyData;

    private String name;

    /**
     * If not null, planet surface map should be rendered to an image and assigned to this element
     */
    private transient Element surfaceRenderTarget = null;

    public StarSystem(String name, Star star, int globalMapX, int globalMapY) {
        this.name = name;
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
    }

    public void setPlanets(BasePlanet[] planets) {
        this.planets = planets;
    }

    public Map<String, Serializable> getVariables() {
        return variables;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isInside(Positionable p) {
        return (p.getX() >= -radius && p.getX() <= radius && p.getY() >= -radius && p.getY() <= radius);
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics g, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(tileX, tileY)) {
            return;
        }
        if (camera.getTileWidth() != 64) {
            //hack: this is galaxy map screen, where all stars are drawn within single screen
            // draw only dots, not images themselves, as they won't fit on screen and will overlap ugly
            g.setColor(star.color);
            EngineUtils.drawCircleCentered(g, camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, (int) (camera.getTileWidth() / star.size), star.color, true);
        } else {
            g.drawImage(star.getImage(), camera.getXCoord(tileX) - star.getImage().getWidth() / 2, camera.getYCoord(tileY) - star.getImage().getHeight() / 2);
        }
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(GameContainer container, Player player) {
    }

    private void updateMove(GameContainer container, World world) {
        super.update(container, world);

        int y = world.getPlayer().getShip().getY();
        int x = world.getPlayer().getShip().getX();

        if (container.getInput().isKeyPressed(Input.KEY_L)) {
            /*LandingPartyEquipScreen screen = new LandingPartyEquipScreen(false);
            screen.enter(world);
            world.setCurrentRoom(screen);*/
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
            return;
        }

        if ((container.getInput().isKeyDown(Input.KEY_UP) && y <= -radius)
                || (container.getInput().isKeyDown(Input.KEY_DOWN) && y >= radius)
                || (container.getInput().isKeyDown(Input.KEY_LEFT) && x <= -radius)
                || (container.getInput().isKeyDown(Input.KEY_RIGHT) && x >= radius)) {
            GameLogger.getInstance().logMessage("Leaving star system...");
            world.setCurrentRoom(world.getGalaxyMap());
            world.getGalaxyMap().enter(world);
            player.getShip().setPos(globalMapX, globalMapY);
            // do not keep background
            background = null;
        }

        boolean isAtPlanet = false;
        for (BasePlanet p : planets) {
            if (x == p.getGlobalX() && y == p.getGlobalY()) {
                isAtPlanet = true;
                if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                    landOnCurrentPlanet(world);
                    break;
                } else if (world.isUpdatedThisFrame()) {
                    p.processCollision(container, world.getPlayer());
                    break;
                }
            }
        }

        // if user ship is at planet, show additional gui panel
        final Element scanLandPanel = GUI.getInstance().getNifty().getScreen("star_system_gui").findElementByName("interactPanel");
        if (scanLandPanel != null) {
            boolean landPanelVisible = scanLandPanel.isVisible();
            if (!isAtPlanet && landPanelVisible && getSpaceObjectAtPlayerShipPosition() == null) {
                scanLandPanel.setVisible(false);
            } else if (isAtPlanet && !landPanelVisible) {
                Button leftButton = scanLandPanel.findNiftyControl("left_button", Button.class);
                leftButton.setText(Localization.getText("gui", "space.land"));
                scanLandPanel.setVisible(true);
            }
        }
    }

    public BasePlanet getPlanetAtPlayerShipPosition() {
        int x = player.getShip().getX();
        int y = player.getShip().getY();
        for (BasePlanet p : planets) {
            if (x == p.getGlobalX() && y == p.getGlobalY()) {
                return p;
            }
        }
        return null;
    }

    public SpaceObject getSpaceObjectAtPlayerShipPosition() {
        int x = player.getShip().getX();
        int y = player.getShip().getY();
        for (SpaceObject p : ships) {
            if (x == p.getX() && y == p.getY()) {
                return p;
            }
        }
        return null;
    }

    public void landOnCurrentPlanet(World world) {
        BasePlanet p = getPlanetAtPlayerShipPosition();
        if (p == null) {
            return;
        }
        if (p.getCategory() == PlanetCategory.GAS_GIANT) {
            GameLogger.getInstance().logMessage("We can not land here.");
            return;
        }
        GameLogger.getInstance().logMessage("Descending to surface...");
        world.setCurrentRoom(p);
        p.enter(world);


    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        try {
            ois.defaultReadObject();
            effects = new LinkedList<Effect>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateShoot(World world, boolean next, boolean prev, boolean shoot) {
        if (ships.isEmpty()) {
            // nothing to shoot at
            return;
        }
        int targetIdx = 0;
        List<SpaceObject> availableTargets = new ArrayList<SpaceObject>();

        final Ship playerShip = world.getPlayer().getShip();
        final StarshipWeapon weapon = playerShip.getWeapons().get(selectedWeapon);

        if (target != null && playerShip.getDistance(target) > weapon.getWeaponDesc().range) {
            // target moved out of range
            target = null;
        }

        //TODO: firing sectors
        for (SpaceObject spaceObject : ships) {
            if (weapon.getWeaponDesc().range >= playerShip.getDistance(spaceObject)) {
                availableTargets.add(spaceObject);
                if (target == null) {
                    target = spaceObject;
                }
                if (target == spaceObject) {
                    targetIdx = availableTargets.size() - 1;
                }
            }
        }

        if (availableTargets.isEmpty()) {
            // no target available in weapon range
            return;
        }

        if (next) {
            targetIdx++;
            if (targetIdx >= availableTargets.size()) {
                targetIdx = 0;
            }
        } else if (prev) {
            targetIdx--;
            if (targetIdx < 0) {
                targetIdx = availableTargets.size() - 1;
            }
        }

        target = availableTargets.get(targetIdx);

        if (shoot) {

            if (weapon.getReloadTimeLeft() > 0) {
                GameLogger.getInstance().logMessage("Weapon not yet reloaded");
                return;
            }

            // firing
            final int damage = weapon.getWeaponDesc().damage;
            target.onAttack(world, playerShip, damage);
            GameLogger.getInstance().logMessage("Bang! Dealt " + damage + " damage to " + target.getName());

            effects.add(new BlasterShotEffect(playerShip, target, world.getCamera(), 800, weapon.getWeaponDesc().shotSprite));

            if (!target.isAlive()) {
                GameLogger.getInstance().logMessage(target.getName() + " destroyed");
                ships.remove(target);
                target = null;
            }
            weapon.setReloadTimeLeft(weapon.getWeaponDesc().reloadTurns);
            world.setUpdatedThisFrame(true);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void onWeaponButtonPressed(World world, int index) {
        if (mode == MODE_MOVE) {
            selectedWeapon = index;
            Ship playerShip = world.getPlayer().getShip();
            if (playerShip.getWeapons().size() <= selectedWeapon || playerShip.getWeapons().get(selectedWeapon) == null) {
                GameLogger.getInstance().logMessage("No weapon in slot " + (1 + selectedWeapon));
                return;
            }

            if (playerShip.getWeapons().get(selectedWeapon).getReloadTimeLeft() > 0) {
                GameLogger.getInstance().logMessage("Weapon not yet reloaded");
                return;
            }

            mode = MODE_SHOOT;
            GUI.getInstance().getNifty().getCurrentScreen().findElementByName("shoot_panel").setVisible(true);
        } else {
            mode = MODE_MOVE;
            GUI.getInstance().getNifty().getCurrentScreen().findElementByName("shoot_panel").setVisible(false);
        }
    }


    @Override
    public void update(GameContainer container, World world) {
        if (background == null) {
            createBackground(world);
        }
        if (!effects.isEmpty()) {
            List<Effect> newList = new ArrayList<Effect>(effects);
            for (Effect currentEffect : newList) {
                currentEffect.update(container, world);
            }
            for (Iterator<Effect> iter = effects.iterator(); iter.hasNext(); ) {
                Effect e = iter.next();
                if (e.isOver()) {
                    iter.remove();
                }
            }
            return;
        }

        final Ship playerShip = world.getPlayer().getShip();

        if (mode == MODE_MOVE) {
            updateMove(container, world);
            for (int i = Input.KEY_1; i <= Input.KEY_9; ++i) {
                if (container.getInput().isKeyPressed(i)) {
                    onWeaponButtonPressed(world, i - Input.KEY_1);
                    return;
                }
            }
        } else {
            updateShoot(
                    world
                    , container.getInput().isKeyPressed(Input.KEY_UP) || container.getInput().isKeyPressed(Input.KEY_RIGHT)
                    , container.getInput().isKeyPressed(Input.KEY_DOWN) || container.getInput().isKeyPressed(Input.KEY_LEFT)
                    , container.getInput().isKeyPressed(Input.KEY_F) || container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(selectedWeapon + Input.KEY_1)
            );
            if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                mode = MODE_MOVE;
            }
        }

        boolean shipAtSameCoords = false;
        for (Iterator<SpaceObject> iter = ships.iterator(); iter.hasNext(); ) {
            SpaceObject ship = iter.next();

            if (ship.getX() == playerShip.getX() && ship.getY() == playerShip.getY()) {
                shipAtSameCoords = true;
                if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                    ship.onContact(world);
                }
            }
            if (world.isUpdatedThisFrame()) {
                ship.update(container, world);
                if (!ship.isAlive()) {
                    iter.remove();
                }
            }
        }

        final Element scanLandPanel = GUI.getInstance().getNifty().getScreen("star_system_gui").findElementByName("interactPanel");
        if (scanLandPanel != null) {
            boolean landPanelVisible = scanLandPanel.isVisible();
            if (!shipAtSameCoords && landPanelVisible && getPlanetAtPlayerShipPosition() == null) {
                scanLandPanel.setVisible(false);
            } else if (shipAtSameCoords && !landPanelVisible) {
                Button leftButton = scanLandPanel.findNiftyControl("left_button", Button.class);
                leftButton.setText(Localization.getText("gui", "space.hail"));
                scanLandPanel.setVisible(true);
            }
        }

        playerShip.update(container, world);
    }

    public boolean isVisited() {
        return visited;
    }

    @Override
    public void enter(World world) {
        super.enter(world);
        player = world.getPlayer();
        player.getShip().setPos(-radius + 1, 0);
        // in star system camera is always fixed on center
        //world.getCamera().setTarget(new BasePositionable(world.getCamera().getNumTilesX() / 2, world.getCamera().getNumTilesY() / 2));
        world.getCamera().setTarget(player.getShip());
        if (background == null) {
            createBackground(world);
        }
        world.setCurrentStarSystem(this);
        GUI.getInstance().getNifty().gotoScreen("star_system_gui");
        world.onPlayerEnteredSystem(this);

        if (firstEnterDialog != null && !visited) {
            world.addOverlayWindow(firstEnterDialog);
        }
        visited = true;
    }

    private void createBackground(World world) {
        background = new ParallaxBackground(
                radius * 3 * (int) world.getCamera().getTileWidth()
                , radius * 3 * (int) world.getCamera().getTileHeight()
                , 0//planets.length * world.getCamera().getTileWidth()
                , 0//planets.length * world.getCamera().getTileHeight()
                , planets.length);
    }

    private void renderCurrentPlanetSurface(GameContainer container, Graphics g) {
        g.clear();
        g.setColor(Color.black);
        BasePlanet p = getPlanetAtPlayerShipPosition();
        if (p == null || !(p instanceof Planet)) {
            return;
        }
        Planet planet = (Planet) p;
        final float newTileWidth = container.getWidth() / (float) planet.getWidth();
        final float newTileHeight = container.getHeight() / (float) planet.getHeight();
        Camera myCamera = new Camera(0, 0, planet.getWidth(), planet.getHeight(), newTileWidth, newTileHeight);

        myCamera.setTarget(new BasePositionable(planet.getWidth() / 2, planet.getHeight() / 2));
        planet.getSurface().drawLandscapeMap(g, myCamera);
        g.flush();
        try {
            Image image = new Image(container.getWidth(), container.getHeight());
            g.copyArea(image, 0, 0);
            EngineUtils.setImageForGUIElement(surfaceRenderTarget, image);
        } catch (SlickException e) {
            e.printStackTrace();
        }
        g.clear();
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        if (surfaceRenderTarget != null) {
            renderCurrentPlanetSurface(container, g);
            surfaceRenderTarget = null;
            return;
        }

        if (background != null) {
            background.draw(g, camera);
            if (backgroundSprite != null) {
                float x = background.getXCoordPoint(camera, -100, -1);
                float y = background.getYCoordPoint(camera, -100, -1);
                g.drawImage(ResourceManager.getInstance().getImage(backgroundSprite), x, y);
            }
        }

        player.getShip().draw(container, g, camera);

        g.setColor(star.color);

        final float starX = camera.getXCoord(0) + (camera.getTileWidth() / 2);
        final float starY = camera.getYCoord(0) + camera.getTileHeight() / 2;
        if (camera.isInViewport(0, 0)) {
            // draw 3 circles

            final Image starImage = star.getImage();
            g.drawImage(starImage, starX - starImage.getWidth() / 2, starY - starImage.getHeight() / 2);
        }

        // first draw all orbits
        for (BasePlanet p : planets) {
            float planetX = camera.getXCoord(p.getGlobalX()) + (camera.getTileWidth() / 2);
            float planetY = camera.getYCoord(p.getGlobalY()) + camera.getTileWidth() / 2;
            int radius = (int) Math.sqrt(Math.pow((planetX - starX), 2) + Math.pow((planetY - starY), 2));
            //EngineUtils.drawCircleCentered(g, starX, starY, radius, Color.gray, false);
            EngineUtils.drawDashedCircleCentered(g, starX, starY, radius, new Color(0, 0, 150));
        }

        // and then all planets
        for (BasePlanet p : planets) {
            p.drawOnGlobalMap(container, g, camera, 0, 0);
        }


        final int selectedWeaponRange;
        if (mode == MODE_SHOOT) {
            selectedWeaponRange = player.getShip().getWeapons().get(selectedWeapon).getWeaponDesc().range;
        } else {
            selectedWeaponRange = 0;
        }
        g.setColor(Color.red);
        for (SpaceObject ship : ships) {
            ship.draw(container, g, camera);
            if (ship.getX() == player.getShip().getX() && ship.getY() == player.getShip().getY()) {
                // GameLogger.getInstance().addStatusMessage("Press <enter> to contact");
            }

            if (mode == MODE_SHOOT && player.getShip().getDistance(ship) < selectedWeaponRange) {
                // every targetable ship is surrounded by rectangle
                g.drawRect(camera.getXCoord(ship.getX()), camera.getYCoord(ship.getY()), camera.getTileWidth(), camera.getTileHeight());
            }
        }

        if (mode == MODE_SHOOT) {
            if (target != null) {
                // draw target mark
                g.drawImage(ResourceManager.getInstance().getImage("target"), camera.getXCoord(target.getX()), camera.getYCoord(target.getY()));
            }

            // drawing rect that shows radius of current weapon
            g.drawRect((camera.getNumTilesX() / 2 - selectedWeaponRange + 1) * camera.getTileWidth()
                    , (camera.getNumTilesY() / 2 - selectedWeaponRange + 1) * camera.getTileHeight()
                    , (2 * selectedWeaponRange - 1) * camera.getTileWidth()
                    , (2 * selectedWeaponRange - 1) * camera.getTileHeight());

        }

        g.setColor(Color.red);
        g.drawRect(camera.getXCoord(-radius), camera.getYCoord(-radius), 2 * radius * camera.getTileWidth(), 2 * radius * camera.getTileHeight());

        for (Effect currentEffect : effects) {
            currentEffect.draw(container, g, camera);
        }
    }

    public List<SpaceObject> getShips() {
        return ships;
    }

    public int getAstronomyData() {
        return astronomyData;
    }

    public void setAstronomyData(int astronomyData) {
        this.astronomyData = astronomyData;
    }

    public int getGlobalMapX() {
        return globalMapX;
    }

    public int getGlobalMapY() {
        return globalMapY;
    }

    public int getRadius() {
        return radius;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public void setStar(Star star) {
        this.star = star;
    }

    public void setFirstEnterDialog(Dialog firstEnterDialog) {
        this.firstEnterDialog = firstEnterDialog;
    }

    public BasePlanet[] getPlanets() {
        return planets;
    }

    public boolean isQuestLocation() {
        return isQuestLocation;
    }

    public void setQuestLocation(boolean questLocation) {
        isQuestLocation = questLocation;
    }

    public void setBackgroundSprite(String backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public ParallaxBackground getBackground() {
        return background;
    }

    public void setSurfaceRenderTarget(Element surfaceRenderTarget) {
        this.surfaceRenderTarget = surfaceRenderTarget;
    }
}

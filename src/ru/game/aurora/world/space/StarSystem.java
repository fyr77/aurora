/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Point;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.niffy.FriendlyAttackConfirmationController;
import ru.game.aurora.gui.niffy.InteractionTargetSelectorController;
import ru.game.aurora.player.Player;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.planet.BasePlanet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("InstanceVariableMayNotBeInitializedByReadObject")
public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject, ITileMap {

    public static final Color[] possibleColors = {Color.red, Color.white, Color.yellow, new Color(122, 155, 243)};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    private static final long serialVersionUID = 2L;

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

    private GameObject target;

    private boolean visited = false;

    /**
     * Dialog that will be shown when player enters this system for first time.
     */
    private Dialog firstEnterDialog;

    /**
     * Special background sprite that will be drawn between parallax background and planets pane
     */
    private String backgroundSprite;

    private String backgroundNebula1;
    private Point nebula1Offset;
    private String backgroundNebula2;
    private Point nebula2Offset;

    /**
     * Current mode
     */
    private int mode = MODE_MOVE;

    private Star star;

    private BasePlanet[] planets;

    private AsteroidBelt asteroidBelt = null;

    private int globalMapX;

    private int globalMapY;

    private Reputation reputation;

    // if set to false, player can not leave this star system
    private boolean canBeLeft = true;

    private transient ParallaxBackground background;

    private transient PriorityQueue<Effect> effects = new PriorityQueue<>();

    private transient Effect currentEffect = null;

    /**
     * Relation between tile size and max planet size
     * 3 means max planet will have radius of 3 tiles
     */
    public final static int PLANET_SCALE_FACTOR = 3;

    public final static int STAR_SCALE_FACTOR = 4;

    // size of star system. moving out of radius from the star initiates return to global map
    private int radius;

    private final List<GameObject> ships = new ArrayList<>();

    /**
     * Variables available for quest logic
     */
    private final Map<String, Serializable> variables = new HashMap<>();

    /**
     * How many unexplored data for Astronomy research this star system contains
     */
    private int astronomyData;

    private final String name;

    // information about quests and events in this star system, used in starmap view
    private transient String messageForStarMap;

    public StarSystem(String name, Star star, int globalMapX, int globalMapY) {
        this.name = name;
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
    }

    public void setPlanets(BasePlanet[] planets) {
        this.planets = planets;
    }

    public void setAsteroidBelt(int innerRadius, int width) {
        asteroidBelt = new AsteroidBelt(innerRadius, width);
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

    public int getRadius() {
        return radius;
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics g, Camera camera, int tileX, int tileY) {
       if (!camera.isInViewport(tileX, tileY, 4)) {
            return;
       }
        if (camera.getTileWidth() != 64) {
            //hack: this is galaxy map screen, where all stars are drawn within single screen
            // draw only dots, not images themselves, as they won't fit on screen and will overlap ugly
            //g.setColor(star.color);
            //EngineUtils.drawCircleCentered(g, camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, (int) (camera.getTileWidth() / star.size), star.color, true);
            Image starImage = ParallaxBackground.getImage(star.size, star.color);
            g.drawImage(starImage, camera.getXCoord(tileX) - camera.getTileWidth() / 2, camera.getYCoord(tileY) - camera.getTileHeight());
        } else {
            g.drawImage(star.getImage(), camera.getXCoord(tileX) + (camera.getTileWidth() - star.getImage().getWidth()) / 2, camera.getYCoord(tileY) + (camera.getTileHeight() - star.getImage().getHeight()) / 2);
            g.setColor(Color.white);

            g.drawString(name,   camera.getXCoord(tileX) + (camera.getTileWidth() - g.getFont().getWidth(name)) / 2, camera.getYCoord(tileY) + star.getImage().getHeight() / 2 + AuroraGame.tileSize / 2);
            if (messageForStarMap != null) {
                //g.drawString(messageForStarMap,   camera.getXCoord(tileX) + (camera.getTileWidth() - g.getFont().getWidth(messageForStarMap)) / 2, camera.getYCoord(tileY) + star.getImage().getHeight() / 2 + AuroraGame.tileSize);
                EngineUtils.drawDashedCircleCentered(g, camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, (int) (star.getImage().getHeight() * 0.8), Color.green, 20);
            }
        }
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    /**
     * Sync local reputation with a global one.
     * Do it only if some NPC ships are left in star system.
     * If player has killed everybody, evidently there is no one left, who could tell that it was player fault, so
     * his rep with other races will not decrease
     */
    private void checkAndSynchronizeReputation(World world) {
        for (GameObject so : ships) {
            if (so instanceof NPCShip) {
                world.getReputation().merge(reputation);
                return;
            }
        }
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

        if ((y < -radius)
                || (y >= radius)
                || (x < -radius)
                || (x >= radius)) {
            if (canBeLeft) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "space.leaving_star_system"));
                checkAndSynchronizeReputation(world);
                world.setCurrentRoom(world.getGalaxyMap());
                world.getGalaxyMap().enter(world);
                player.getShip().setPos(globalMapX, globalMapY);
                // do not keep background
                background = null;
                world.onPlayerLeftSystem(this);
                return;
            } else {
                if (world.isUpdatedThisFrame()) {
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "space.can_not_leave_star_system"));
                }
            }
        }

        final List<GameObject> spaceObjectAtPlayerShipPosition = getGameObjectsAtPosition(player.getShip());


        // if user ship is at planet, show additional gui panel
        final Element scanLandPanel = GUI.getInstance().getNifty().getScreen("star_system_gui").findElementByName("interactPanel");
        if (scanLandPanel != null) {
            boolean landPanelVisible = scanLandPanel.isVisible();
            if (landPanelVisible && spaceObjectAtPlayerShipPosition.isEmpty()) {
                scanLandPanel.setVisible(false);
            } else if (!landPanelVisible && !spaceObjectAtPlayerShipPosition.isEmpty()) {
                Button leftButton = scanLandPanel.findNiftyControl("left_button", Button.class);
                leftButton.setText(spaceObjectAtPlayerShipPosition.get(0).getInteractMessage());
                scanLandPanel.setVisible(true);
            }
        }

        if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))) {
            interactWithObjectAtShipPosition(world);
        }
    }

    public void interactWithObjectAtShipPosition(final World world) {
        final List<GameObject> spaceObjectAtPlayerShipPosition = getGameObjectsAtPosition(world.getPlayer().getShip());

        if (spaceObjectAtPlayerShipPosition.isEmpty()) {
            return;
        }
        if (spaceObjectAtPlayerShipPosition.size() == 1) {
            spaceObjectAtPlayerShipPosition.get(0).interact(world);
            return;
        }

        InteractionTargetSelectorController.open(new IStateChangeListener<GameObject>() {
            private static final long serialVersionUID = -8114467555795780919L;

            @Override
            public void stateChanged(GameObject param) {
                param.interact(world);
            }
        }, spaceObjectAtPlayerShipPosition);
    }


    public List<GameObject> getGameObjectsAtPosition(Positionable pos) {
        List<GameObject> rz = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();
        for (GameObject p : ships) {
            if (x == p.getX() && y == p.getY()) {
                rz.add(p);
            }
        }

        for (BasePlanet p : planets) {
            if (x == p.getX() && y == p.getY()) {
                rz.add(p);
            }
            if (p.getSatellites() != null) {
                for (BasePlanet s : p.getSatellites()) {
                    if (x == s.getX() && y == s.getY()) {
                        rz.add(s);
                    }
                }
            }
        }
        return rz;
    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        try {
            ois.defaultReadObject();
            effects = new PriorityQueue<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateShoot(final World world, boolean next, boolean prev, boolean shoot) {
        if (ships.isEmpty()) {
            // nothing to shoot at
            return;
        }
        int targetIdx = 0;
        List<GameObject> availableTargets = new ArrayList<>();

        final Ship playerShip = world.getPlayer().getShip();
        final WeaponInstance weapon = playerShip.getWeapons().get(selectedWeapon);

        if (target != null && (!target.isAlive() || playerShip.getDistance(target) > weapon.getWeaponDesc().getRange())) {
            // target moved out of range
            target = null;
        }

        //TODO: firing sectors
        for (GameObject spaceObject : ships) {
            if (spaceObject.canBeAttacked() && weapon.getWeaponDesc().getRange() >= playerShip.getDistance(spaceObject) && playerShip.getFaction() != spaceObject.getFaction()) {
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
                GameLogger.getInstance().logMessage(Localization.getText("gui", "space.weapon_not_reloaded"));
                return;
            }

            // firing
            final int damage = weapon.getWeaponDesc().getDamage();

            List<GameObject> targetsAtSamePosition = getGameObjectsAtPosition(target);

            for (Iterator<GameObject> iterator = targetsAtSamePosition.iterator(); iterator.hasNext(); ) {
                GameObject so = iterator.next();
                if (!so.canBeAttacked()) {
                    iterator.remove();
                }
            }

            if (targetsAtSamePosition.size() <= 1) {
                checkIsFriendlyAndFire(world, target, playerShip, weapon, damage);
            } else {
                InteractionTargetSelectorController.open(new IStateChangeListener<GameObject>() {
                    private static final long serialVersionUID = 1084963569632582987L;

                    @Override
                    public void stateChanged(GameObject param) {
                        checkIsFriendlyAndFire(world, param, playerShip, weapon, damage);
                    }
                }, targetsAtSamePosition);
            }
        }
    }

    private void checkIsFriendlyAndFire(World world, final GameObject targetObject, final Ship playerShip, WeaponInstance weapon, final int damage) {
        if ((targetObject instanceof NPCShip && ((NPCShip) targetObject).isHostile) || (targetObject.getFaction() == null || targetObject.getFaction().isHostileTo(world, world.getPlayer().getShip()))) {
            doFire(world, targetObject, playerShip, weapon, damage);
            return;
        }
        // show confirmation popup
        FriendlyAttackConfirmationController.open(world, targetObject, weapon, damage);
    }


    public void doFire(World world, final GameObject targetObject, final Ship playerShip, WeaponInstance weapon, final int damage) {
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.player_attack"), damage, targetObject.getName()));

        Effect e = weapon.getWeaponDesc().createShotEffect(playerShip, targetObject, world.getCamera(), 800);
        e.setEndListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = 8150717419595750398L;

            @Override
            public void stateChanged(World world) {
                targetObject.onAttack(world, playerShip, damage);
                if (!target.isAlive()) {
                    GameLogger.getInstance().logMessage(targetObject.getName() + " " + Localization.getText("gui", "space.destroyed"));
                    target = null;
                }
            }
        });
        effects.add(e);

        ResourceManager.getInstance().getSound(weapon.getWeaponDesc().shotSound).play();


        weapon.fire();
        world.setUpdatedThisFrame(true);

        onWeaponButtonPressed(world, selectedWeapon);
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
                GameLogger.getInstance().logMessage(Localization.getText("gui", "space.no_weapon_in_slot") + " " + (1 + selectedWeapon));
                return;
            }

            if (playerShip.getWeapons().get(selectedWeapon).getReloadTimeLeft() > 0) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "space.weapon_not_reloaded"));
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

        if (currentEffect == null && !effects.isEmpty()) {
            currentEffect = effects.remove();
            if (currentEffect != null && currentEffect.getStartSound() != null) {
                ResourceManager.getInstance().getSound(currentEffect.getStartSound()).play();
            }
        }
        if (currentEffect != null) {
            currentEffect.update(container, world);
            if (currentEffect.isOver()) {
                currentEffect.onOver(world);
                currentEffect = null;
            }
            return;
        }

        final Ship playerShip = world.getPlayer().getShip();

        if (mode == MODE_MOVE) {
            updateMove(container, world);
            for (int i = 0; i < InputBinding.weapons.length; ++i) {
                if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.weapons[i]))) {
                    onWeaponButtonPressed(world, i);
                    return;
                }
            }
        } else {
            updateShoot(
                    world
                    , container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP))
                            || container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT))
                    , container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN))
                            || container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT))
                    , container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.SHOOT))
                            || container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
                            || container.getInput().isKeyPressed(selectedWeapon + Input.KEY_1)
            );
            if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                onWeaponButtonPressed(world, -1);
            }
        }

        boolean shipAtSameCoords = false;
        List<GameObject> shipsCopy = new LinkedList<>(ships); //to prevent CMO
        for (GameObject ship : shipsCopy) {
            if (ship.getX() == playerShip.getX() && ship.getY() == playerShip.getY()) {
                shipAtSameCoords = true;
            }

            ship.update(container, world);
        }

        for (Iterator<GameObject> iter = ships.iterator(); iter.hasNext(); ) {
            GameObject ship = iter.next();
            if (!ship.isAlive()) {
                iter.remove();
            }
        }

        final Element scanLandPanel = GUI.getInstance().getNifty().getScreen("star_system_gui").findElementByName("interactPanel");
        if (scanLandPanel != null) {
            boolean landPanelVisible = scanLandPanel.isVisible();
            if (!shipAtSameCoords && landPanelVisible && getGameObjectsAtPosition(player.getShip()).isEmpty()) {
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
        reputation = world.getReputation().copy();
        player = world.getPlayer();
        player.getShip().setPos(-radius + 1, 0);
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
        world.getCamera().resetViewPort();
        visited = true;
    }

    @Override
    public void returnTo(World world) {
        world.getCamera().setTarget(player.getShip());
        if (background == null) {
            createBackground(world);
        }
        world.getCamera().resetViewPort();
        GUI.getInstance().getNifty().gotoScreen("star_system_gui");
    }

    @Override
    public ITileMap getMap() {
        return this;
    }

    @Override
    public boolean turnIsADay() {
        return true;
    }

    private void createBackground(World world) {
        background = new ParallaxBackground(
                radius * 3 * (int) world.getCamera().getTileWidth()
                , radius * 3 * (int) world.getCamera().getTileHeight()
                , 0//planets.length * world.getCamera().getTileWidth()
                , 0//planets.length * world.getCamera().getTileHeight()
                , planets.length);
    }


    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        if (background != null) {
            if (backgroundNebula1 != null) {
                final Random r = CommonRandom.getRandom();
                Image nebula1 = ResourceManager.getInstance().getImage(backgroundNebula1);
                //Если отрисовка происходит впервые - задаём случайное положение спрайта туманности (зависит от разрешения)
                if (nebula1Offset == null) {
                    nebula1Offset = new Point(
                            r.nextFloat() * (camera.getViewportTilesX() * camera.getTileWidth() - nebula1.getWidth()),
                            r.nextFloat() * (camera.getViewportTilesY() * camera.getTileHeight() - nebula1.getHeight()));
                }
                float x = background.getXCoordPoint(camera, 0, ParallaxBackground.PLANES_COUNT + 1) + nebula1Offset.getX();
                float y = background.getYCoordPoint(camera, 0, ParallaxBackground.PLANES_COUNT + 1) + nebula1Offset.getY();
                g.drawImage(nebula1, x, y);
            }
            if (backgroundNebula2 != null) {
                final Random r = CommonRandom.getRandom();
                Image nebula2 = ResourceManager.getInstance().getImage(backgroundNebula2);
                if (nebula2Offset == null) {
                    nebula2Offset = new Point(
                            r.nextFloat() * (camera.getViewportTilesX() * camera.getTileWidth() - nebula2.getWidth()),
                            r.nextFloat() * (camera.getViewportTilesY() * camera.getTileHeight() - nebula2.getHeight()));
                }
                float x = background.getXCoordPoint(camera, 0, ParallaxBackground.PLANES_COUNT + 1) + nebula2Offset.getX();
                float y = background.getYCoordPoint(camera, 0, ParallaxBackground.PLANES_COUNT + 1) + nebula2Offset.getY();
                g.drawImage(nebula2, x, y);
            }
            background.draw(g, camera);
            if (backgroundSprite != null) {
                float x = background.getXCoordPoint(camera, -100, -1);
                float y = background.getYCoordPoint(camera, -100, -1);
                g.drawImage(ResourceManager.getInstance().getImage(backgroundSprite), x, y);
            }
        }


        final float starX = camera.getXCoord(0) + (camera.getTileWidth() / 2);
        final float starY = camera.getYCoord(0) + camera.getTileHeight() / 2;

        if (star != null) {
            g.setColor(star.color);
            final Image starImage = star.getImage();
            g.drawImage(starImage, starX - starImage.getWidth() / 2, starY - starImage.getHeight() / 2);
        }

        // first draw all orbits
        for (BasePlanet p : planets) {
            float planetX = camera.getXCoord(p.getX()) + (camera.getTileWidth() / 2);
            float planetY = camera.getYCoord(p.getY()) + camera.getTileWidth() / 2;
            int radius = (int) Math.sqrt(Math.pow((planetX - starX), 2) + Math.pow((planetY - starY), 2));
            EngineUtils.drawDashedCircleCentered(g, starX, starY, radius, new Color(30, 30, 100));

            if (p.getSatellites() != null) {
                for (BasePlanet satellite : p.getSatellites()) {
                    float satelliteX = camera.getXCoord(satellite.getX()) + (camera.getTileWidth() / 2);
                    float satelliteY = camera.getYCoord(satellite.getY()) + camera.getTileWidth() / 2;
                    radius = (int) Math.sqrt(Math.pow((satelliteX - planetX), 2) + Math.pow((satelliteY - planetY), 2));
                    EngineUtils.drawDashedCircleCentered(g, planetX, planetY, radius, new Color(30, 30, 100));
                }
            }
        }

        // and then all planets
        for (BasePlanet p : planets) {
            p.drawOnGlobalMap(container, g, camera, 0, 0);
            if (p.getSatellites() != null) {
                for (BasePlanet satellite : p.getSatellites()) {
                    satellite.drawOnGlobalMap(container, g, camera, 0, 0);
                }
            }
        }

        //belt
        if (asteroidBelt != null) {
            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {

                    float screenX = camera.getXCoord(x);
                    float screenY = camera.getYCoord(y);
                    if (screenX >= - camera.getTileWidth() && screenX <= (camera.getNumTilesX() + 1) * camera.getTileWidth()
                            && screenY >= - camera.getTileHeight() && screenY <= (camera.getNumTilesY() + 1) * camera.getTileHeight()) {
                        long dist = Math.round(Math.sqrt(Math.pow(0 - x, 2) + Math.pow(0 - y, 2)));
                        if (dist >= asteroidBelt.innerRadius && dist < asteroidBelt.innerRadius + asteroidBelt.width) {
                            Image asteroidSprite = ResourceManager.getInstance().getImage("asteroids");
                            g.drawImage(asteroidSprite, screenX, screenY);
                        }
                    }
                }
            }
        }

        // now draw effects that are beyond starships
        if (currentEffect != null && currentEffect.getOrder() == Effect.DrawOrder.BACK) {
            currentEffect.draw(container, g, camera, world);
        }


        final int selectedWeaponRange;
        if (mode == MODE_SHOOT) {
            selectedWeaponRange = player.getShip().getWeapons().get(selectedWeapon).getWeaponDesc().getRange();
        } else {
            selectedWeaponRange = 0;
        }
        g.setColor(Color.red);
        for (GameObject ship : ships) {
            ship.draw(container, g, camera, world);
            if (mode == MODE_SHOOT && ship.canBeAttacked() && player.getShip().getDistance(ship) < selectedWeaponRange && player.getShip().getFaction() != ship.getFaction()) {
                // every targetable ship is surrounded by rectangle
                g.drawRect(camera.getXCoord(ship.getX()), camera.getYCoord(ship.getY()), camera.getTileWidth(), camera.getTileHeight());
            }
        }

        if (mode == MODE_SHOOT) {
            if (target != null && target.isAlive()) {
                // draw target mark
                g.drawImage(ResourceManager.getInstance().getImage("target"), camera.getXCoord(target.getX()), camera.getYCoord(target.getY()));
            }

            EngineUtils.drawTileCircleCentered(g, camera, selectedWeaponRange);
        }

        if (canBeLeft) {
            g.setColor(Color.red);
            g.drawRect(camera.getXCoord(-radius), camera.getYCoord(-radius), 2 * radius * camera.getTileWidth(), 2 * radius * camera.getTileHeight());
        }
        g.setColor(Color.white);

        player.getShip().draw(container, g, camera, world);

        if (currentEffect != null && currentEffect.getOrder() == Effect.DrawOrder.FRONT) {
            currentEffect.draw(container, g, camera, world);
        }
    }

    public void setRandomEmptyPosition(Positionable object, double minRadius, double maxRadius) {
        int orbit;

        minRadius *= radius;
        maxRadius *= radius;

        boolean isEmpty;
        do {
            orbit = Math.max(1, (int) (CommonRandom.getRandom().nextDouble() * (maxRadius - minRadius) + minRadius));
            final int x = CommonRandom.getRandom().nextInt(2 * orbit) - orbit;
            final int y = (int) (Math.sqrt(orbit * orbit - x * x) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));
            object.setPos(x, y);
            isEmpty = true;
            // check that there are no planets or other objects at this position
            for (GameObject obj : ships) {
                if (obj == object) {
                    continue;
                }
                if (BasePositionable.getDistance(obj, object) == 0) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                for (BasePlanet p : planets) {
                    if (p.getDistance(object) == 0) {
                        isEmpty = false;
                        break;
                    }
                }
            }
        } while (!isEmpty);
    }

    /**
     * Sets random position of a given object within this star system.
     * Not within sun, not near borders, on an empty spot
     */
    public void setRandomEmptyPosition(Positionable object) {
        final double maxRadius = Configuration.getDoubleProperty("world.starsystem.objectMaxRadius");
        final double minRadius = Configuration.getDoubleProperty("world.starsystem.objectMinRadius");
        setRandomEmptyPosition(object, minRadius, maxRadius);
    }

    public List<GameObject> getShips() {
        return ships;
    }

    public int getAstronomyData() {
        return astronomyData;
    }

    public void setAstronomyData(int astronomyData) {
        this.astronomyData = astronomyData;
    }

    public int getX() {
        return globalMapX;
    }

    public int getY() {
        return globalMapY;
    }

    @Override
    public void setPos(int newX, int newY) {
        globalMapX = newX;
        globalMapY = newY;
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

    public void setBackgroundNebula1(String nebula1Sprite) {
        this.backgroundNebula1 = nebula1Sprite;
    }

    public void setBackgroundNebula2(String nebula2Sprite) {
        this.backgroundNebula2 = nebula2Sprite;
    }

    public ParallaxBackground getBackground() {
        return background;
    }

    public Reputation getReputation() {
        return reputation;
    }

    public void setCanBeLeft(boolean canBeLeft) {
        this.canBeLeft = canBeLeft;
    }

    public String getCoordsString() {
        return String.format("[%d, %d]", globalMapX, globalMapY);
    }

    public String getMessageForStarMap() {
        return messageForStarMap;
    }

    public void setMessageForStarMap(String messageForStarMap) {
        if (messageForStarMap != null && messageForStarMap.isEmpty()) {
            this.messageForStarMap = null;
            return;
        }
        this.messageForStarMap = messageForStarMap;
    }

    public Star getStar() {
        return star;
    }

    @Override
    public List<GameObject> getObjects() {
        return ships;
    }

    @Override
    public Object getUserData() {
        return null;
    }

    @Override
    public void setUserData(Object o) {

    }

    @Override
    public int getWidthInTiles() {
        return radius * 2;
    }

    @Override
    public int getHeightInTiles() {
        return radius * 2;
    }

    private static class AsteroidBelt implements Serializable {
        private static final long serialVersionUID = 652085640285216434L;

        private final int innerRadius;

        private final int width;

        public AsteroidBelt(int innerRadius, int width) {
            this.innerRadius = innerRadius;
            this.width = width;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StarSystem that = (StarSystem) o;

        if (globalMapX != that.globalMapX) return false;
        if (globalMapY != that.globalMapY) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = globalMapX;
        result = 31 * result + globalMapY;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}

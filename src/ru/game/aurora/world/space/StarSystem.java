/**
 * User: jedi-philosopher
 * Date: 30.11.12
 * Time: 22:42
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.player.Player;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.planet.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject {

    public static final Color[] possibleColors = {Color.red, Color.white, Color.yellow, new Color(122, 155, 243)};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    private static final long serialVersionUID = -1L;

    public static class Star implements Serializable {
        private static final long serialVersionUID = -3746922025754658839L;
        // 1 is largest star, 4 is smallest
        public final int size;

        public final Color color;

        // colors for drawing in star system view
        // star consists of 3 gradients of main color
        private transient Color coreColor;

        private transient Color outerColor;

        public Star(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        public Color getCoreColor() {
            if (coreColor == null) {
                coreColor = EngineUtils.lightenColor(color);
            }
            return coreColor;
        }

        public Color getOuterColor() {
            if (outerColor == null) {
                outerColor = EngineUtils.darkenColor(color, 0.75f);
            }
            return outerColor;
        }
    }

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
        g.setColor(star.color);
        EngineUtils.drawCircleCentered(g, camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, camera.getTileWidth() / star.size, star.color, true);
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

        if (container.getInput().isKeyPressed(Input.KEY_E)) {
            LandingPartyEquipScreen screen = new LandingPartyEquipScreen(false);
            screen.enter(world);
            world.setCurrentRoom(screen);
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


        for (BasePlanet p : planets) {
            if (x == p.getGlobalX() && y == p.getGlobalY()) {
                if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                    GameLogger.getInstance().logMessage("Descending to surface...");

                    world.setCurrentRoom(p);

                    final LandingParty landingParty = world.getPlayer().getLandingParty();
                    if (p.canBeLanded() && (landingParty == null || !landingParty.canBeLaunched(world))) {
                        // first landing, show 'Landing party equipment' screen

                        LandingPartyEquipScreen screen = new LandingPartyEquipScreen(true);
                        screen.enter(world);
                        world.setCurrentRoom(screen);
                        if (world.getGlobalVariables().containsKey("tutorial.landing")) {
                            // this is first landing on a planet, show tutorial dialog
                            Dialog d = Dialog.loadFromFile("dialogs/tutorials/planet_landing_tutorial.json");
                            world.addOverlayWindow(d);
                            world.getGlobalVariables().remove("tutorial.landing");
                        }
                    } else {
                        p.enter(world);
                    }
                    break;
                } else if (container.getInput().isKeyPressed(Input.KEY_S)) {
                    if (!(p instanceof Planet)) {
                        GameLogger.getInstance().logMessage("Can not scan this planet");
                        return;
                    }
                    PlanetScanScreen s = new PlanetScanScreen(this, (Planet) p);
                    s.enter(world);
                    world.setCurrentRoom(s);
                    return;
                } else if (world.isUpdatedThisFrame()) {
                    p.processCollision(container, world.getPlayer());
                    break;
                }
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        try {
            ois.defaultReadObject();
            effects = new LinkedList<Effect>();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateShoot(GameContainer container, World world) {
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

        GameLogger.getInstance().addStatusMessage("Press <enter> to fire from weapon " + selectedWeapon);
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

        if (container.getInput().isKeyPressed(Input.KEY_F) || container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(selectedWeapon + Input.KEY_1)) {

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
                    selectedWeapon = i - Input.KEY_1;
                    if (playerShip.getWeapons().size() <= selectedWeapon || playerShip.getWeapons().get(selectedWeapon) == null) {
                        GameLogger.getInstance().logMessage("No weapon in slot " + (1 + selectedWeapon));
                        return;
                    }

                    if (playerShip.getWeapons().get(selectedWeapon).getReloadTimeLeft() > 0) {
                        GameLogger.getInstance().logMessage("Weapon not yet reloaded");
                        return;
                    }

                    mode = MODE_SHOOT;
                    return;

                }
            }
        } else {
            updateShoot(container, world);
            if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                mode = MODE_MOVE;
            }
        }

        for (Iterator<SpaceObject> iter = ships.iterator(); iter.hasNext(); ) {
            SpaceObject ship = iter.next();

            if (ship.getX() == playerShip.getX() && ship.getY() == playerShip.getY()) {
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
        world.onPlayerEnteredSystem(this);

        if (firstEnterDialog != null && !visited) {
            world.addOverlayWindow(firstEnterDialog);
        }

        visited = true;
        GUI.getInstance().getNifty().gotoScreen("star_system_gui");
    }

    private void createBackground(World world) {
        background = new ParallaxBackground(
                radius * 3 * world.getCamera().getTileWidth()
                , radius * 3 * world.getCamera().getTileHeight()
                , 0//planets.length * world.getCamera().getTileWidth()
                , 0//planets.length * world.getCamera().getTileHeight()
                , planets.length);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        if (background != null) {
            background.draw(g, camera);
            if (backgroundSprite != null) {
                float x = background.getXCoordPoint(camera, -100, -1);
                float y = background.getYCoordPoint(camera, -100, -1);
                g.drawImage(ResourceManager.getInstance().getImage(backgroundSprite), x, y);
            }
        }

        player.getShip().draw(container, g, camera);
        player.addGlobalStatus();
        GameLogger.getInstance().addStatusMessage("==========================");
        g.setColor(star.color);

        final int starX = camera.getXCoord(0) + (camera.getTileWidth() / 2);
        final int starY = camera.getYCoord(0) + camera.getTileHeight() / 2;
        if (camera.isInViewport(0, 0)) {
            // draw 3 circles
            EngineUtils.drawCircleCentered(g, starX, starY, (int) (camera.getTileWidth() * STAR_SCALE_FACTOR / (2 * star.size) * 1.25), star.getOuterColor(), true);
            EngineUtils.drawCircleCentered(g, starX, starY, camera.getTileWidth() * STAR_SCALE_FACTOR / (2 * star.size), star.color, true);
            EngineUtils.drawCircleCentered(g, starX, starY, (int) (camera.getTileWidth() * STAR_SCALE_FACTOR / (2 * star.size) * 0.75), star.getCoreColor(), true);
        }

        for (BasePlanet p : planets) {
            if (p.getGlobalX() == player.getShip().getX() && p.getGlobalY() == player.getShip().getY()) {
                GameLogger.getInstance().addStatusMessage("Approaching planet: ");
                GameLogger.getInstance().addStatusMessage("Press <S> to scan");
                GameLogger.getInstance().addStatusMessage("Press <enter> to launch surface party");
            }

            int planetX = camera.getXCoord(p.getGlobalX()) + (camera.getTileWidth() / 2);
            int planetY = camera.getYCoord(p.getGlobalY()) + camera.getTileWidth() / 2;
            int radius = (int) Math.sqrt(Math.pow((planetX - starX), 2) + Math.pow((planetY - starY), 2));
            EngineUtils.drawCircleCentered(g, starX, starY, radius, Color.gray, false);
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
                GameLogger.getInstance().addStatusMessage("Press <enter> to contact");
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
}

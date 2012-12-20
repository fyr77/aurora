/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 18:46
 */
package ru.game.aurora.world.planet;

import jgame.JGColor;
import jgame.JGPoint;
import jgame.impl.JGEngineInterface;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.Player;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Class for planet.
 * Draws itself on Star System map.
 * Contains planetary data - atmosphere, size, flora and fauna (if any)
 * Contains Room - planet surface, with logic for landing party movement.
 */
public class Planet implements Room, GalaxyMapObject {

    /**
     * Mode for moving. Arrows control landing party movement.
     */
    private static final int MODE_MOVE = 0;

    /**
     * Mode for shooting. Arrows control target selection.
     */
    private static final int MODE_SHOOT = 1;

    /**
     * Current mode
     */
    private int mode = MODE_MOVE;

    private StarSystem owner;

    private PlanetCategory category;

    private PlanetAtmosphere atmosphere;

    /**
     * Planet size type. 1 is largest, 4 is smallest.
     * Planet image size on global map and dimensions of planet surface depends on it.
     */
    private int size;

    /**
     * Tiles with planet surface.
     * Absolute value of each element is tile type
     * If value is negative, this means this tile is not yet explored, it is shown as black square
     */
    private byte[][] surface;

    private int width;

    private int height;

    private static final Random r = new Random();

    private LandingParty landingParty;

    /**
     * Where landing shuttle is located. Launching to orbit and refilling oxygen is available at shuttle
     */
    private JGPoint shuttlePosition;

    /**
     * Position of planet in star system
     */
    private int globalX;

    private int globalY;

    /**
     * Available animal species descriptions, if any.
     */
    private AnimalSpeciesDesc[] animalSpecies;

    /**
     * Animals that are located on planet surface.
     */
    private List<PlanetObject> planetObjects = new ArrayList<PlanetObject>();

    /**
     * When in fire mode, this is currently selected target
     */
    private PlanetObject target = null;

    public Planet(StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y, boolean hasLife) {
        this.owner = owner;
        this.category = cat;
        this.atmosphere = atmosphere;
        this.size = size;
        this.globalX = x;
        this.globalY = y;
        switch (size) {
            case 1:
                this.width = 500;
                this.height = 500;
                break;
            case 2:
                this.width = 300;
                this.height = 300;
                break;
            case 3:
                this.width = 200;
                this.height = 200;
                break;
            case 4:
                this.width = 100;
                this.height = 100;
                break;
            default:
                throw new IllegalArgumentException("Unsupported planet size value");
        }
        // different planets will have different probabilities for tiles
        ProbabilitySet<Byte> ps = new ProbabilitySet<Byte>();
        for (byte b : category.availableSurfaces) {
            ps.put(b, r.nextDouble() * (r.nextInt(5) + 1));
        }

        surface = new byte[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                surface[i][j] = (byte) -ps.getRandom();
            }
        }

        final int resourceDeposits = r.nextInt(40 / size);
        for (int i = 0; i < resourceDeposits; ++i) {
            OreDeposit d = new OreDeposit(this, r.nextInt(10), r.nextInt(10), CollectionUtils.selectRandomElement(OreDeposit.OreType.values()), r.nextInt(3) + 1);
            int oreX;
            int oreY;
            do {
                oreX = r.nextInt(10);
                oreY = r.nextInt(10);
            } while (!SurfaceTypes.isPassible(surface[oreY][oreX]));
            d.setPos(oreX, oreY);
            planetObjects.add(d);
        }

        if (hasLife) {
            // generate random species descs. Currently only one
            animalSpecies = new AnimalSpeciesDesc[1];
            animalSpecies[0] = new AnimalSpeciesDesc(this, "Alien mammoth", "mammal_large_1", "mammal_large_1_dead", false, true, 3, 0, AnimalSpeciesDesc.Behaviour.PASSIVE);

            final int animalCount = r.nextInt(10) + 5;
            for (int i = 0; i < animalCount; ++i) {
                Animal a = new Animal(this, 0, 0, animalSpecies[0]);
                int animalX;
                int animalY;
                do {
                    animalX = r.nextInt(10);
                    animalY = r.nextInt(10);
                } while (!SurfaceTypes.isPassible(a, surface[animalY][animalX]));
                a.setPos(animalX, animalY);
                planetObjects.add(a);
            }
        }
    }


    @Override
    public void enter(World world) {
        landingParty = world.getPlayer().getLandingParty();
        int x = landingParty.getX();
        int y = landingParty.getY();

        while (!SurfaceTypes.isPassible(landingParty, surface[y][x])) {
            x++;
            y += r.nextInt(2) - 1;
        }
        landingParty.setPos(x, y);

        world.getCamera().setTarget(landingParty);
        shuttlePosition = new JGPoint(landingParty.getX(), landingParty.getY());
        int openedTiles = updateVisibility(landingParty.getX(), landingParty.getY(), 5);
        landingParty.addCollectedGeodata(openedTiles);
    }

    public int wrapX(int x) {
        if (x < 0) {
            return width + x;
        } else if (x >= width) {
            return x - width;
        }
        return x;
    }

    public int wrapY(int y) {
        if (y < 0) {
            return height + y;
        } else if (y >= height) {
            return y - height;
        }
        return y;
    }

    /**
     * Updates planet map. Makes tiles visible in given range from given point
     *
     * @return Amount of tiles opened
     */
    private int updateVisibility(int x, int y, int range) {
        int rz = 0;
        for (int i = y - range; i <= y + range; ++i) {
            for (int j = x - range; j <= x + range; ++j) {
                int pointX = wrapX(j);
                int pointY = wrapY(i);
                if (surface[pointY][pointX] < 0) {
                    surface[pointY][pointX] *= -1;
                    ++rz;
                }
            }
        }
        return rz;
    }

    /**
     * This update is used in MOVE mode. Moving landing party around.
     */
    private void updateMove(JGEngine engine, World world) {
        int x = world.getPlayer().getLandingParty().getX();
        int y = world.getPlayer().getLandingParty().getY();

        if (engine.getKey(JGEngineInterface.KeyUp)) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyDown)) {
            y++;
            world.setUpdatedThisFrame(true);
        }

        if (engine.getKey(JGEngineInterface.KeyLeft)) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (engine.getKey(JGEngineInterface.KeyRight) && x < engine.getWidth()) {
            x++;
            world.setUpdatedThisFrame(true);
        }

        x = wrapX(x);
        y = wrapY(y);

        if (!SurfaceTypes.isPassible(landingParty, surface[y][x])) {
            world.setUpdatedThisFrame(false);
            x = world.getPlayer().getLandingParty().getX();
            y = world.getPlayer().getLandingParty().getY();
        }

        if (engine.getKey(JGEngineInterface.KeyEnter)) {
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


        int tilesExplored = updateVisibility(x, y, 1);
        landingParty.addCollectedGeodata(tilesExplored);

        if (x == shuttlePosition.x && y == shuttlePosition.y) {

            if (world.isUpdatedThisFrame()) {
                GameLogger.getInstance().logMessage("Refilling oxygen");
                world.getPlayer().getLandingParty().refillOxygen();
            }
            if (engine.getKey(JGEngine.KeyEnter)) {
                GameLogger.getInstance().logMessage("Launching shuttle to orbit...");
                world.setCurrentRoom(owner);
                owner.enter(world);
                world.getPlayer().getShip().setPos(globalX, globalY);
                engine.clearKey(JGEngine.KeyEnter);
                landingParty.onReturnToShip(world);
            }
        }
        world.getPlayer().getLandingParty().setPos(x, y);
    }

    private int getDist(int first, int second, int total) {
        int max = Math.max(first, second);
        int min = Math.min(first, second);

        return Math.min(max - min, total + min - max);

    }

    private int getRange(LandingParty party, Positionable target) {
        int xDist = getDist(party.getX(), target.getX(), width);
        int yDist = getDist(party.getY(), target.getY(), height);
        return xDist + yDist;
    }

    /**
     * This update method is used in FIRE mode. Selecting targets and shooting.
     */
    private void updateShoot(JGEngine engine, World world) {
        if (planetObjects.isEmpty()) {
            return;
        }
        int targetIdx = 0;
        List<PlanetObject> availableTargets = new ArrayList<PlanetObject>();

        if (target != null && getRange(landingParty, target) > landingParty.getWeapon().getRange()) {
            // target moved out of range
            target = null;
        }

        for (PlanetObject planetObject : planetObjects) {
            if (!planetObject.canBeShotAt()) {
                continue;
            }
            if (surface[planetObject.getY()][planetObject.getX()] < 0) {
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

        if (engine.getKey(JGEngine.KeyUp) || engine.getKey(JGEngine.KeyRight)) {
            targetIdx++;
            if (targetIdx >= availableTargets.size()) {
                targetIdx = 0;
            }
        } else if (engine.getKey(JGEngine.KeyDown) || engine.getKey(JGEngine.KeyLeft)) {
            targetIdx--;
            if (targetIdx < 0) {
                targetIdx = availableTargets.size() - 1;
            }
        }

        target = availableTargets.get(targetIdx);

        if (engine.getLastKeyChar() == 'f' || engine.getKey(JGEngine.KeyEnter)) {
            // firing
            final int damage = landingParty.calcDamage();
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

    @Override
    public void update(JGEngine engine, World world) {
        char c = engine.getLastKeyChar();

        switch (mode) {
            case MODE_MOVE:
                if (c == 'f') {
                    mode = MODE_SHOOT;
                    return;
                }
                updateMove(engine, world);
                break;
            case MODE_SHOOT:
                if (engine.getKey(JGEngine.KeyEsc)) {
                    mode = MODE_MOVE;
                    return;
                }
                updateShoot(engine, world);
                break;
            default:
                throw new IllegalStateException("Unknown planet update type " + mode);

        }

        if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE && world.isUpdatedThisFrame()) {
            world.getPlayer().getLandingParty().consumeOxygen();
        }

        if (world.getPlayer().getLandingParty().getOxygen() < 0) {
            GameLogger.getInstance().logMessage("Lost connection with landing party");
            world.setCurrentRoom(owner);
            owner.enter(world);
            world.getPlayer().getShip().setPos(globalX, globalY);
            engine.clearKey(JGEngine.KeyEnter);
        }

        for (PlanetObject a : planetObjects) {
            a.update(engine, world);
        }
    }

    public void printPlanetStatus() {
        GameLogger.getInstance().addStatusMessage("Planet info:");
        GameLogger.getInstance().addStatusMessage(String.format("Size: [%d, %d]", width, height));
        GameLogger.getInstance().addStatusMessage("Atmosphere: " + atmosphere);
        GameLogger.getInstance().addStatusMessage("=====================================");
    }

    public void drawLandscape(JGEngine engine, Camera camera, boolean detailed) {
        for (int i = camera.getTarget().getY() - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() + camera.getNumTilesY() / 2; ++i) {
            for (int j = camera.getTarget().getX() - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() + camera.getNumTilesX() / 2; ++j) {

                final byte type = surface[wrapY(i)][wrapX(j)];
                if (type < 0) {
                    continue;
                }
                if (detailed) {
                    SurfaceTypes.drawDetailed(
                            type
                            , camera.getXCoord(j)
                            , camera.getYCoord(i)
                            , camera.getTileWidth()
                            , camera.getTileHeight()
                            , engine);
                } else {
                    SurfaceTypes.drawSimple(
                            type
                            , camera.getXCoord(j)
                            , camera.getYCoord(i)
                            , camera.getTileWidth()
                            , camera.getTileHeight()
                            , engine);
                }
            }

        }
    }


    public void drawObjects(JGEngine engine, Camera camera) {
        // this part (monsters, shuttle, landing party) is drawn only when landing party is on surface
        if (landingParty != null) {
            landingParty.draw(engine, camera);


            engine.drawImage("shuttle", camera.getXCoordWrapped(shuttlePosition.x, width), camera.getYCoordWrapped(shuttlePosition.y, height));

            if (landingParty.getX() == shuttlePosition.x && landingParty.getY() == shuttlePosition.y) {
                GameLogger.getInstance().addStatusMessage("Press <enter> to return to orbit");
            }

            engine.setColor(JGColor.red);
            for (PlanetObject a : planetObjects) {
                // draw only if tile under this animal is visible
                if (surface[a.getY()][a.getX()] >= 0) {
                    a.draw(engine, camera);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && a.canBeShotAt() && getRange(landingParty, a) < landingParty.getWeapon().getRange()) {
                        engine.drawRect(camera.getXCoordWrapped(a.getX(), width), camera.getYCoordWrapped(a.getY(), height), camera.getTileWidth(), camera.getTileHeight(), false, false);
                    }
                }

                if (a.getX() == landingParty.getX() && a.getY() == landingParty.getY()) {
                    a.printStatusInfo();
                }

            }

            if (mode == MODE_SHOOT && target != null) {
                // draw target mark
                engine.drawImage(camera.getXCoordWrapped(target.getX(), width), camera.getYCoordWrapped(target.getY(), height), "target");
            }
            GameLogger.getInstance().addStatusMessage(mode == MODE_MOVE ? "MOVE" : "SHOOT");
        }
    }

    public byte getTileTypeAt(int x, int y) {
        return surface[y][x];
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        printPlanetStatus();
        drawLandscape(engine, camera, true);
        drawObjects(engine, camera);
    }

    @Override
    public void drawOnGlobalMap(JGEngine engine, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }
        JGColor color;
        switch (category) {
            case PLANET_ROCK:
                color = JGColor.grey;
                break;
            case PLANET_ICE:
                color = JGColor.white;
                break;
            default:
                color = JGColor.grey;
        }
        engine.setColor(color);
        engine.drawOval(
                camera.getXCoord(globalX) + (engine.tileWidth() / 2)
                , camera.getYCoord(globalY) + engine.tileWidth() / 2
                , StarSystem.PLANET_SCALE_FACTOR * engine.tileWidth() / size
                , StarSystem.PLANET_SCALE_FACTOR * engine.tileHeight() / size
                , true
                , true);
    }

    public int getGlobalX() {
        return globalX;
    }

    public int getGlobalY() {
        return globalY;
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(JGEngine engine, Player player) {
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

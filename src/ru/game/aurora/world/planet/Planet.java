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
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
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
    private List<Animal> animals = new ArrayList<Animal>();

    /**
     * When in fire mode, this is currently selected target
     */
    private Animal target = null;

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

        surface = new byte[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                surface[i][j] = (byte) (-category.availableSurfaces[r.nextInt(category.availableSurfaces.length)]);
            }
        }

        if (hasLife) {
            // generate random species descs. Currently only one
            animalSpecies = new AnimalSpeciesDesc[1];
            animalSpecies[0] = new AnimalSpeciesDesc(this, "Alien mammoth", "mammal_large_1", false, true, 3, 0, AnimalSpeciesDesc.Behaviour.PASSIVE);

            final int animalCount = r.nextInt(10) + 5;
            for (int i = 0; i < animalCount; ++i) {
                animals.add(new Animal(this, r.nextInt(/*width*/10), r.nextInt(/*height*/10), animalSpecies[0]));
            }
        }
    }


    @Override
    public void enter(World world) {
        landingParty = world.getPlayer().getLandingParty();
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
                if (landingParty.getCollectedGeodata() > 0) {
                    GameLogger.getInstance().logMessage("Adding " + landingParty.getCollectedGeodata() + " pieces of raw geodata");
                }
                world.getPlayer().getResearchState().getGeodata().addRawData(landingParty.getCollectedGeodata());
                landingParty.setCollectedGeodata(0);
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
        if (animals.isEmpty()) {
            return;
        }
        int targetIdx = 0;
        List<Animal> availableTargets = new ArrayList<Animal>();

        if (target != null && getRange(landingParty, target) > landingParty.getWeapon().getRange()) {
            // target moved out of range
            target = null;
        }

        for (Animal animal : animals) {
            if (surface[animal.getY()][animal.getX()] < 0) {
                // do not target animals on unexplored tiles
                continue;
            }
            if (landingParty.getWeapon().getRange() >= getRange(landingParty, animal)) {
                availableTargets.add(animal);
                if (target == null) {
                    target = animal;
                }

                if (target == animal) {
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
            target.setHp(target.getHp() - damage);
            GameLogger.getInstance().logMessage("Bang! Dealt " + damage + " damage to " + target.getDesc().getName());
            if (target.getHp() <= 0) {
                GameLogger.getInstance().logMessage(target.getDesc().getName() + " killed");
                animals.remove(target);
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

        for (Animal a : animals) {
            a.update(engine, world);
        }
    }

    public void printPlanetStatus() {
        GameLogger.getInstance().addStatusMessage("Planet info:");
        GameLogger.getInstance().addStatusMessage(String.format("Size: [%d, %d]", width, height));
        GameLogger.getInstance().addStatusMessage("Atmosphere: " + atmosphere);
        GameLogger.getInstance().addStatusMessage("=====================================");
    }

    public void drawLandscape(JGEngine engine, Camera camera) {
        for (int i = camera.getTarget().getY() - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() + camera.getNumTilesY() / 2; ++i) {
            for (int j = camera.getTarget().getX() - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() + camera.getNumTilesX() / 2; ++j) {
                JGColor color = JGColor.black;

                switch (surface[wrapY(i)][wrapX(j)]) {
                    case SurfaceTypes.DIRT:
                        color = JGColor.orange;
                        break;
                    case SurfaceTypes.ICE:
                        color = JGColor.white;
                        break;
                    case SurfaceTypes.ROCKS:
                        color = JGColor.grey;
                        break;
                    case SurfaceTypes.WATER:
                        color = JGColor.blue;
                        break;
                }
                engine.setColor(color);
                engine.drawRect(camera.getXCoord(j), camera.getYCoord(i), engine.tileWidth(), engine.tileHeight(), true, false);
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
            for (Animal a : animals) {
                // draw only if tile under this animal is visible
                if (surface[a.getY()][a.getX()] >= 0) {
                    a.draw(engine, camera);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && getRange(landingParty, a) < landingParty.getWeapon().getRange()) {
                        engine.drawRect(camera.getXCoordWrapped(a.getX(), width), camera.getYCoordWrapped(a.getY(), height), camera.getTileWidth(), camera.getTileHeight(), false, false);
                    }
                }
            }

            if (mode == MODE_SHOOT && target != null) {
                // draw target mark
                engine.drawImage(camera.getXCoordWrapped(target.getX(), width), camera.getYCoordWrapped(target.getY(), height), "target");
            }
            GameLogger.getInstance().addStatusMessage(mode == MODE_MOVE ? "MOVE" : "SHOOT");
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        printPlanetStatus();
        drawLandscape(engine, camera);
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
        engine.drawOval(camera.getXCoord(globalX) + (engine.tileWidth() / 2), camera.getYCoord(globalY) + engine.tileWidth() / 2, engine.tileWidth() / size, engine.tileHeight() / size, true, true);
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

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
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Random;

public class Planet implements Room, GalaxyMapObject {
    private StarSystem owner;

    private PlanetCategory category;

    private PlanetAtmosphere atmosphere;

    private int size;

    private byte[][] surface;

    private int width;

    private int height;

    private Random r = new Random();

    private LandingParty landingParty;

    private JGPoint shuttlePosition;

    private int globalX;

    private int globalY;

    public Planet(StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y) {
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
    }


    @Override
    public void enter(World world) {
        landingParty = world.getPlayer().getLandingParty();
        world.getCamera().setTarget(landingParty);
        shuttlePosition = new JGPoint(landingParty.getX(), landingParty.getY());
        updateVisibility(landingParty.getX(), landingParty.getY(), 5);
    }

    private int wrapX(int x) {
        if (x < 0) {
            return width + x;
        } else if (x >= width) {
            return x - width;
        }
        return x;
    }

    private int wrapY(int y) {
        if (y < 0) {
            return height + y;
        } else if (y >= height) {
            return y - height;
        }
        return y;
    }

    /**
     * Updates planet map. Makes tile visible in given range from given point
     *
     * @param x
     * @param y
     */
    private void updateVisibility(int x, int y, int range) {
        for (int i = y - range; i <= y + range; ++i) {
            for (int j = x - range; j <= x + range; ++j) {
                int pointX = wrapX(j);
                int pointY = wrapY(i);
                if (surface[pointY][pointX] < 0) {
                    surface[pointY][pointX] *= -1;
                }
            }
        }
    }

    @Override
    public void update(JGEngine engine, World world) {
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
        updateVisibility(x, y, 1);

        if (atmosphere != PlanetAtmosphere.BREATHABLE_ATMOSPHERE && world.isUpdatedThisFrame()) {
            world.getPlayer().getLandingParty().consumeOxygen();
        }

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
            }
        }
        world.getPlayer().getLandingParty().setPos(x, y);

        if (world.getPlayer().getLandingParty().getOxygen() < 0) {
            GameLogger.getInstance().logMessage("Lost connection with landing party");
            world.setCurrentRoom(owner);
            owner.enter(world);
            world.getPlayer().getShip().setPos(globalX, globalY);
            engine.clearKey(JGEngine.KeyEnter);
        }
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {

        GameLogger.getInstance().addStatusMessage("Planet info:");
        GameLogger.getInstance().addStatusMessage(String.format("Size: [%d, %d]", width, height));
        GameLogger.getInstance().addStatusMessage("Atmosphere: " + atmosphere);
        GameLogger.getInstance().addStatusMessage("=====================================");

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
        landingParty.draw(engine, camera);

        //todo: better wrapping of objects
        if (camera.isInViewport(shuttlePosition.x, shuttlePosition.y)) {
            engine.drawImage("shuttle", camera.getXCoord(shuttlePosition.x), camera.getYCoord(shuttlePosition.y));
        } else if (camera.isInViewport(shuttlePosition.x + width, shuttlePosition.y)) {
            engine.drawImage("shuttle", camera.getXCoord(shuttlePosition.x + width), camera.getYCoord(shuttlePosition.y));
        } else if (camera.isInViewport(shuttlePosition.x + width, shuttlePosition.y + height)) {
            engine.drawImage("shuttle", camera.getXCoord(shuttlePosition.x + width), camera.getYCoord(shuttlePosition.y + height));
        } else if (camera.isInViewport(shuttlePosition.x, shuttlePosition.y + height)) {
            engine.drawImage("shuttle", camera.getXCoord(shuttlePosition.x), camera.getYCoord(shuttlePosition.y + height));
        }

        if (landingParty.getX() == shuttlePosition.x && landingParty.getY() == shuttlePosition.y) {
            GameLogger.getInstance().addStatusMessage("Press <enter> to return to orbit");
        }
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
}

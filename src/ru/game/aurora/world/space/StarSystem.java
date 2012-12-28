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
import ru.game.aurora.player.Player;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.*;

import java.util.ArrayList;
import java.util.List;

public class StarSystem extends BaseSpaceRoom implements GalaxyMapObject {
    public static final Color[] possibleColors = {Color.red, Color.white, Color.yellow, new Color(122, 155, 243)};

    public static final int[] possibleSizes = {1, 2, 3, 4};

    public static class Star {
        // 1 is largest star, 4 is smallest
        public final int size;
        public final Color color;

        public Star(int size, Color color) {
            this.size = size;
            this.color = color;
        }
    }

    private Star star;

    private BasePlanet[] planets;

    private int globalMapX;

    private int globalMapY;

    private ParallaxBackground background;

    /**
     * Relation between tile size and max planet size
     * 3 means max planet will have radius of 3 tiles
     */
    public final static int PLANET_SCALE_FACTOR = 2;

    public final static int STAR_SCALE_FACTOR = 4;

    // size of star system. moving out of radius from the star initiates return to global map
    private int radius;

    private List<NPCShip> ships = new ArrayList<NPCShip>();

    /**
     * How many unexplored data for Astronomy research this star system contains
     */
    private int astronomyData;

    public StarSystem(Star star, int globalMapX, int globalMapY) {
        this.star = star;
        this.globalMapX = globalMapX;
        this.globalMapY = globalMapY;
    }

    public void setPlanets(BasePlanet[] planets) {
        this.planets = planets;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics g, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(tileX, tileY)) {
            return;
        }
        g.setColor(star.color);
        g.fillOval(camera.getXCoord(tileX) + camera.getTileWidth() / 2, camera.getYCoord(tileY) + camera.getTileHeight() / 2, camera.getTileWidth() / star.size, camera.getTileHeight() / star.size);
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(GameContainer container, Player player) {
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);

        int y = world.getPlayer().getShip().getY();
        int x = world.getPlayer().getShip().getX();

        if (container.getInput().isKeyDown(Input.KEY_E)) {
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
                if (container.getInput().isKeyDown(Input.KEY_ENTER)) {
                    GameLogger.getInstance().logMessage("Descending to surface...");

                    world.setCurrentRoom(p);

                    final LandingParty landingParty = world.getPlayer().getLandingParty();
                    if (landingParty == null || !landingParty.canBeLaunched(world)) {
                        // first landing, show 'Landing party equipment' screen
                        LandingPartyEquipScreen screen = new LandingPartyEquipScreen(true);
                        screen.enter(world);
                        world.setCurrentRoom(screen);
                    } else {
                        p.enter(world);
                    }
                    break;
                } else if (container.getInput().isKeyDown(Input.KEY_S)) {
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


        for (NPCShip ship : ships) {
            if (ship.getX() == x && ship.getY() == y) {
                if (!ship.isHostile() && container.getInput().isKeyDown(Input.KEY_ENTER)) {
                    world.setCurrentDialog(ship.getRace().getDefaultDialog());
                }
            }
            if (world.isUpdatedThisFrame()) {
                ship.update(container, world);
            }
        }

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
            background = new ParallaxBackground(
                    radius * 3 * world.getCamera().getTileWidth()
                    , radius * 3 * world.getCamera().getTileHeight()
                    , 0//planets.length * world.getCamera().getTileWidth()
                    , 0//planets.length * world.getCamera().getTileHeight()
                    , planets.length);
        }
        world.onPlayerEnteredSystem(this);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        background.draw(g, camera);
        player.getShip().draw(container, g, camera);
        player.addGlobalStatus();
        GameLogger.getInstance().addStatusMessage("==========================");
        g.setColor(star.color);

        final int starX = camera.getXCoord(0) + (camera.getTileWidth() / 2);
        final int starY = camera.getYCoord(0) + camera.getTileHeight() / 2;
        if (camera.isInViewport(0, 0)) {
            g.fillOval(starX, starY, camera.getTileWidth() * STAR_SCALE_FACTOR / star.size, camera.getTileHeight() * STAR_SCALE_FACTOR / star.size);
        }

        for (BasePlanet p : planets) {
            if (p.getGlobalX() == player.getShip().getX() && p.getGlobalY() == player.getShip().getY()) {
                GameLogger.getInstance().addStatusMessage("Approaching planet: ");
                GameLogger.getInstance().addStatusMessage("Press <S> to scan");
                GameLogger.getInstance().addStatusMessage("Press <enter> to launch surface party");
            }

            int planetX = camera.getXCoord(p.getGlobalX()) + (camera.getTileWidth() / 2);
            int planetY = camera.getYCoord(p.getGlobalY()) + camera.getTileWidth() / 2;
            float size = (float) (Math.sqrt(Math.pow((planetX - starX), 2) + Math.pow((planetY - starY), 2)) * 2);

            g.setColor(Color.gray);
            g.drawOval(starX, starY, size, size);
            p.drawOnGlobalMap(container, g, camera, 0, 0);

        }
        for (NPCShip ship : ships) {
            ship.draw(container, g, camera);
            if (ship.getX() == player.getShip().getX() && ship.getY() == player.getShip().getY()) {
                if (!ship.isHostile()) {
                    GameLogger.getInstance().addStatusMessage("Press <enter> to hail ship");
                }
            }
        }

        g.setColor(Color.red);
        g.drawRect(camera.getXCoord(-radius), camera.getYCoord(-radius), 2 * radius * camera.getTileWidth(), 2 * radius * camera.getTileHeight());
    }

    public List<NPCShip> getShips() {
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
}

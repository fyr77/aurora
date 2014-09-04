package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.dungeon.IVictoryCondition;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * Internal tile map representation for planet surfaces, where each tile is assigned a byte value that contains encoded
 * information about this tile
 */
public class SurfaceTileMap implements ITileMap, Serializable {

    private static final long serialVersionUID = 1L;

    private final int width;

    private final int height;

    /**
     * Animals that are located on planet surface.
     */
    private final List<GameObject> planetObjects = new ArrayList<>();

    protected transient SoftReference<AStarPathFinder> pathfinder;

    /**
     * Tiles with planet surface.
     * Actual contents are encoded by bits
     * <p/>
     * vpm0tttt
     * <p/>
     * v - visibility bit, 1 means tile is not explored, 0 is for explored
     * p - bit shows if tile can be passed on foot (1)
     * m - mountains
     * 0 - reserved
     * tttt - tile type
     */
    private final byte[][] surface;

    public SurfaceTileMap(int width, int height, byte[][] surface) {
        this.height = height;
        this.surface = surface;
        this.width = width;
    }

    public SurfaceTileMap(SurfaceTileMap other) {
        this.height = other.height;
        this.width = other.width;
        this.surface = new byte[height][width];
        for (int i = 0; i < height; ++i) {
            System.arraycopy(other.surface[i], 0, surface[i], 0, width);
        }
    }

    private static final TileDrawer mountainDrawer = new TileDrawer("mountains", (byte) 0);

    private static final Map<Byte, TileDrawer> drawers = new HashMap<>();

    static {
        drawers.put(SurfaceTypes.ROCKS, new TileDrawer("rock", SurfaceTypes.ROCKS));
        drawers.put(SurfaceTypes.STONES, new TileDrawer("stones", SurfaceTypes.STONES));
        drawers.put(SurfaceTypes.DIRT, new TileDrawer("sand", SurfaceTypes.DIRT));
        drawers.put(SurfaceTypes.ICE, new TileDrawer("ice", SurfaceTypes.ICE));
        drawers.put(SurfaceTypes.SNOW, new TileDrawer("snow", SurfaceTypes.SNOW));
        drawers.put(SurfaceTypes.ASPHALT, new TileDrawer("asph", SurfaceTypes.ASPHALT));

    }

    @Override
    public List<GameObject> getObjects() {
        return planetObjects;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        SortedSet<Byte> neighbours = new TreeSet<>(); // sorted becase surface type byte defines surfaces draw order
        for (int i = camera.getTarget().getY() - camera.getPointTileY(camera.getViewportY()) - 1 - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() - camera.getPointTileY(camera.getViewportY()) + camera.getNumTilesY() / 2 + 1; ++i) {
            for (int j = camera.getTarget().getX() - camera.getPointTileX(camera.getViewportX()) - 1 - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() - camera.getPointTileX(camera.getViewportX()) + camera.getNumTilesX() / 2 + 1; ++j) {

                final byte type = surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)];
                if ((type & SurfaceTypes.VISIBILITY_MASK) == 0) {
                    continue;
                }

                SurfaceTypes.drawDetailed(
                        type
                        , camera.getXCoord(j)
                        , camera.getYCoord(i)
                        , camera.getTileWidth()
                        , camera.getTileHeight()
                        , graphics);

                // now draw edges of next sprites
                neighbours.clear();
                for (int ii = -1; ii <= 1; ++ii) {
                    for (int jj = -1; jj <= 1; ++jj) {
                        if (ii == jj && ii == 0) {
                            continue;
                        }

                        byte st = SurfaceTypes.getType(surface[EngineUtils.wrap(i + ii, height)][EngineUtils.wrap(j + jj, width)]);
                        if (st <= SurfaceTypes.getType(surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)])) {
                            continue;
                        }
                        neighbours.add(st);
                    }
                }

                for (Byte b : neighbours) {
                    TileDrawer td = drawers.get(b);
                    if (td != null) {
                        td.drawTile(graphics, camera, surface, i, j, width, height);
                    }
                }

            }
        }


        // after all draw mountains
        for (int i = camera.getTarget().getY() - camera.getPointTileY(camera.getViewportY()) - 1 - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() - camera.getPointTileY(camera.getViewportY()) + camera.getNumTilesY() / 2 + 1; ++i) {
            // first draw outer mountains (that have only one neighbour on X)
            for (int j = camera.getTarget().getX() - camera.getPointTileX(camera.getViewportX()) - 1 - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() - camera.getPointTileX(camera.getViewportX()) + camera.getNumTilesX() / 2 + 1; j++) {
                if ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.VISIBILITY_MASK) == 0) {
                    continue;
                }


                if ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("stones"), camera.getXCoord(j), camera.getYCoord(i));
                }
                if ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.MOUNTAINS_MASK) == 0) {
                    boolean left = ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j - 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean right = ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j + 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean up = ((surface[EngineUtils.wrap(i - 1, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean down = ((surface[EngineUtils.wrap(i + 1, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);

                    boolean downLeft = ((surface[EngineUtils.wrap(i + 1, height)][EngineUtils.wrap(j - 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean downRight = ((surface[EngineUtils.wrap(i + 1, height)][EngineUtils.wrap(j + 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean upLeft = ((surface[EngineUtils.wrap(i - 1, height)][EngineUtils.wrap(j - 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);
                    boolean upRight = ((surface[EngineUtils.wrap(i - 1, height)][EngineUtils.wrap(j + 1, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0);

                    mountainDrawer.drawTile(graphics, camera, i, j, left, right, up, down, downLeft, downRight, upLeft, upRight);
                }
                if (allNeighboursAreMountain(EngineUtils.wrap(j, width), EngineUtils.wrap(i + 1, height))) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("stones"), camera.getXCoord(j), camera.getYCoord(i));

                } else {
                    // draw 2nd floor
                    boolean left = allNeighboursAreMountain(j - 1, i + 1);
                    boolean right = allNeighboursAreMountain(j + 1, i + 1);
                    boolean up = allNeighboursAreMountain(j, i - 1 + 1);
                    boolean down = allNeighboursAreMountain(j, i + 1 + 1);

                    boolean downLeft = allNeighboursAreMountain(j - 1, i + 1 + 1);
                    boolean downRight = allNeighboursAreMountain(j + 1, i + 1 + 1);
                    boolean upLeft = allNeighboursAreMountain(j - 1, i - 1 + 1);
                    boolean upRight = allNeighboursAreMountain(j + 1, i - 1 + 1);

                    mountainDrawer.drawTile(graphics, camera, i, j, left, right, up, down, downLeft, downRight, upLeft, upRight);
                }
                TileDrawer.drawFoWEdges(graphics, camera, surface, i, j, width, height);
            }
        }
    }

    private boolean allNeighboursAreMountain(int x, int y) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i != 0 && j != 0) {
                    continue;
                }
                if ((!SurfaceTypes.isMountain(surface[EngineUtils.wrap(y + j, height)][EngineUtils.wrap(x + i, width)]))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int updateVisibility(int x, int y, int range) {
        int rz = 0;
        for (int i = y - range; i <= y + range; ++i) {
            for (int j = x - range; j <= x + range; ++j) {
                int pointX = EngineUtils.wrap(j, width);
                int pointY = EngineUtils.wrap(i, height);
                if (0 == (SurfaceTypes.VISIBILITY_MASK & surface[pointY][pointX])) {
                    surface[pointY][pointX] |= SurfaceTypes.VISIBILITY_MASK;
                    ++rz;
                }
            }
        }
        return rz;
    }

    @Override
    public void setTilePassable(int x, int y, boolean isPassable) {
        final int wrappedX = EngineUtils.wrap(x, width);
        final int wrappedY = EngineUtils.wrap(y, height);
        if (!isPassable) {
            surface[wrappedY][wrappedX] |= SurfaceTypes.OBSTACLE_MASK;
        } else {
            surface[wrappedY][wrappedX] &= ~SurfaceTypes.OBSTACLE_MASK;
        }
    }

    @Override
    public BasePositionable getEntryPoint() {
        return null;
    }

    @Override
    public Collection<BasePositionable> getExitPoints() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IVictoryCondition> getVictoryConditions() {
        return Collections.emptyList();
    }

    public void drawLandscapeMap(Graphics graphics, Camera camera) {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                SurfaceTypes.drawSimple(
                        surface[i][j]
                        , camera.getXCoord(j)
                        , camera.getYCoord(i)
                        , camera.getTileWidth()
                        , camera.getTileHeight()
                        , graphics);
            }
        }
    }

    @Override
    public boolean isTilePassable(int x, int y) {
        return SurfaceTypes.isPassible(getByteWrapped(x, y));
    }

    private byte getByteWrapped(int x, int y) {
        return surface[EngineUtils.wrap(y, height)][EngineUtils.wrap(x, width)];
    }

    @Override
    public boolean isTilePassable(LandingParty landingParty, int x, int y) {
        return isTilePassable(x, y);
    }

    @Override
    public boolean isTileVisible(int x, int y) {
        return (SurfaceTypes.VISIBILITY_MASK & getByteWrapped(x, y)) != 0;
    }

    @Override
    public boolean lineOfSightExists(int x1, int y1, int x2, int y2) {
        return true;
    }

    @Override
    public int getWidthInTiles() {
        return width;
    }

    @Override
    public int getHeightInTiles() {
        return height;
    }

    @Override
    public void pathFinderVisited(int i, int i2) {
    }

    @Override
    public boolean blocked(PathFindingContext pathFindingContext, int i, int i2) {
        return isTilePassable(EngineUtils.wrap(i, width), EngineUtils.wrap(i2, height));
    }

    @Override
    public float getCost(PathFindingContext pathFindingContext, int i, int i2) {
        return 1.0f;
    }

    @Override
    public boolean isWrapped() {
        return true;
    }

    public byte getTileAt(int x, int y) {
        return getByteWrapped(x, y);
    }

    @Override
    public boolean contains(int i, int i1) {
        return true;
    }

    @Override
    public boolean isObstacle(int i, int i1) {
        return isTilePassable(EngineUtils.wrap(i, width), EngineUtils.wrap(i1, height));
    }

    @Override
    public void visit(int i, int i1) {
        // not implemented, has custom view logic
    }

    public byte[][] getSurface() {
        return surface;
    }


    @Override
    public AStarPathFinder getPathFinder() {
        AStarPathFinder pf = pathfinder != null ? pathfinder.get() : null;
        if (pf == null) {
            pf = new AStarPathFinder(this, getWidthInTiles(), false);
            pathfinder = new SoftReference<AStarPathFinder>(pf);
        }
        return pf;
    }
}

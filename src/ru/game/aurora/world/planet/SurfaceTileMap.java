package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.ITileMap;

import java.io.Serializable;
import java.util.*;

/**
 * Internal tile map representation for planet surfaces, where each tile is assigned a byte value that contains encoded
 * information about this tile
 */
public class SurfaceTileMap implements ITileMap, Serializable {

    private static final long serialVersionUID = 1L;

    private int width;

    private int height;

    /**
     * Animals that are located on planet surface.
     */
    private List<PlanetObject> planetObjects = new ArrayList<>();


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
    private byte[][] surface;

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

    private static TileDrawer mountainDrawer = new TileDrawer("mountains", (byte) 0);

    private static Map<Byte, TileDrawer> drawers = new HashMap<>();

    static {
        drawers.put(SurfaceTypes.ROCKS, new TileDrawer("rock", SurfaceTypes.ROCKS));
        drawers.put(SurfaceTypes.STONES, new TileDrawer("stones", SurfaceTypes.STONES));
        drawers.put(SurfaceTypes.DIRT, new TileDrawer("sand", SurfaceTypes.DIRT));
        drawers.put(SurfaceTypes.ICE, new TileDrawer("ice", SurfaceTypes.ICE));
        drawers.put(SurfaceTypes.SNOW, new TileDrawer("snow", SurfaceTypes.SNOW));
    }

    @Override
    public List<PlanetObject> getObjects() {
        return planetObjects;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        for (int i = camera.getTarget().getY() - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() + camera.getNumTilesY() / 2; ++i) {
            for (int j = camera.getTarget().getX() - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() + camera.getNumTilesX() / 2; ++j) {

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
                Set<Byte> neighbours = new TreeSet<>();
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
        for (int i = camera.getTarget().getY() - camera.getNumTilesY() / 2; i <= camera.getTarget().getY() + camera.getNumTilesY() / 2; ++i) {
            // first draw outer mountains (that have only one neighbour on X)
            for (int j = camera.getTarget().getX() - camera.getNumTilesX() / 2; j <= camera.getTarget().getX() + camera.getNumTilesX() / 2; j++) {
                if ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.VISIBILITY_MASK) == 0) {
                    continue;
                }


                if ((surface[EngineUtils.wrap(i, height)][EngineUtils.wrap(j, width)] & SurfaceTypes.MOUNTAINS_MASK) != 0) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("rock"), camera.getXCoord(j), camera.getYCoord(i));
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
                    graphics.drawImage(ResourceManager.getInstance().getImage("rock"), camera.getXCoord(j), camera.getYCoord(i));

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
        if (!isPassable) {
            surface[y][x] |= SurfaceTypes.OBSTACLE_MASK;
        } else {
            surface[y][x] &= ~SurfaceTypes.OBSTACLE_MASK;
        }
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
        return SurfaceTypes.isPassible(surface[y][x]);
    }

    @Override
    public boolean isTilePassable(LandingParty landingParty, int x, int y) {
        return isTilePassable(x, y);
    }

    @Override
    public boolean isTileVisible(int x, int y) {
        return (SurfaceTypes.VISIBILITY_MASK & surface[y][x]) != 0;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public byte getTileAt(int x, int y) {
        return surface[y][x];
    }
}

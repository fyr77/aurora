/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.07.13
 * Time: 15:06
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;

import java.util.HashMap;
import java.util.Map;

// draws tiles with beautiful borders between
public class TileDrawer {
    private final String tileName;

    private Map<Integer, String> sprites = new HashMap<>();

    private byte mySurfaceType;

    public TileDrawer(String tileName, byte mySurfaceType) {
        this.tileName = tileName;
        this.mySurfaceType = mySurfaceType;

        sprites.put(31, tileName + "_4");
        sprites.put(11, tileName + "_5");
        sprites.put(107, tileName + "_6");
        sprites.put(127, tileName + "_7");
        sprites.put(22, tileName + "_8");
        sprites.put(214, tileName + "_9");
        sprites.put(223, tileName + "_10");
        sprites.put(251, tileName + "_11");
        sprites.put(248, tileName + "_12");
        sprites.put(208, tileName + "_13");
        sprites.put(254, tileName + "_14");
        sprites.put(104, tileName + "_15");
    }

    public void drawTile(Graphics graphics, Camera camera, int tileY, int tileX, boolean left, boolean right, boolean up, boolean down, boolean downLeft, boolean downRight, boolean upLeft, boolean upRight) {
        int number = 0;
        if (upLeft) {
            number |= 11;
        }
        if (up) {
            number |= 31;
        }

        if (upRight) {
            number |= 22;
        }

        if (left) {
            number |= 107;
        }

        if (right) {
            number |= 214;
        }

        if (downLeft) {
            number |= 104;
        }

        if (down) {
            number |= 248;
        }

        if (downRight) {
            number |= 208;
        }
        if (number == 0) {
            return;
        }
        String name = sprites.get(number);
        if (name != null) {
            graphics.drawImage(ResourceManager.getInstance().getImage(name), camera.getXCoord(tileX), camera.getYCoord(tileY));
        }
    }

    public void drawTile(Graphics graphics, Camera camera, byte[][] surface, int tileY, int tileX, int width, int height) {
        if (SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY, height)][EngineUtils.wrap(tileX, width)], mySurfaceType)) {
            return;
        }
        boolean left = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY, height)][EngineUtils.wrap(tileX - 1, width)], mySurfaceType);
        boolean right = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY, height)][EngineUtils.wrap(tileX + 1, width)], mySurfaceType);
        boolean up = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX, width)], mySurfaceType);
        boolean down = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX, width)], mySurfaceType);

        boolean downLeft = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX - 1, width)], mySurfaceType);
        boolean downRight = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX + 1, width)], mySurfaceType);
        boolean upLeft = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX - 1, width)], mySurfaceType);
        boolean upRight = SurfaceTypes.sameBaseSurfaceType(surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX + 1, width)], mySurfaceType);

        drawTile(graphics, camera, tileY, tileX, left, right, up, down, downLeft, downRight, upLeft, upRight);
    }

    public static void drawFoWEdges(Graphics graphics, Camera camera, byte[][] surface, int tileY, int tileX, int width, int height) {
        {
            boolean left = ((surface[EngineUtils.wrap(tileY, height)][EngineUtils.wrap(tileX - 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean right = ((surface[EngineUtils.wrap(tileY, height)][EngineUtils.wrap(tileX + 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean up = ((surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean down = ((surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            //corners
            boolean lu = ((surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX - 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean ld = ((surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX - 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean ru = ((surface[EngineUtils.wrap(tileY - 1, height)][EngineUtils.wrap(tileX + 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);
            boolean rd = ((surface[EngineUtils.wrap(tileY + 1, height)][EngineUtils.wrap(tileX + 1, width)] & SurfaceTypes.VISIBILITY_MASK) == 0);

            if (left) {
                if (up) {
                    if (down) {
                        if (right) {
                            graphics.drawImage(ResourceManager.getInstance().getImage("fow_c"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                        } else {
                            graphics.drawImage(ResourceManager.getInstance().getImage("fow_lud"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                        }
                    } else {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_lui"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    }
                } else if (down) {
                    if (right) {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_ldr"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    } else {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_ldi"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    }
                } else {
                    if (right) {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_right"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    }
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_left"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
            } else if (right) {
                if (up) {
                    if (down) {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_rud"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    } else {
                        graphics.drawImage(ResourceManager.getInstance().getImage("fow_rui"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                    }
                } else if (down) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_rdi"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                } else {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_right"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
            } else if (up) {
                if (down) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_down"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
                graphics.drawImage(ResourceManager.getInstance().getImage("fow_up"), camera.getXCoord(tileX), camera.getYCoord(tileY));
            } else if (down) {
                graphics.drawImage(ResourceManager.getInstance().getImage("fow_down"), camera.getXCoord(tileX), camera.getYCoord(tileY));
            } else {
                if (lu) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_lu"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
                if (ld) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_ld"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
                if (ru) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_ru"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
                if (rd) {
                    graphics.drawImage(ResourceManager.getInstance().getImage("fow_rd"), camera.getXCoord(tileX), camera.getYCoord(tileY));
                }
            }
        }
    }
}

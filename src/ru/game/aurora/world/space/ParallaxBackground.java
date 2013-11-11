package ru.game.aurora.world.space;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.12.12
 * Time: 15:07
 */

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParallaxBackground {
    public static final int PLANES_COUNT = 5;

    public static final int STARS_PER_PLANE = 100;

    public static class Star {
        public float x;
        public float y;
        public Color color;
        public int baseSize;
        public Image sprite;

        public Star(float x, float y, Color color, int baseSize) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.baseSize = baseSize;
        }
        public void setSprite(Image sprite) {
            this.sprite = sprite;
        }
    }

    /**
     * Real number of star per plane, depends on system size
     */
    private int starsPerPlane;

    private Star[][] stars;

    private static final Random r = new Random();

    private float baseWidth = 3;

    Image s0; //sprites for stars
    Image s1;
    Image s2;
    Image s3;

    public ParallaxBackground(float width, float height, float centerX, float centerY, int density) {
        s0 = ResourceManager.getInstance().getImage("stars_s0");
        s1 = ResourceManager.getInstance().getImage("stars_s1");
        s2 = ResourceManager.getInstance().getImage("stars_s2");
        s3 = ResourceManager.getInstance().getImage("stars_s3");
        if (density == 0) {
            density = 1;
        }
        starsPerPlane = STARS_PER_PLANE * density;
        stars = new Star[PLANES_COUNT][starsPerPlane];
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star newStar;
                Image star_image;
                Map<Color, Color> colorMap = new HashMap<>();

                newStar = new Star(r.nextFloat() * 2 * width - centerX - width, r.nextFloat() * 2 * height - centerY - height, CollectionUtils.selectRandomElement(StarSystem.possibleColors), r.nextInt((int) baseWidth) + 1);

                //select colors for star
                if (newStar.color == Color.red) {
                    colorMap.put(new Color(0, 113, 61, 255), new Color(95,29,18));
                    colorMap.put(new Color(134, 197, 86, 255), new Color(221,102,61));
                } else if (newStar.color == Color.white) {
                    colorMap.put(new Color(0, 113, 61, 255), new Color(56,56,56));
                    colorMap.put(new Color(134, 197, 86, 255), new Color(141,141,141));
                } else if (newStar.color == Color.yellow) {
                    colorMap.put(new Color(0, 113, 61, 255), new Color(113,91,0));
                    colorMap.put(new Color(134, 197, 86, 255), new Color(194,190,57));
                } else {
                    colorMap.put(new Color(0, 113, 61, 255), new Color(0,42,113));
                    colorMap.put(new Color(134, 197, 86, 255), new Color(86,128,197));
                }

                //select sprite for star
                switch (i) {
                    case 2:
                        star_image = EngineUtils.replaceColors(s1, colorMap);
                        break;
                    case 3:
                        star_image = EngineUtils.replaceColors(s2, colorMap);
                        break;
                    case 4:
                        star_image = EngineUtils.replaceColors(s3, colorMap);
                        break;
                    default:
                        star_image = EngineUtils.replaceColors(s0, colorMap);
                        break;
                }

                newStar.setSprite(star_image);
                stars[i][j] = newStar;
            }
        }
    }

    public float getXCoordPoint(Camera camera, int pointX, int planeNumber) {
        return pointX - ((camera.getTarget().getX() - camera.getViewportTilesX() / 2) * camera.getTileWidth() + camera.getTarget().getOffsetX()) / (planeNumber * 2 + 5);
    }

    public float getYCoordPoint(Camera camera, int pointY, int planeNumber) {
        return pointY - ((camera.getTarget().getY() - camera.getViewportTilesY() / 2) * camera.getTileHeight() + camera.getTarget().getOffsetY()) / (planeNumber * 2 + 5);
    }

    public void setBaseWidth(float baseWidth) {
        this.baseWidth = baseWidth;
    }

    public void draw(Graphics graphics, Camera camera) {
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star s = stars[i][j];
                int realX = (int) getXCoordPoint(camera, (int) s.x, i);
                int realY = (int) getYCoordPoint(camera, (int) s.y, i);
                graphics.drawImage(s.sprite,realX,realY);
            }
        }
    }
}

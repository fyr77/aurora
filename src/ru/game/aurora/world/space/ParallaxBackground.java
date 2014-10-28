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
        public final float y;
        public final Color color;
        public Image sprite;

        public Star(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
        public void setSprite(Image sprite) {
            this.sprite = sprite;
        }

        @Override
        public String toString() {
            return "Star{" +
                    "x=" + x +
                    ", y=" + y +
                    ", color=" + color +
                    '}';
        }
    }

    /**
     * Real number of star per plane, depends on system size
     */
    protected final int starsPerPlane;

    protected final Star[][] stars;

    private static final Random r = new Random();

    private static class StarCacheKey
    {
        int spriteIdx;

        Color baseColor;

        private StarCacheKey(int spriteIdx, Color baseColor) {
            this.spriteIdx = spriteIdx;
            this.baseColor = baseColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StarCacheKey that = (StarCacheKey) o;

            return spriteIdx == that.spriteIdx && !(baseColor != null ? !baseColor.equals(that.baseColor) : that.baseColor != null);
        }

        @Override
        public int hashCode() {
            int result = spriteIdx;
            result = 31 * result + (baseColor != null ? baseColor.hashCode() : 0);
            return result;
        }
    }

    private static Map<StarCacheKey, Image> sprites = new HashMap<>();

    private Image getImage(int spriteIdx, Color baseColor)
    {
        StarCacheKey key = new StarCacheKey(spriteIdx, baseColor);
        Image star_image = sprites.get(key);
        if (star_image != null) {
            return star_image;
        }

        Map<Color, Color> colorMap = new HashMap<>();
        //select colors for star
        if (baseColor == Color.red) {
            colorMap.put(new Color(0, 113, 61, 255), new Color(95,29,18));
            colorMap.put(new Color(134, 197, 86, 255), new Color(221,102,61));
        } else if (baseColor == Color.white) {
            colorMap.put(new Color(0, 113, 61, 255), new Color(56,56,56));
            colorMap.put(new Color(134, 197, 86, 255), new Color(141,141,141));
        } else if (baseColor == Color.yellow) {
            colorMap.put(new Color(0, 113, 61, 255), new Color(113,91,0));
            colorMap.put(new Color(134, 197, 86, 255), new Color(194,190,57));
        } else {
            colorMap.put(new Color(0, 113, 61, 255), new Color(0,42,113));
            colorMap.put(new Color(134, 197, 86, 255), new Color(86,128,197));
        }

        //select sprite for star
        switch (spriteIdx) {
            case 2:
                star_image = EngineUtils.replaceColors(ResourceManager.getInstance().getImage("stars_s1"), colorMap);
                break;
            case 3:
                star_image = EngineUtils.replaceColors(ResourceManager.getInstance().getImage("stars_s2"), colorMap);
                break;
            case 4:
                star_image = EngineUtils.replaceColors(ResourceManager.getInstance().getImage("stars_s3"), colorMap);
                break;
            default:
                star_image = EngineUtils.replaceColors(ResourceManager.getInstance().getImage("stars_s0"), colorMap);
                break;
        }

        sprites.put(key, star_image);
        return star_image;
    }

    public ParallaxBackground(float width, float height, float centerX, float centerY, int density) {
        if (density == 0) {
            density = 1;
        }
        starsPerPlane = STARS_PER_PLANE * density;
        stars = new Star[PLANES_COUNT][starsPerPlane];
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star newStar = new Star(r.nextFloat() * 2 * width - centerX - width, r.nextFloat() * 2 * height - centerY - height, CollectionUtils.selectRandomElement(StarSystem.possibleColors));
                Image star_image = getImage(i, newStar.color);
                newStar.setSprite(star_image);
                stars[i][j] = newStar;
            }
        }
    }

    public float getXCoordPoint(Camera camera, float pointX, int planeNumber) {
        return pointX - ((camera.getTarget().getX() - camera.getViewportTilesX() / 2) * camera.getTileWidth() + camera.getTarget().getOffsetX()) / (planeNumber * 2 + 5);
    }

    public float getYCoordPoint(Camera camera, float pointY, int planeNumber) {
        return pointY - ((camera.getTarget().getY() - camera.getViewportTilesY() / 2) * camera.getTileHeight() + camera.getTarget().getOffsetY()) / (planeNumber * 2 + 5);
    }

    public void draw(Graphics graphics, Camera camera) {
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star s = stars[i][j];
                float realX = getXCoordPoint(camera, s.x, i);
                float realY = getYCoordPoint(camera, s.y, i);
                if (camera.isInViewportScreen(realX, realY)) {
                    graphics.drawImage(s.sprite,realX,realY);
                }
            }
        }
    }
}

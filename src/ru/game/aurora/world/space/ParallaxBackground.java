package ru.game.aurora.world.space;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.12.12
 * Time: 15:07
 */

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.CollectionUtils;

import java.util.Random;

public class ParallaxBackground {
    public static final int PLANES_COUNT = 5;

    public static final int STARS_PER_PLANE = 100;

    public static class Star {
        public float x;
        public float y;
        public Color color;
        public int baseSize;

        public Star(float x, float y, Color color, int baseSize) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.baseSize = baseSize;
        }
    }

    /**
     * Real number of star per plane, depends on system size
     */
    private int starsPerPlane;

    private Star[][] stars;

    private static final Random r = new Random();

    private float baseWidth = 3;

    public ParallaxBackground(float width, float height, float centerX, float centerY, int density) {
        if (density == 0) {
            density = 1;
        }
        starsPerPlane = STARS_PER_PLANE * density;
        stars = new Star[PLANES_COUNT][starsPerPlane];
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                stars[i][j] = new Star(r.nextFloat() * 2 * width - centerX - width, r.nextFloat() * 2 * height - centerY - height, CollectionUtils.selectRandomElement(StarSystem.possibleColors), r.nextInt((int) baseWidth) + 1);
            }
        }
    }

    // same but for absolute coordinate (not tile)
    public float getXCoordPoint(Camera camera, int pointX, int planeNumber) {
        return (pointX - 1.0f / (planeNumber * 2 + 5) * (camera.getTarget().getX() - camera.getViewportTilesX() / 2) * camera.getTileWidth());
    }

    public float getYCoordPoint(Camera camera, int pointY, int planeNumber) {
        return (pointY - 1.0f / (planeNumber * 2 + 5) * (camera.getTarget().getY() - camera.getViewportTilesY() / 2) * camera.getTileHeight());
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

                graphics.setColor(s.color);

                float diameter = Math.max(1, 2 * (float) Math.ceil(s.baseSize / (2 * i + 1)));

                if (realX < -diameter || realY < -diameter || realX > camera.getTileWidth() * camera.getNumTilesX() || realY > camera.getTileHeight() * camera.getNumTilesY()) {
                    continue;
                }
                graphics.fillOval(realX, realY, diameter, diameter);
            }
        }
    }
}

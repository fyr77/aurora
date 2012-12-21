package ru.game.aurora.world.space;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.12.12
 * Time: 15:07
 */

import jgame.JGColor;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.CollectionUtils;

import java.util.Random;

public class ParallaxBackground {
    private static final int PLANES_COUNT = 3;

    private static final int STARS_PER_PLANE = 150;

    private static class Star {
        public int x;
        public int y;
        public JGColor color;

        private Star(int x, int y, JGColor color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    /**
     * Real number of star per plane, depends on system size
     */
    private int starsPerPlane;

    private Star[][] stars;

    private static final Random r = new Random();

    private float baseWidth = 6;

    public ParallaxBackground(int width, int height, int centerX, int centerY, int density) {
        if (density == 0) {
            density = 1;
        }
        starsPerPlane = STARS_PER_PLANE * density;
        stars = new Star[PLANES_COUNT][starsPerPlane];
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                stars[i][j] = new Star(r.nextInt(2 * width) - centerX - width, r.nextInt(2 * height) - centerY - height, CollectionUtils.selectRandomElement(StarSystem.possibleColors));
            }
        }
    }

    // same but for absolute coordinate (not tile)
    public double getXCoordPoint(Camera camera, int pointX, int planeNumber) {
        return (pointX - 1.0 / (planeNumber * 2 + 5) * (camera.getTarget().getX() - camera.getViewportTilesX() / 2) * camera.getTileWidth());
    }

    public double getYCoordPoint(Camera camera, int pointY, int planeNumber) {
        return (pointY - 1.0 / (planeNumber * 2 + 5) * (camera.getTarget().getY() - camera.getViewportTilesY() / 2) * camera.getTileHeight());
    }

    public void draw(JGEngine engine, Camera camera) {
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star s = stars[i][j];
                engine.setColor(s.color);

                double radius = Math.ceil(baseWidth / (2 * i + 1));
                if (radius < 3.0) {
                    // jgame does not draw oval with radius smaller than 3
                    radius = 3.0;
                }
                engine.drawOval(getXCoordPoint(camera, s.x, i), getYCoordPoint(camera, s.y, i), radius, radius, true, true);
            }
        }
    }

    public void setBaseWidth(float baseWidth) {
        this.baseWidth = baseWidth;
    }
}

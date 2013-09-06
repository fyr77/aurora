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
    private static final int PLANES_COUNT = 3;

    private static final int STARS_PER_PLANE = 150;

    private static class Star {
        public int x;
        public int y;
        public Color color;

        private Star(float x, float y, Color color) {
            this.x = (int) x;
            this.y = (int) y;
            this.color = color;
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
                stars[i][j] = new Star(r.nextFloat() * 2 * width - centerX - width, r.nextFloat() * 2 * height - centerY - height, CollectionUtils.selectRandomElement(StarSystem.possibleColors));
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
                int realX = (int) getXCoordPoint(camera, s.x, i);
                int realY = (int) getYCoordPoint(camera, s.y, i);

                graphics.setColor(s.color);

                float diameter = 2 * (float) Math.ceil(baseWidth / (2 * i + 1));
             /*   if (diameter < 4.0f) {
                    // jgame does not draw oval with diameter smaller than 3
                    diameter = 4.0f;
                }*/

                if (realX < -diameter || realY < -diameter || realX > camera.getTileWidth() * camera.getNumTilesX() || realY > camera.getTileHeight() * camera.getNumTilesY()) {
                    continue;
                }
                graphics.fillOval(realX, realY, diameter, diameter);
            }
        }
    }
}

package ru.game.aurora.world.generation.quest.asteroidbelt;

import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.space.ParallaxBackground;

class EncounterParallaxBackground extends ParallaxBackground {

    public EncounterParallaxBackground(float width, float height, float centerX, float centerY, int density) {
        super(width, height, centerX, centerY, density);
    }

    @Override
    public void draw(Graphics graphics, Camera camera) {
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                Star s = stars[i][j];
                float realX = getXCoordPoint(camera, s.x, i) - camera.getViewportX();
                float realY = getYCoordPoint(camera, s.y, i) - camera.getViewportY();

                if (camera.isInViewportScreen(realX, realY)) {
                    graphics.drawImage(s.sprite, realX, realY);
                }
            }
        }
    }
}
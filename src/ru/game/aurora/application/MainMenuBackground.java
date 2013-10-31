package ru.game.aurora.application;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.space.ParallaxBackground;
import ru.game.aurora.world.space.StarSystem;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 30.10.13
 * Time: 23:25
 */
public class MainMenuBackground {

    private ParallaxBackground.Star[][] stars;

    private long elapsed;

    private int width;

    public MainMenuBackground(int width, int height) {
        this.width = width;
        stars = new ParallaxBackground.Star[ParallaxBackground.PLANES_COUNT][ParallaxBackground.STARS_PER_PLANE];
        final Random random = CommonRandom.getRandom();

        for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
            for (int j = 0; j < ParallaxBackground.STARS_PER_PLANE; ++j) {
                stars[i][j] = new ParallaxBackground.Star(random.nextInt(width), random.nextInt(height), CollectionUtils.selectRandomElement(StarSystem.possibleColors), random.nextInt(3) + 1);
            }
        }
    }

    public void update(GameContainer container) {
        elapsed += (container.getTime() - AuroraGame.getLastFrameTime());
        if (elapsed >= 200) {
            for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
                for (int j = 0; j < ParallaxBackground.STARS_PER_PLANE; ++j) {
                    stars[i][j].x -= 1.0 / (i + 1);
                    if (stars[i][j].x < -5) {
                        stars[i][j].x = width + 5;
                    }
                }
            }

        }
    }

    public void draw(Graphics graphics) {
        for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
            for (int j = 0; j < ParallaxBackground.STARS_PER_PLANE; ++j) {
                final ParallaxBackground.Star s = stars[i][j];
                graphics.setColor(s.color);
                float diameter = Math.max(1, 2 * (float) Math.ceil(s.baseSize / (2 * i + 1)));
                graphics.fillOval(s.x, s.y, diameter, diameter);
            }
        }
    }
}

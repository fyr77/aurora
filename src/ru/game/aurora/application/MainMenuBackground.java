package ru.game.aurora.application;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.world.space.ParallaxBackground;
/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 30.10.13
 * Time: 23:25
 */
public class MainMenuBackground extends ParallaxBackground
{

    private static final int STARS_PER_PLANE = 30;

    private long elapsed;

    private int width;

    public MainMenuBackground(int width, int height) {
        super(width, height, 0, 0, 8);
        for (int i = 0; i < PLANES_COUNT; ++i) {
            for (int j = 0; j < starsPerPlane; ++j) {
                stars[i][j].x += width + AuroraGame.tileSize;
            }
        }
        this.width = width;
    }

    public void update(GameContainer container) {
        elapsed += (container.getTime() - AuroraGame.getLastFrameTime());
        if (elapsed >= 200) {
            for (int i = 0; i < PLANES_COUNT; ++i) {
                for (int j = 0; j < starsPerPlane; ++j) {
                    stars[i][j].x -= 1.0 / (i + 1);
                    if (stars[i][j].x < -AuroraGame.tileSize) {
                        stars[i][j].x = width + AuroraGame.tileSize;
                    }
                }
            }
        }
    }

    public void draw(Graphics graphics) {
        for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
            for (int j = 0; j < STARS_PER_PLANE; ++j) {
                final ParallaxBackground.Star s = stars[i][j];
                graphics.drawImage(s.sprite,s.x,s.y);
            }
        }
    }
}

package ru.game.aurora.application;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.ParallaxBackground;
import ru.game.aurora.world.space.StarSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 30.10.13
 * Time: 23:25
 */
public class MainMenuBackground {

    private ParallaxBackground.Star[][] stars;

    private static final int STARS_PER_PLANE = 30;

    private long elapsed;

    private int width;

    Image s0; //sprites for stars
    Image s1;
    Image s2;
    Image s3;

    public MainMenuBackground(int width, int height) {
        s0 = ResourceManager.getInstance().getImage("stars_s0");
        s1 = ResourceManager.getInstance().getImage("stars_s1");
        s2 = ResourceManager.getInstance().getImage("stars_s2");
        s3 = ResourceManager.getInstance().getImage("stars_s3");
        this.width = width;
        stars = new ParallaxBackground.Star[ParallaxBackground.PLANES_COUNT][ParallaxBackground.STARS_PER_PLANE];
        final Random random = CommonRandom.getRandom();

        for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
            for (int j = 0; j < STARS_PER_PLANE; ++j) {
                ParallaxBackground.Star newStar;
                Image star_image;
                Map<Color, Color> colorMap = new HashMap<>();

                newStar = new ParallaxBackground.Star(random.nextInt(width), random.nextInt(height), CollectionUtils.selectRandomElement(StarSystem.possibleColors), random.nextInt(3) + 1);

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
                        star_image = EngineUtils.replaceColors(s2, colorMap);
                        break;
                    case 3:
                        star_image = EngineUtils.replaceColors(s1, colorMap);
                        break;
                    case 4:
                        star_image = EngineUtils.replaceColors(s0, colorMap);
                        break;
                    default:
                        star_image = EngineUtils.replaceColors(s3, colorMap);
                        break;
                }

                newStar.setSprite(star_image);
                stars[i][j] = newStar;
            }
        }
    }

    public void update(GameContainer container) {
        elapsed += (container.getTime() - AuroraGame.getLastFrameTime());
        if (elapsed >= 200) {
            for (int i = 0; i < ParallaxBackground.PLANES_COUNT; ++i) {
                for (int j = 0; j < STARS_PER_PLANE; ++j) {
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
            for (int j = 0; j < STARS_PER_PLANE; ++j) {
                final ParallaxBackground.Star s = stars[i][j];
                graphics.drawImage(s.sprite,s.x,s.y);
            }
        }
    }
}

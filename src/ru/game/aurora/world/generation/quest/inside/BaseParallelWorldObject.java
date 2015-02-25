package ru.game.aurora.world.generation.quest.inside;

import org.newdawn.slick.*;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.SpaceDebris;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
class Circle implements Serializable {
    private static final long serialVersionUID = -4251577067050699176L;

    int offsetX;
    int offsetY;
    int radiusMin;
    int radiusMax;
    int radius;
    int speed = 1;
    boolean collapsing;

    Color color;
    Color outerColor;

    Circle(Color color, int offsetX, int offsetY, int radiusMin, int radiusMax) {
        this.color = color;
        this.outerColor = color.darker();
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.collapsing = false;
        this.radiusMin = radiusMin;
        this.radiusMax = radiusMax;
        speed = CommonRandom.getRandom().nextInt(2) + 1;
        if (CommonRandom.getRandom().nextBoolean()) {
            radius = radiusMin;
        } else {
            radius = radiusMax;
            speed *= -1;
        }
    }

    public void collapse() {
        speed = Math.abs(speed * 3) * -1;
        collapsing = true;
    }

    public void update() {
        radius += speed;
        if (collapsing && radius <= 0) {
            speed = 0;
            return;
        }
        if (radius >= radiusMax || (radius < radiusMin && !collapsing)) {
            speed *= -1;
        }
    }

    public void draw(float x, float y, Graphics g, Camera camera) {
        final float centerX = x + offsetX - radius / 2;
        final float centerY = y - radius / 2 + offsetY;
        if (camera != null && !camera.isInViewportScreen(centerX, centerY)) {
            return;
        }
        g.setColor(color);
        g.fillOval(centerX, centerY, radius - 1, radius - 1);
        g.setLineWidth(3);
        g.setColor(outerColor);
        g.drawOval(centerX, centerY, radius, radius);
        g.setLineWidth(1);

    }
}

public class BaseParallelWorldObject extends BaseGameObject {
    private static final long serialVersionUID = -2425758032579005221L;

    private List<Circle> circles;

    public BaseParallelWorldObject(Color c, int sizeInTiles) {
        this.name = "<unknown>";
        this.circles = new ArrayList<>();
        for (int i = 0; i < 3 + CommonRandom.getRandom().nextInt(3 * sizeInTiles); ++i) {
            final int radiusMin = CommonRandom.getRandom().nextInt((int) (0.5f * sizeInTiles * AuroraGame.tileSize)) + 5;
            circles.add(new Circle(
                    c
                    , (sizeInTiles * CommonRandom.getRandom().nextInt(AuroraGame.tileSize) - AuroraGame.tileSize * sizeInTiles) / 2
                    , sizeInTiles * (CommonRandom.getRandom().nextInt(AuroraGame.tileSize) - AuroraGame.tileSize) / 2
                    , radiusMin
                    , radiusMin + CommonRandom.getRandom().nextInt((int) (0.5f * sizeInTiles * AuroraGame.tileSize)) + 5
            ));
        }
    }

    @Override
    public boolean canBeAttacked() {
        return true;
    }

    @Override
    public Image getImage() {
        try {
            Image image = new Image(3 * AuroraGame.tileSize, 3 * AuroraGame.tileSize);
            for (Circle c : circles) {
                c.draw(image.getWidth() / 2, image.getHeight() / 2, image.getGraphics(), null);
            }
            return image;

        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAttack(World world, GameObject attacker, int damaged) {
        for (Circle c : circles) {
            if (damaged-- <= 0) {
                return;
            }
            c.collapse();
        }
        if (circles.size() <= damaged) {
            if (CommonRandom.getRandom().nextBoolean()) {
                world.getCurrentStarSystem().getObjects().add(new SpaceDebris(x, y, "bio_remains", new InsideEncounterGenerator.BioCellsItem()));
            }
        }
        super.onAttack(world, attacker, damaged);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        for (Circle c : circles) {
            c.draw(camera.getXCoord(x) + getOffsetX() + AuroraGame.tileSize
                    , camera.getYCoord(y) + getOffsetY() + AuroraGame.tileSize
                    , g
                    , camera);
        }
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        for (Iterator<Circle> iterator = circles.iterator(); iterator.hasNext(); ) {
            Circle c = iterator.next();
            c.update();
            if (c.speed == 0) {
                iterator.remove();
            }
        }
        if (circles.isEmpty()) {
            isAlive = false;
        }
    }
}

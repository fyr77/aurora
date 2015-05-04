package ru.game.aurora.effects;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.World;

/**
 * Falls from the bottom of screen to a given screen coordinate
 */
public class FallingMeteor extends Effect {

    protected final Vector2f currentPos;

    private final Vector2f movementDir;

    private final Vector2f target;

    public FallingMeteor(int targetScreenX, int targetScreenY) {
        super(CommonRandom.getRandom().nextInt(AuroraGame.tilesX * AuroraGame.tileSize), 0, HIGH_PRIORITY, DrawOrder.FRONT);
        this.drawable = new Drawable("meteor", true);
        this.currentPos = new Vector2f(x, y);
        this.target = new Vector2f(targetScreenX, targetScreenY);


        movementDir = new Vector2f(this.target.getX() - this.currentPos.getX(), this.target.getY() - this.currentPos.getY());
        float movementSpeed = movementDir.length() / 180; // should fly in approx 3 seconds, 180 frames
        movementDir.normalise();
        movementDir.scale(movementSpeed);
    }


    @Override
    public boolean isOver() {
        return isAlive;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        drawable.draw(g, currentPos.getX(), currentPos.getY());
    }

    @Override
    public void update(GameContainer container, World world) {
        currentPos.add(movementDir);
        if (currentPos.distance(target) < AuroraGame.tileSize) {
            isAlive = false;
        }
    }
}

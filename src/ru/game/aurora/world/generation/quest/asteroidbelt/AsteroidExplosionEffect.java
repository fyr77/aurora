package ru.game.aurora.world.generation.quest.asteroidbelt;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.world.World;

class AsteroidExplosionEffect extends ExplosionEffect {

    private static final long serialVersionUID = 8585466191348519688L;

    public AsteroidExplosionEffect(int x, int y, String animName) {
        super(x, y, animName, true, true);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        graphics.drawAnimation(anim, getX() - camera.getViewportX(), getY());
    }
}

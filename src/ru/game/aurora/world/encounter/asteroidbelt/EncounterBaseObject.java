package ru.game.aurora.world.encounter.asteroidbelt;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.World;

class EncounterBaseObject extends BaseGameObject {

    private static final long serialVersionUID = 3609917899526392712L;

    private double colliderRadius;

    public EncounterBaseObject(int x, int y, Drawable drawable){
        super(x, y, drawable);
        colliderRadius = getImage().getHeight()/2;
    }

    public double getColliderRadius(){
        return colliderRadius;
    }

    public void scaleCollider(double value){
        colliderRadius *= value;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        drawable.draw(g, getX() - camera.getViewportX(), getY(), false);
    }

    public boolean collision(EncounterBaseObject obj){
        return Math.pow(obj.getX() - getX(), 2) + Math.pow(obj.getY() - getY(), 2) <= Math.pow(obj.colliderRadius + colliderRadius, 2);
    }

    public void updateMovement() {}
}
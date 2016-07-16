package ru.game.aurora.world.generation.quest.asteroidbelt;

import ru.game.aurora.common.Drawable;

class ShootEffect extends EncounterBaseObject {

    private static final long serialVersionUID = 843835642414274689L;

    private int speedX;

    public ShootEffect(int speedX) {
        super(0, 0, new Drawable("laser_beam"));
        this.speedX = speedX;
    }

    @Override
    public boolean collision(EncounterBaseObject obj) {
        if(obj instanceof Asteroid) {
            // line segment vs circle collision check
            double x1 = getX();
            double y1 = getY();
            double x2 = x1;
            double y2 = y1 + getImage().getWidth();
            double xC = obj.getX() + obj.getImage().getWidth()/2;
            double yC = obj.getY() + obj.getImage().getHeight()/2;
            double r = obj.getColliderRadius();

            x1 -= xC;
            y1 -= yC;
            x2 -= xC;
            y2 -= yC;

            double dx = x2 - x1;
            double dy = y2 - y1;

            double a = dx*dx + dy*dy;
            double b = 2.0 * (x1*dx + y1*dy);
            double c = x1*x1 + y1*y1 - r*r;

            if (-b < 0.0) {
                return (c < 0.0);
            }

            if (-b < (2.0*a)) {
                return ((4.0 * a * c - b * b) < 0.0);
            }

            return (a + b + c < 0);
        }
        else{
            return false;
        }
    }

    @Override
    public void updateMovement() {
        setPos(getX() + speedX, getY());
    }
}

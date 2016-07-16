package ru.game.aurora.world.generation.quest.asteroidbelt;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.common.Drawable;

class Asteroid extends EncounterBaseObject {

    private static final long serialVersionUID = -117755417568772247L;
    private final int speedMax;
    private final int speedMin;

    private int speedX;
    private int speedY;

    public Asteroid(String textureId){
        super(0, 0, new Drawable(textureId));
        speedMax = Configuration.getIntProperty("encounter.asteroid_belt.asteroid_max_speed");
        speedMin = Configuration.getIntProperty("encounter.asteroid_belt.asteroid_min_speed");
    }

    public void resetParams(int startX, int screenWidth, int screenMinHeight, int screenMaxHeight) {
        speedX = CommonRandom.nextInt(speedMin, speedMax);

        if(CommonRandom.getRandom().nextFloat() < 0.3f) { // 30% to set diagonal trajectory
            // diagonal trajectory
            int x = CommonRandom.nextInt(screenWidth / 4, screenWidth + screenWidth / 4);

            if(CommonRandom.getRandom().nextBoolean()) {
                // negative diagonal trajectory
                speedY = -speedX;
                int y = screenMaxHeight + screenMaxHeight/8;
                super.setPos(startX + x, y);
            }
            else{
                // positive diagonal trajectory
                speedY = speedX;
                int y = screenMinHeight - screenMaxHeight/8;
                super.setPos(startX + x, y);
            }
        }
        else{
            // horizontal trajectory
            int y = CommonRandom.nextInt(screenMinHeight, screenMaxHeight);
            super.setPos(startX + screenWidth, y);
            speedY = 0;
        }
    }

    @Override
    public void updateMovement() {
        setPos(getX() - speedX, getY() + speedY);
    }
}

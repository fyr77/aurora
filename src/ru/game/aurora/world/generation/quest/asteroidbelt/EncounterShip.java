package ru.game.aurora.world.generation.quest.asteroidbelt;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.common.Drawable;

class EncounterShip extends EncounterBaseObject {

    private static final long serialVersionUID = 5667960822191899117L;

    private long timeLastShoot;
    private long timeShootCooldown;

    public EncounterShip() {
        super(0, 0, new Drawable("aurora"));
        timeShootCooldown = Configuration.getIntProperty("encounter.asteroid_belt.shoot_cooldown_ms");
    }

    public boolean readyToShoot(GameContainer container){
        if(timeLastShoot + timeShootCooldown < container.getTime()){
            return true;
        }
        else{
            return false;
        }
    }

    public void shootSetCooldown(GameContainer container){
        timeLastShoot = container.getTime();
    }
}

package ru.game.aurora.world.space.ships;

import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Created by di Grigio on 28.03.2017.
 */
public class ShipDesc implements JsonConfigManager.EntityWithId, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String defaultName;
    private final String imageId;
    private final String race;

    private final int maxHp;
    private final float dodgeChance;

    private final boolean isStationary;

    public ShipDesc(String id, String defaultName, String imageId, String race, int maxHp, int dodgeChance, boolean isStationary){
        this.id = id;
        this.defaultName = defaultName;
        this.imageId = imageId;
        this.race = race;

        this.maxHp = maxHp;
        this.dodgeChance = dodgeChance;

        this.isStationary = isStationary;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public AlienRace getRace(){
        if(!World.getWorld().getFactions().containsKey(race)){
            return null;
        }
        else{
            return (AlienRace)World.getWorld().getFactions().get(race);
        }
    }

    public Drawable getDrawable() {
        return new Drawable(imageId);
    }

    public int getMaxHp(){
        return maxHp;
    }

    public float getDodgeChance(){
        return dodgeChance;
    }

    public boolean isStationary() { return isStationary; }

    @Override
    public String getCustomClass() {
        return null;
    }
}

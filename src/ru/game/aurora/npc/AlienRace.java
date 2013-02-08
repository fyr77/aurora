/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;

public class AlienRace implements Serializable {

    private static final long serialVersionUID = 5107718114248088759L;

    private String name;

    /**
     * 0-3 - hate: will attack on sight
     * 4-6 - dislike: will not attack, but also will not communicate
     * 7-9 - neutral: will communicate, can occasionally help
     * 10-12 - like: will help and easily share information
     */
    private int relationToPlayer;

    private StarSystem homeworld;

    /**
     * Default dialog is used when hailing random encounter ship of this race
     */
    private Dialog defaultDialog;

    private String shipSprite;

    public AlienRace(String name, String shipSprite, int relationToPlayer, Dialog defaultDialog) {
        this.name = name;
        this.relationToPlayer = relationToPlayer;
        this.shipSprite = shipSprite;
        this.homeworld = homeworld;
        this.defaultDialog = defaultDialog;
    }

    public NPCShip createRandomShip() {
        NPCShip ship = new NPCShip(0, 0, getShipSprite(), this, null, getName() + " ship");
        ship.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"), StarshipWeapon.MOUNT_ALL));
        return ship;
    }


    public Dialog getDefaultDialog() {
        return defaultDialog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRelationToPlayer() {
        return relationToPlayer;
    }

    public void setRelationToPlayer(int relationToPlayer) {
        this.relationToPlayer = relationToPlayer;
    }

    public StarSystem getHomeworld() {
        return homeworld;
    }

    /**
     * How many tiles away from main homeworld system player can meet ships of this race
     */
    public int getTravelDistance() {
        return 5;
    }

    public void setHomeworld(StarSystem homeworld) {
        this.homeworld = homeworld;
    }

    public String getShipSprite() {
        return shipSprite;
    }

    public boolean isHostileTo(AlienRace other) {
        return false;
    }

    public boolean isHostileToPlayer() {
        return getRelationToPlayer() <= 3;
    }
}

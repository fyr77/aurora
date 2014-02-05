/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;

public class AlienRace implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;


    private StarSystem homeworld;

    private int travelDistance = 5;

    /**
     * Default dialog is used when hailing random encounter ship of this race
     */
    private Dialog defaultDialog;

    private String shipSprite;

    private NPCShipFactory defaultFactory;

    public AlienRace(String name, String shipSprite, Dialog defaultDialog) {
        this.name = name;
        this.shipSprite = shipSprite;
        this.defaultDialog = defaultDialog;
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


    public NPCShipFactory getDefaultFactory() {
        return defaultFactory;
    }

    public StarSystem getHomeworld() {
        return homeworld;
    }

    /**
     * How many tiles away from main homeworld system player can meet ships of this race
     */
    public int getTravelDistance() {
        return travelDistance;
    }

    public void setTravelDistance(int travelDistance) {
        this.travelDistance = travelDistance;
    }

    public void setHomeworld(StarSystem homeworld) {
        this.homeworld = homeworld;
    }

    public String getShipSprite() {
        return shipSprite;
    }

    public void setDefaultFactory(NPCShipFactory defaultFactory) {
        this.defaultFactory = defaultFactory;
    }

    public void setDefaultDialog(Dialog defaultDialog) {
        this.defaultDialog = defaultDialog;
    }
}

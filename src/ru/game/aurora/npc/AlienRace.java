/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.music.Playlist;
import ru.game.aurora.world.space.StarSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class AlienRace implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String name;


    private StarSystem homeworld;

    private int travelDistance = CommonRandom.getRandom().nextInt(55) + 30;

    /**
     * Default dialog is used when hailing random encounter ship of this race
     */
    private Dialog defaultDialog;

    private final String shipSprite;

    private NPCShipFactory defaultFactory;

    // set to true after first communication, if set to true - this race are is drawn on global map
    private boolean isKnown = false;

    // custom music that is played in dialogs
    private transient Playlist music;

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

    public boolean isKnown() {
        return isKnown;
    }

    public void setKnown(boolean known) {
        isKnown = known;
    }

    public Playlist getMusic() {
        return music;
    }

    public void setMusic(Playlist music) {
        this.music = music;
    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        music = ResourceManager.getInstance().getPlaylist(getName());

    }
}

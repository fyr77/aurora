/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AlienRace implements Serializable {

    private static final long serialVersionUID = 5107718114248088759L;

    private String name;


    private StarSystem homeworld;

    private int travelDistance = 5;

    /**
     * Default dialog is used when hailing random encounter ship of this race
     */
    private Dialog defaultDialog;

    private String shipSprite;

    /**
     * Relation with other races. If a race is not present in this mapping then relation is neutral
     * 0-3 - hate: will attack on sight
     * 4-6 - dislike: will not attack, but also will not communicate
     * 7-9 - neutral: will communicate, can occasionally help
     * 10-12 - like: will help and easily share information
     */
    private Map<String, Integer> relations = new HashMap<>();

    private NPCShipFactory defaultFactory = new NPCShipFactory() {

        private static final long serialVersionUID = -6223078452316173728L;

        @Override
        public NPCShip createShip() {
            NPCShip ship = new NPCShip(0, 0, getShipSprite(), AlienRace.this, null, getName() + " ship");
            ship.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"), StarshipWeapon.MOUNT_ALL));
            return ship;
        }
    };

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


    public void setRelation(AlienRace race, int value) {
        if (race.getName().equals("Humanity")) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.relation_changed"), name, getRelation(race), value));
        }
        relations.put(race.getName(), value);
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

    public int getRelation(AlienRace race) {
        if (race == null) {
            return 5;
        }
        if (race == this) {
            return 10;
        }
        Integer i = relations.get(race.getName());
        if (i == null) {
            return 5;
        }
        return i;
    }
}

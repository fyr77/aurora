/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:55
 */
package ru.game.aurora.player.earth;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.engineering.upgrades.*;
import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.*;

public class EarthState implements Serializable
{
    private static final long serialVersionUID = 3L;

    private final List<PrivateMessage> messages = new LinkedList<>();

    private final Set<ShipUpgrade> availableUpgrades = new HashSet<>();

    private int technologyLevel = 0;

    private EvacuationState evacuationState;

    // quest dialogs that override default earth dialog
    // ordered in a queue, as there may be more than one at a time
    private final Queue<Dialog> earthSpecialDialogs = new LinkedList<>();

    public EarthState()
    {
        availableUpgrades.add(new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon")));
        availableUpgrades.add(new LabUpgrade());
        availableUpgrades.add(new WorkshopUpgrade());
        availableUpgrades.add(new BarracksUpgrade());
        availableUpgrades.add(new AstroDroneUpgrade());
    }

    public void updateTechnologyLevel(int value) {
        technologyLevel += value;
    }

    public List<PrivateMessage> getMessages() {
        return messages;
    }

    public Queue<Dialog> getEarthSpecialDialogs() {
        return earthSpecialDialogs;
    }

    public EvacuationState getEvacuationState() {
        return evacuationState;
    }

    public void setEvacuationState(EvacuationState evacuationState) {
        this.evacuationState = evacuationState;
    }

    public void update(World world) {
        if (evacuationState != null) {
            evacuationState.update(world);
        }
    }

    public Set<ShipUpgrade> getAvailableUpgrades() {
        return availableUpgrades;
    }

    public int getTechnologyLevel() {
        return technologyLevel;
    }
}

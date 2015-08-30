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
    private static final long serialVersionUID = 4L;

    private final List<PrivateMessage> messages = new LinkedList<>();

    private final Set<ShipUpgrade> availableUpgrades = new HashSet<>();
    // quest dialogs that override default earth dialog
    // ordered in a queue, as there may be more than one at a time
    private final Queue<Dialog> earthSpecialDialogs = new LinkedList<>();
    private int technologyLevel = 0;
    private int undistributedProgress = 0;
    private Map<EarthUpgrade.Type, Integer> progress;
    private EvacuationState evacuationState;

    public EarthState()
    {
        availableUpgrades.add(new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon")));
        availableUpgrades.add(new LabUpgrade());
        availableUpgrades.add(new WorkshopUpgrade());
        availableUpgrades.add(new BarracksUpgrade());
        availableUpgrades.add(new AstroDroneUpgrade());
        availableUpgrades.add(new MedBayUpgrade());
        progress = new HashMap<>();
        progress.put(EarthUpgrade.Type.EARTH, 0);
        progress.put(EarthUpgrade.Type.SHIP, 0);
        progress.put(EarthUpgrade.Type.SPACE, 0);
    }

    public void updateTechnologyLevel(int value) {
        undistributedProgress += value;
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

    public int getProgress(EarthUpgrade.Type type) {
        return progress.get(type);
    }

    public int getUndistributedProgress() {
        return undistributedProgress;
    }

    public void addProgress(World world, EarthUpgrade.Type type, int amount)
    {
        int oldValue = getProgress(type);
        int newValue = oldValue + amount;
        for (EarthUpgrade upgrade : EarthUpgrade.getUpgrades(type)) {
            if (upgrade.getValue() < oldValue) {
                continue;
            }

            if (upgrade.getValue() > newValue) {
                break;
            }
            upgrade.unlock(world);
        }
        progress.put(type, newValue);
        undistributedProgress -= amount;
    }
}

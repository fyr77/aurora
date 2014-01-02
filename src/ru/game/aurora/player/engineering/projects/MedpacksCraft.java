package ru.game.aurora.player.engineering.projects;

import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.MedPack;

/**
 * Date: 23.12.13
 * Time: 17:31
 */
public class MedpacksCraft extends EngineeringProject {
    private static final long serialVersionUID = -2561453708074000488L;
    private int amount;

    public MedpacksCraft(int amount) {
        super("medpacks_craft", "medpack");
        remainingProgress = 10;
        this.amount = amount;
    }

    @Override
    public void onCompleted(World world) {
        Ship ship = world.getPlayer().getShip();
        MedPack mp = new MedPack();
        ship.addItem(mp, amount);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}

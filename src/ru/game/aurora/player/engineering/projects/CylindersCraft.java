package ru.game.aurora.player.engineering.projects;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Cylinders;

/**
 * Date: 03.01.14
 * Time: 4:17
 */
public class CylindersCraft extends EngineeringProject {
    private static final long serialVersionUID = -7257363608665894778L;

    private int amount;
    private static final int progress = 10;

    public CylindersCraft(int amount) {
        super("cylinders_craft", "oxygen_tank", progress);
        this.amount = amount;
    }

    @Override
    public void onCompleted(World world) {
        super.onCompleted(world);
        world.getPlayer().getShip().addItem(new Cylinders(), amount);
        remainingProgress = progress;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public int getCost() {
        return Configuration.getIntProperty("engineering.craft_cost.cylinders");
    }
}

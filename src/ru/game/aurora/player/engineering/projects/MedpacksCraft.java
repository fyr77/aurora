package ru.game.aurora.player.engineering.projects;

import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.MedPack;

/**
 * Date: 23.12.13
 * Time: 17:31
 */
public class MedpacksCraft extends EngineeringProject {
    private static final long serialVersionUID = -2561453708074000488L;

    private int amount; //сколько делать аптечек за один "проект"
    private static final int progress = 10; //как долго будет идти создание аптечек

    public MedpacksCraft(int amount) {
        super("medpacks_craft", "medpack");
        remainingProgress = progress;
        this.amount = amount;
    }

    @Override
    public void onCompleted(World world) {
        world.getPlayer().getShip().addItem(new MedPack(), amount);
        remainingProgress = progress;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}

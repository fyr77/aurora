package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 17:34
 */
public class WorkshopUpgrade extends ShipUpgrade {
    static final int size = Configuration.getIntProperty("upgrades.workshop.crew");

    private static final long serialVersionUID = 1L;

    public WorkshopUpgrade() {
        super("workshop_upgrade", "workshop_module", "upgrades");
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.setMaxEngineers(ship.getMaxEngineers() + size);
        world.getPlayer().getEngineeringState().setIdleEngineers(world.getPlayer().getEngineeringState().getIdleEngineers() + size);
        world.onCrewChanged();
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        ship.setMaxEngineers(ship.getMaxEngineers() - size);

        int engineersToRemove = size;

        final EngineeringState engineeringState = world.getPlayer().getEngineeringState();
        if (engineeringState.getIdleEngineers() > 0) {
            int idleToRemove = Math.min(engineersToRemove, engineeringState.getIdleEngineers());
            engineeringState.setIdleEngineers(engineeringState.getIdleEngineers() - idleToRemove);
            engineersToRemove -= idleToRemove;
        }

        for (EngineeringProject epr : engineeringState.getProjects()) {
            if (engineersToRemove <= 0) {
                break;
            }
            int projectScientistsToRemove = Math.min(engineersToRemove, epr.getEngineersAssigned());
            epr.changeEngineers(-projectScientistsToRemove, world);
            engineersToRemove -= projectScientistsToRemove;
        }
        world.onCrewChanged();
    }

    @Override
    public int getSpace() {
        return Configuration.getIntProperty("upgrades.workshop.size");
    }
}

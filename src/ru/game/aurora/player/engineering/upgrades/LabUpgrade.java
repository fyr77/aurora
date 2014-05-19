package ru.game.aurora.player.engineering.upgrades;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.04.14
 * Time: 17:14
 */

public class LabUpgrade extends ShipUpgrade {
    static final int size = Configuration.getIntProperty("upgrades.lab.crew");

    private static final long serialVersionUID = 4448865642175738742L;

    public LabUpgrade() {
        super("lab_upgrade", "scientist_dialog", "upgrades");
    }

    @Override
    public void onInstalled(World world, Ship ship) {
        ship.setMaxScientists(ship.getMaxScientists() + size);
        world.getPlayer().getResearchState().setIdleScientists(world.getPlayer().getResearchState().getIdleScientists() + size);
        world.onCrewChanged();
    }

    @Override
    public void onRemoved(World world, Ship ship) {
        ship.setMaxScientists(ship.getMaxScientists() - size);

        int scientistsToRemove = size;

        final ResearchState researchState = world.getPlayer().getResearchState();
        if (researchState.getIdleScientists() > 0) {
            int idleToRemove = Math.min(scientistsToRemove, researchState.getIdleScientists());
            researchState.setIdleScientists(researchState.getIdleScientists() - idleToRemove);
            scientistsToRemove -= idleToRemove;
        }

        for (ResearchProjectState rps : researchState.getCurrentProjects()) {
            if (scientistsToRemove <= 0) {
                break;
            }
            int projectScientistsToRemove = Math.min(scientistsToRemove, rps.scientists);
            rps.scientists -= projectScientistsToRemove;
            scientistsToRemove -= projectScientistsToRemove;
        }
        world.onCrewChanged();
    }

    @Override
    public int getSpace() {
        return Configuration.getIntProperty("upgrades.lab.size");
    }
}

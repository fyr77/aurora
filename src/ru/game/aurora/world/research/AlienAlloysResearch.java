package ru.game.aurora.world.research;

import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

public class AlienAlloysResearch extends BaseResearchWithFixedProgress
{
    public AlienAlloysResearch() {
        super( "alien_alloys"
                , "hull_module"
                , new ResearchReport(
                        "hull_module"
                        , "alien_alloys.report"
                ),
                150,
                100);
    }

    @Override
    public void onCompleted(World world) {
        world.getPlayer().getEarthState().getAvailableUpgrades().add(new ShipUpgrade("hull", "hull_module", "upgrades") {
            @Override
            public void onInstalled(World world, Ship ship) {
                ship.changeMaxHull(5);
                ship.setHull(ship.getMaxHull());
            }

            @Override
            public void onRemoved(World world, Ship ship) {
                ship.changeMaxHull(-5);
                ship.setHull(ship.getMaxHull());
            }

            @Override
            public int getSpace() {
                return 20;
            }
        });
    }
}

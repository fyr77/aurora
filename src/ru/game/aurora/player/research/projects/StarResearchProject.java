/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 14:44
 */

package ru.game.aurora.player.research.projects;

import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

public class StarResearchProject extends BaseResearchWithFixedProgress {
    private static final long serialVersionUID = 2376161445036847151L;

    private StarSystem starSystem;

    public StarResearchProject(StarSystem starSystem) {
        super(String.format("Star exploration at [%d, %d]"
                , starSystem.getGlobalMapX()
                , starSystem.getGlobalMapY())
                , "Collecting of detailed data about anomalous star in specific star system, located at given star coordinates. In order to perform measurements, starship must stay close to star"
                , "star_research"
                , 30);
        this.starSystem = starSystem;
    }

    @Override
    public void update(World world, int scientists) {
        if (world.getCurrentStarSystem() != starSystem) {
            return;
        }

        if (Math.abs(world.getPlayer().getShip().getX()) + Math.abs(world.getPlayer().getShip().getY()) > 4) {
            return;
        }
        progress -= scientists;
    }

    @Override
    public String getStatusString(World world, int scientists) {

        if (world.getCurrentStarSystem() != starSystem) {
            return "Must be in specific star system";
        }

        if (Math.abs(world.getPlayer().getShip().getX()) + Math.abs(world.getPlayer().getShip().getY()) > 4) {
            return "Must be closer to star";
        }

        if (progress > 0) {
            return "Processing, " + progress + " data remaining";
        }

        return "Completed";
    }

    @Override
    public int getScore() {
        return 50;
    }
}

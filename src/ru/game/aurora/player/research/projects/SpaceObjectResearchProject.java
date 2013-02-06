/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:27
 */

package ru.game.aurora.player.research.projects;

import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Research of a space object that resides in given star system.
 * In order for research to progress, player ship must be in given system and stay close enough to object
 */
public class SpaceObjectResearchProject extends BaseResearchWithFixedProgress {

    private static final long serialVersionUID = 2376161445036847151L;

    protected StarSystem starSystem;

    protected BasePositionable position;

    public SpaceObjectResearchProject(String name, String description, String icon, int initialProgress, StarSystem starSystem, BasePositionable object) {
        super(name, description, icon, initialProgress);
        this.starSystem = starSystem;
        this.position = object;
    }

    protected boolean isInRange(World world)
    {
        return Math.pow(position.getX() - world.getPlayer().getShip().getX(), 2) + Math.pow(world.getPlayer().getShip().getY() - position.getY(), 2) <= 16;
    }

    @Override
    public void update(World world, int scientists) {
        if (world.getCurrentStarSystem() != starSystem) {
            return;
        }

        if (!isInRange(world)) {
            return;
        }
        progress -= scientists;
    }

    @Override
    public String getStatusString(World world, int scientists) {

        if (world.getCurrentStarSystem() != starSystem) {
            return "Must be in specific star system";
        }

        if (!isInRange(world)) {
            return "Must be closer to object";
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

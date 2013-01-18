/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 16:40
 */
package ru.game.aurora.player.research;


import ru.game.aurora.world.World;

public abstract class BaseResearchWithFixedProgress extends ResearchProjectDesc {
    protected int progress;

    protected final int initialProgress;

    public BaseResearchWithFixedProgress(String name, String description, String icon, int initialProgress) {
        super(name, description, icon);
        this.initialProgress = initialProgress;
        this.progress = initialProgress;
    }

    @Override
    public void update(World world, int scientists) {
        progress -= scientists;
    }

    @Override
    public String getStatusString(World world, int scientists) {
        if (progress > initialProgress * 0.6) {
            return "Poor";
        }

        if (progress > initialProgress * 0.3) {
            return "Good";
        }

        if (progress > 0) {
            return "Almost done";
        }

        return "Completed";
    }

    @Override
    public boolean isCompleted() {
        return progress <= 0;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
}

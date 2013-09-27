/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 18.01.13
 * Time: 16:40
 */
package ru.game.aurora.player.research;


import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.world.World;

public class BaseResearchWithFixedProgress extends ResearchProjectDesc implements JsonConfigManager.EntityWithId {
    private static final long serialVersionUID = 1L;

    protected int progress;

    protected int score;

    protected final int initialProgress;

    public BaseResearchWithFixedProgress(String id, String icon, int initialProgress, int score) {
        super(id, icon);
        this.initialProgress = initialProgress;
        this.progress = initialProgress;
        this.score = score;
    }

    public BaseResearchWithFixedProgress(String id, String icon, ResearchReport report, int initialProgress, int score) {
        this(id, icon, initialProgress, score);
        this.report = report;
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

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public String getId() {
        return id;
    }
}

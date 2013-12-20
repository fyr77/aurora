/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 13:48
 */
package ru.game.aurora.player.research.projects;


import ru.game.aurora.application.Localization;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.World;

public class ArtifactResearch extends ResearchProjectDesc {
    private static final long serialVersionUID = 1L;

    private int progress = 100;

    private double speedModifier;

    public ArtifactResearch(ResearchReport report) {
        super("artifact", "artifact_research", report);
    }

    public void setSpeedModifier(double speedModifier) {
        this.speedModifier = speedModifier;
    }

    @Override
    public void update(World world, int scientists) {
        progress -= Math.max(1, scientists * speedModifier);
    }

    @Override
    public String getStatusString(World world, int scientists) {
        if (progress > 60) {
            return Localization.getText("research", "poor");
        }

        if (progress > 30) {
            return Localization.getText("research", "good");
        }

        if (progress > 0) {
            return Localization.getText("research", "almost_done");
        }

        return Localization.getText("research", "done");
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
        return 50;
    }

}

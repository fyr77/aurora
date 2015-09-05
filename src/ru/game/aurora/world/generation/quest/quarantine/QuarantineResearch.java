package ru.game.aurora.world.generation.quest.quarantine;

import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.world.World;

/**
 * Research for a medicine for Quarantine quest
 */
public class QuarantineResearch extends BaseResearchWithFixedProgress {
    private double researchBoost;

    public QuarantineResearch(double researchBoost) {
        super("quarantine_medicine", "bio_research", 600, 50);
        this.researchBoost = researchBoost;
    }

    @Override
    public void update(World world, int scientists) {
        progress -= scientists * researchBoost;

        if (progress <= 0) {
            QuarantineQuest.endQuest(world);
        }
    }
}

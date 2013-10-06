/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.03.13
 * Time: 13:11
 */
package ru.game.aurora.world.generation.humanity;

import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.projects.AnimalResearch;
import ru.game.aurora.world.World;


public class BiologyResearch extends EarthResearch {
    private static final long serialVersionUID = -3631665834356175502L;

    private int materialCount = 0;

    private static final int TARGET_COUNT = 5;

    public BiologyResearch() {
        super("cancer_cure", Integer.MAX_VALUE);
    }

    @Override
    protected void onCompleted(World world) {
    }

    @Override
    public boolean isAlive() {
        return materialCount < TARGET_COUNT;
    }

    @Override
    public void onReturnToEarth(World world) {
        for (ResearchProjectDesc p : world.getPlayer().getResearchState().getCompletedProjects()) {
            if (p instanceof AnimalResearch) {
                materialCount++;
            }
        }

        if (materialCount >= TARGET_COUNT) {
            world.getPlayer().getEarthState().getMessages().add(
                    new PrivateMessage(
                            "cancer_cure"
                            , "news"
                    )
            );

            world.getPlayer().getEarthState().updateTechnologyLevel(100);

        }
    }
}

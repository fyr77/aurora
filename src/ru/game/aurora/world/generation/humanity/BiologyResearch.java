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


public class BiologyResearch extends EarthResearch
{
    private static final long serialVersionUID = -3631665834356175502L;

    private int materialCount = 0;

    private static final int TARGET_COUNT = 5;

    public BiologyResearch() {
        super(Integer.MAX_VALUE);
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
        for (ResearchProjectDesc p : world.getPlayer().getResearchState().getCompletedProjects())
        {
            if (p instanceof AnimalResearch) {
                materialCount++;
            }
        }

        if (materialCount >= TARGET_COUNT) {
            world.getPlayer().getEarthState().getMessages().add(
                    new PrivateMessage(
                            "Cancer cure comes from space"
                            , "As it was announced on recent annual congress on Extraterrestrial Biology, scientists have discovered a possible way to effectively cure cancer. Special " +
                            " type of immune system cells, found in one of alien lifeforms brought from distant planet by Aurora project ship, effectively destroys cancer cells and swellings. \n" +
                            " Scientists declare that after they succeed in synthesizing such cells in human body, cancer will be completely defeated."
                            , "news"
                    )
            );

            world.getPlayer().getEarthState().updateTechnologyLevel(100);

        }
    }
}

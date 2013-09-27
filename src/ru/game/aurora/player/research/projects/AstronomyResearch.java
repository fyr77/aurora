package ru.game.aurora.player.research.projects;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.12.12
 * Time: 14:10
 */

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;


/**
 * Available in every star system.
 * Collects data about space objects such as planets/stars/etc
 */
public class AstronomyResearch extends ResearchProjectDesc
{

    private static final long serialVersionUID = -4290850863471850561L;

    public AstronomyResearch() {
        super("astronomy", "astronomy_research");
    }

    @Override
    public void update(World world, int scientists) {
        Room r = world.getCurrentRoom();
        if (!(r instanceof StarSystem)) {
            return;
        }

        StarSystem ss = (StarSystem) r;
        int dataToProcess = Math.min(scientists, (ss).getAstronomyData());
        world.getPlayer().getResearchState().addProcessedAstroData(dataToProcess);
        ss.setAstronomyData(ss.getAstronomyData() - dataToProcess);
        if (dataToProcess > 0 && ss.getAstronomyData() <= 0) {
            GameLogger.getInstance().logMessage(Localization.getText("research", "astronomy.all_data_collected"));
        }
    }

    @Override
    public String getStatusString(World world, int scientists) {
        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null) {
            return Localization.getText("research", "astronomy.paused");
        }

        return String.format(Localization.getText("research", "astronomy.status"), ss.getAstronomyData(), ss.getAstronomyData() / (float) scientists);
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public int getScore() {
        return 0;
    }
}

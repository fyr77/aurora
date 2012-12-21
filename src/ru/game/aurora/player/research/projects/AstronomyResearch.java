package ru.game.aurora.player.research.projects;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.12.12
 * Time: 14:10
 */

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchScreen;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;


/**
 * Available in every star system.
 * Collects data about space objects such as planets/stars/etc
 */
public class AstronomyResearch extends ResearchProjectDesc {
    public AstronomyResearch() {
        super("Astronomy", "Scan and explore space objects in current star system: build navigation maps, celestial bodies catalog and sailing directions", "astronomy_research");
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
            GameLogger.getInstance().logMessage("All astronomy data collected for this star system");
        }
    }

    @Override
    public String getStatusString(World world, int scientists) {
        Room r = world.getCurrentRoom();
        if (r instanceof ResearchScreen) {
            r = ((ResearchScreen) r).getPreviousRoom();
        }
        if (!(r instanceof StarSystem)) {
            return "Research paused, no suitable celestial bodies nearby";
        }

        StarSystem ss = (StarSystem) r;
        return String.format("Remaining %d astro data for this star system, %f days to finish", ss.getAstronomyData(), ss.getAstronomyData() / (float) scientists);
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

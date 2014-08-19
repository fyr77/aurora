package ru.game.aurora.player.research.projects;

import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.JournalEntry;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.13
 * Time: 15:35
 */
public class AlienRaceResearch extends BaseResearchWithFixedProgress
{
    private static final long serialVersionUID = -5140126328244757862L;

    private final JournalEntry entry;

    public AlienRaceResearch(String id, AlienRace race, JournalEntry entry) {
        super(id, race.getDefaultDialog().getIconName(), new ResearchReport(race.getDefaultDialog().getIconName(), "race_research.report"), 50, 50);
        this.entry = entry;
    }

    @Override
    public void onCompleted(World world)
    {
        world.getPlayer().getJournal().addCodex(entry);
    }
}

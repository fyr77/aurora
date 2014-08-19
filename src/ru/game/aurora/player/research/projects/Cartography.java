/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:38
 */

package ru.game.aurora.player.research.projects;

import ru.game.aurora.application.Localization;
import ru.game.aurora.player.research.Geodata;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.world.World;

/**
 * Cartography project converts Raw Geodata into Processed Geodata.
 * Is always available
 */
public class Cartography extends ResearchProjectDesc
{
    private static final long serialVersionUID = 4588596463836210769L;

    private final Geodata geodata;

    public Cartography(Geodata geodata) {
        super("cartography", "cartography_research");
        this.geodata = geodata;
    }

    @Override
    public void update(World world, int scientists) {
        geodata.process(scientists);
    }

    @Override
    public String getStatusString(World world, int scientists) {
        return String.format(Localization.getText("research", "cartography.status"), geodata.getRaw(), scientists != 0 ? Integer.toString(geodata.getRaw() / scientists) : "<inf>");
    }

    @Override
    public boolean isCompleted() {
        return geodata.getRaw() == 0;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    /*
     * Score for cartography is calculated by Geodata class
     */
    @Override
    public int getScore() {
        return 0;
    }
}
